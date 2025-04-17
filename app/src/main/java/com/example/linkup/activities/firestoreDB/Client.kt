package com.example.linkup.activities.firestoreDB

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class Client{
    private val db = Firebase.firestore
    private val userRef = db.collection("Users")
    private val friendRequestRef = db.collection("FriendRequests")

    fun insertUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        val docRef = userRef.document(user.username)

        docRef.get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    //Username already exists
                    onFailure(Exception("Username already taken"))
                }else{
                    //Safe to insert
                    docRef.set(user)
                        .addOnSuccessListener{
                            onSuccess()
                        }
                        .addOnFailureListener {e ->
                            onFailure(e)
                        }
                }
            }
            .addOnFailureListener{e ->
                onFailure(e)
            }
    }

    fun checkIfUsernameExists(username: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit){
        val docRef = userRef.document(username)
        docRef.get()
            .addOnSuccessListener{ document ->
                if(document.exists()){
                    //Username exists
                    onSuccess(true)
                }else{
                    //Username does not exist
                    onSuccess(false)
                }
            }
            .addOnFailureListener{ exception ->
                onFailure(exception)
            }
    }

    fun loginUser(username: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        val docRef = userRef.document(username)
        docRef.get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val user = document.toObject(User::class.java)
                    if(user != null && user.password == password){
                        onSuccess()
                    }else{
                        onFailure(LoginException("Incorrect password!"))
                    }
                }else{
                    onFailure(LoginException("User does not exist!"))
                }
            }
            .addOnFailureListener{ exception ->
                onFailure(exception)
            }
    }

    fun getUserCredentials(username: String, onSuccess: (String, String) -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = userRef.document(username)
        docRef.get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val user = document.toObject(User::class.java)
                    if(user!=null){
                        onSuccess(user.name, user.email)
                    }else{
                        onFailure(Exception("Failed to parse user data"))
                    }
                }else{
                    onFailure(Exception("User does not exist!"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun sendFriendRequest(sender: String, receiver: String, onSuccess: (String) -> Unit,onFailure: (Exception) -> Unit) {
        val senderRef = userRef.document(sender)
        val receiverRef = userRef.document(receiver)

        //Checks if a request already exists or it is already accepted
        friendRequestRef
            .whereEqualTo("sender", senderRef)
            .whereEqualTo("receiver", receiverRef)
            .whereIn("status", listOf(
                FriendRequestStatus.PENDING.value,
                FriendRequestStatus.ACCEPTED.value
            ))
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(!querySnapshot.isEmpty){
                    //A request already exists
                    onFailure(Exception("Friend request already sent or already friends"))
                }else{
                    //No request exists
                    val requestDoc = friendRequestRef.document()    //Generates a new document ID
                    val friendRequest = FriendRequest(
                        sender = senderRef,
                        receiver = receiverRef,
                        status = FriendRequestStatus.PENDING.value
                    )

                    requestDoc.set(friendRequest)
                        .addOnSuccessListener {
                            val generatedId = requestDoc.id
                            onSuccess(generatedId)
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getIncomingFriendRequests(username: String): LiveData<List<FriendRequest>> {
        val liveData = MutableLiveData<List<FriendRequest>>()
        val receiverRef = userRef.document(username)

        friendRequestRef
            .whereEqualTo("receiver", receiverRef)
            .whereEqualTo("status", FriendRequestStatus.PENDING.value)
            .addSnapshotListener { snapshot, exception ->
                if(exception!=null){
                    liveData.postValue(emptyList())
                    return@addSnapshotListener
                }

                if(snapshot!=null && !snapshot.isEmpty){
                    val requests = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FriendRequest::class.java)?.apply {
                            id = doc.id //Sets the document ID as the previously generated ID
                        }
                    }
                    liveData.postValue(requests)
                }else{
                    liveData.postValue(emptyList())
                }
            }

        return liveData
    }


    //Accepts a friend request by updating its status to "accepted"
    fun acceptFriendRequest(requestId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        friendRequestRef.document(requestId)
            .update("status", FriendRequestStatus.ACCEPTED.value)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    //Rejects a friend request by updating its status to "rejected"
    fun rejectFriendRequest(requestId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        friendRequestRef.document(requestId)
            .update("status", FriendRequestStatus.REJECTED.value)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

}