package com.example.linkup.activities.firestoreDB

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class Client{
    private val db = Firebase.firestore
    private val userRef = db.collection("Users")
    private val friendRequestRef = db.collection("FriendRequests")
    private val friendshipRef = db.collection("Friendships")

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

    fun sendFriendRequest(sender: String, receiver: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val senderRef = userRef.document(sender)
        val receiverRef = userRef.document(receiver)

        //Checks if there is a pending or accepted request
        val checkSenderQuery = friendRequestRef
            .whereEqualTo("sender", senderRef)
            .whereEqualTo("receiver", receiverRef)
            .whereIn("status", listOf(FriendRequestStatus.PENDING.value, FriendRequestStatus.ACCEPTED.value))

        val checkReceiverQuery = friendRequestRef
            .whereEqualTo("sender", receiverRef)
            .whereEqualTo("receiver", senderRef)
            .whereIn("status", listOf(FriendRequestStatus.PENDING.value, FriendRequestStatus.ACCEPTED.value))

        //Checks both directions (sender and receiver)
        checkSenderQuery.get().addOnSuccessListener { senderSnapshot ->
            if(senderSnapshot.isEmpty){
                checkReceiverQuery.get().addOnSuccessListener { receiverSnapshot ->
                    if(receiverSnapshot.isEmpty){
                        //No request exists — creates a new one
                        val requestDoc = friendRequestRef.document()
                        val friendRequest = FriendRequest(
                            sender = senderRef,
                            receiver = receiverRef,
                            status = FriendRequestStatus.PENDING.value
                        )

                        requestDoc.set(friendRequest)
                            .addOnSuccessListener {
                                onSuccess(requestDoc.id)
                            }
                            .addOnFailureListener { exception ->
                                onFailure(exception)
                            }
                    }else{
                        //A request or friendship already exists from the receiver
                        onFailure(Exception("Friend request already sent or already friends"))
                    }
                }
            }else{
                //A request or friendship already exists from the sender
                onFailure(Exception("Friend request already sent or already friends"))
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    //Gets the list of incoming friend requests of logged in user
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
                            id = doc.id
                        }
                    }
                    liveData.postValue(requests)
                }else{
                    liveData.postValue(emptyList())
                }
            }

        return liveData
    }

    //Gets the list of friends of logged in user
    fun getFriendsList(username: String): LiveData<List<Friendship>> {
        val liveData = MutableLiveData<List<Friendship>>()
        val userRef = userRef.document(username)
        val allFriends = mutableListOf<Friendship>()

        //Query for friendships where the user is the sender
        val sentFriendQuery = friendshipRef
            .whereEqualTo("userUsername", userRef)

        //Query for friendships where the user is the receiver
        val receivedFriendQuery = friendshipRef
            .whereEqualTo("friendUsername", userRef)

        //Query for both directions
        sentFriendQuery.addSnapshotListener { snapshot, exception ->
            if(exception!=null){
                liveData.postValue(emptyList())
                return@addSnapshotListener
            }

            snapshot?.let{
                val sentFriends = it.documents.mapNotNull { doc ->
                    doc.toObject(Friendship::class.java)?.apply {
                        id = doc.id
                    }
                }
                allFriends.removeAll { it.userUsername == userRef }
                allFriends.addAll(sentFriends)
                liveData.postValue(allFriends.toList())
            }
        }

        receivedFriendQuery.addSnapshotListener { snapshot, exception ->
            if(exception != null){
                liveData.postValue(emptyList())
                return@addSnapshotListener
            }

            snapshot?.let{
                val receivedFriends = it.documents.mapNotNull { doc ->
                    doc.toObject(Friendship::class.java)?.apply {
                        id = doc.id
                    }
                }
                allFriends.removeAll { it.friendUsername == userRef }
                allFriends.addAll(receivedFriends)
                liveData.postValue(allFriends.toList())
            }
        }
        return liveData
    }



    //Accepts a friend request by updating its status to "accepted" and creates a new 'friendship'
    fun acceptFriendRequest(requestId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        friendRequestRef.document(requestId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val friendRequest = documentSnapshot.toObject(FriendRequest::class.java)
                if(friendRequest!=null){
                    //Updates request status to ACCEPTED
                    friendRequestRef.document(requestId)
                        .update("status", FriendRequestStatus.ACCEPTED.value)
                        .addOnSuccessListener {
                            //Creates a new Friendship document
                            val newFriendship = Friendship(
                                userUsername = friendRequest.sender,
                                friendUsername = friendRequest.receiver
                            )

                            friendshipRef.add(newFriendship)
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                                .addOnFailureListener { exception ->
                                    onFailure(exception)
                                }
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                }else{
                    onFailure(Exception("Friend request not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    //Rejects a friend request by updating its status to "rejected"
    fun rejectFriendRequest(requestId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        friendRequestRef.document(requestId)
            .update("status", FriendRequestStatus.REJECTED.value)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun changeUsername(newUsername: String, oldUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val oldDocRef = userRef.document(oldUsername)
        val newDocRef = userRef.document(newUsername)

        oldDocRef.get().addOnSuccessListener { documentSnapshot ->
            if(documentSnapshot.exists()){
                val data = documentSnapshot.data
                if(data != null){
                    newDocRef.set(data)
                        .addOnSuccessListener {
                            userRef.document(newUsername).update("username", newUsername)
                                .addOnSuccessListener {
                                    oldDocRef.delete()
                                        .addOnSuccessListener {
                                            onSuccess()
                                        }
                                        .addOnFailureListener { exception -> onFailure(exception) }
                                }
                                .addOnFailureListener { exception -> onFailure(exception) }
                        }
                        .addOnFailureListener { exception -> onFailure(exception) }
                }else{
                    onFailure(Exception("No data found in old document"))
                }
            }else{
                onFailure(Exception("Old document does not exist"))
            }
        }.addOnFailureListener { exception -> onFailure(exception) }
    }

    fun updateFriendReferences(oldUsername: String, newUsername: String, onComplete: () -> Unit) {
        val oldUserRef = userRef.document(oldUsername)
        val newUserRef = userRef.document(newUsername)

        //Updates Friend Requests
        friendRequestRef.whereEqualTo("sender", oldUserRef).get().addOnSuccessListener { sentRequests ->
            for(doc in sentRequests.documents){
                doc.reference.update("sender", newUserRef)
            }
        }
        friendRequestRef.whereEqualTo("receiver", oldUserRef).get().addOnSuccessListener { receivedRequests ->
            for(doc in receivedRequests.documents){
                doc.reference.update("receiver", newUserRef)
            }
        }

        //Updates Friendships
        friendshipRef.whereEqualTo("userUsername", oldUserRef).get().addOnSuccessListener { sentFriends ->
            for(doc in sentFriends.documents){
                doc.reference.update("userUsername", newUserRef)
            }
        }
        friendshipRef.whereEqualTo("friendUsername", oldUserRef).get().addOnSuccessListener { receivedFriends ->
            for(doc in receivedFriends.documents){
                doc.reference.update("friendUsername", newUserRef)
            }
        }.addOnCompleteListener {
            onComplete()
        }
    }

    fun changePassword(username: String, newPassword: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        userRef.document(username).update("password", newPassword)
            .addOnSuccessListener{
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    //Used to save the token for the FCM push notifications
    fun saveToken(username: String, token: String){
        userRef.document(username).update("token", token)
    }
}