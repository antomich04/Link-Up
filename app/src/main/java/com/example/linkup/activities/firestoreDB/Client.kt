package com.example.linkup.activities.firestoreDB

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore

class Client{
    private val db = Firebase.firestore
    private val userRef = db.collection("Users")
    private val friendRequestRef = db.collection("FriendRequests")
    private val friendshipRef = db.collection("Friendships")
    private val blocksRef = db.collection("Blocks")
    private val chatRef = db.collection("Chats")

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

    fun getUserCredentials(username: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = userRef.document(username)
        docRef.get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val user = document.toObject(User::class.java)
                    if(user!=null){
                        onSuccess(user.name)
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

        //Checks if receiver has blocked the sender
        blocksRef.whereEqualTo("userUsername", receiver)
            .whereEqualTo("blockedUsername", sender)
            .get()
            .addOnSuccessListener { receiverBlockSnapshot ->
                if(!receiverBlockSnapshot.isEmpty){
                    onFailure(Exception("User not found"))
                    return@addOnSuccessListener
                }

                //Checks if sender has blocked the receiver
                blocksRef.whereEqualTo("userUsername", sender)
                    .whereEqualTo("blockedUsername", receiver)
                    .get()
                    .addOnSuccessListener { senderBlockSnapshot ->
                        if(!senderBlockSnapshot.isEmpty){
                            onFailure(Exception("User is blocked"))
                            return@addOnSuccessListener
                        }

                        //Checks if a request already exists
                        val checkSenderQuery = friendRequestRef.whereEqualTo("sender", senderRef)
                            .whereEqualTo("receiver", receiverRef)
                            .whereIn("status", listOf(FriendRequestStatus.PENDING.value, FriendRequestStatus.ACCEPTED.value))

                        val checkReceiverQuery = friendRequestRef.whereEqualTo("sender", receiverRef)
                            .whereEqualTo("receiver", senderRef)
                            .whereIn("status", listOf(FriendRequestStatus.PENDING.value, FriendRequestStatus.ACCEPTED.value))

                        checkSenderQuery.get().addOnSuccessListener { senderSnapshot ->
                            if(senderSnapshot.isEmpty){
                                checkReceiverQuery.get().addOnSuccessListener { receiverSnapshot ->
                                    if(receiverSnapshot.isEmpty){
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
                                        onFailure(Exception("Friend request already sent or already friends"))
                                    }
                                }
                            }else{
                                onFailure(Exception("Friend request already sent or already friends"))
                            }
                        }.addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                    }.addOnFailureListener { exception ->
                        onFailure(exception)
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


    fun updateUserReferences(oldUsername: String, newUsername: String, onComplete: () -> Unit){
        val oldUserRef = userRef.document(oldUsername)
        val newUserRef = userRef.document(newUsername)

        //Creates a batch for document updates
        val batch = db.batch()

        //Updates all references except chats
        val basicReferenceTasks = listOf(
            friendRequestRef.whereEqualTo("sender", oldUserRef).get(),
            friendRequestRef.whereEqualTo("receiver", oldUserRef).get(),
            friendshipRef.whereEqualTo("userUsername", oldUserRef).get(),
            friendshipRef.whereEqualTo("friendUsername", oldUserRef).get(),
            blocksRef.whereEqualTo("userUsername", oldUsername).get()
        )

        //Handles chat updates separately
        val chatQueryTasks = listOf(
            chatRef.whereEqualTo("sender", oldUsername).get(),
            chatRef.whereEqualTo("receiver", oldUsername).get()
        )

        Tasks.whenAllSuccess<QuerySnapshot>(basicReferenceTasks).addOnSuccessListener{ results ->
            val sentRequests = results[0]
            val receivedRequests = results[1]
            val sentFriends = results[2]
            val receivedFriends = results[3]
            val blocks = results[4]

            sentRequests.forEach { doc -> batch.update(doc.reference, "sender", newUserRef) }
            receivedRequests.forEach { doc -> batch.update(doc.reference, "receiver", newUserRef) }
            sentFriends.forEach { doc -> batch.update(doc.reference, "userUsername", newUserRef) }
            receivedFriends.forEach { doc -> batch.update(doc.reference, "friendUsername", newUserRef) }
            blocks.forEach { doc -> batch.update(doc.reference, "userUsername", newUsername) }

            //Handles chat migrations
            Tasks.whenAllSuccess<QuerySnapshot>(chatQueryTasks).addOnSuccessListener { chatResults ->
                val sentChats = chatResults[0]
                val receivedChats = chatResults[1]

                //Gets all unique chat documents that need to be migrated
                val chatDocsToUpdate = (sentChats + receivedChats).distinctBy { it.id }

                //If there are no chats to update, commits the batch and finishes
                if(chatDocsToUpdate.isEmpty()){
                    batch.commit().addOnCompleteListener {
                        onComplete()
                    }
                    return@addOnSuccessListener
                }

                //For tracking completion of all chat migrations
                val chatMigrationTasks = mutableListOf<Task<*>>()

                //Processes each chat document
                for(chatDoc in chatDocsToUpdate){
                    val oldChatDocRef = chatRef.document(chatDoc.id)
                    val chatData = chatDoc.data ?: continue

                    //Determines the other user in the chat
                    val sender = chatData["sender"] as? String ?: continue
                    val receiver = chatData["receiver"] as? String ?: continue

                    val otherUser = if(sender == oldUsername) receiver else sender

                    //Calculates the old and new chat IDs
                    val oldChatId = getChatId(oldUsername, otherUser)
                    val newChatId = getChatId(newUsername, otherUser)

                    //Skips if the chat ID doesn't need to change
                    if(oldChatId == newChatId){
                        continue
                    }

                    val newChatDocRef = chatRef.document(newChatId)

                    //Updates the sender/receiver fields in the chat data
                    val updatedChatData = HashMap(chatData)
                    if(sender == oldUsername){
                        updatedChatData["sender"] = newUsername
                    }else{
                        updatedChatData["receiver"] = newUsername
                    }

                    //Creates a migration task for this chat and its messages
                    val chatMigrationTask = oldChatDocRef.collection("messages").get()
                        .continueWithTask { messagesTask ->
                            newChatDocRef.set(updatedChatData).continueWithTask{
                                //Gets messages from the old chat
                                val messageDocs = messagesTask.result?.documents ?: emptyList()

                                //If there are no messages, returns early
                                if(messageDocs.isEmpty()){
                                    return@continueWithTask Tasks.forResult(null)
                                }

                                //Creates tasks to migrate each message
                                val messageWriteTasks = messageDocs.map{ msgDoc ->
                                    val msgData = msgDoc.data ?: return@map Tasks.forResult(null)

                                    //Updates sender/receiver in message data if needed
                                    val updatedMsgData = HashMap(msgData)
                                    if((msgData["sender"] as? String) == oldUsername){
                                        updatedMsgData["sender"] = newUsername
                                    }
                                    if((msgData["receiver"] as? String) == oldUsername){
                                        updatedMsgData["receiver"] = newUsername
                                    }

                                    //Adds a migrated flag to indicate this message is copied
                                    updatedMsgData["migrated"] = true

                                    //Creates the message in the new location
                                    newChatDocRef.collection("messages").document(msgDoc.id).set(updatedMsgData)
                                }

                                //Waits for all message migrations to complete
                                Tasks.whenAllComplete(messageWriteTasks)
                            }
                        }
                        .continueWithTask{
                            //Deletes the old chat document and its messages after migration
                            oldChatDocRef.collection("messages").get().continueWithTask{ messagesSnapshot ->
                                val batch = db.batch()
                                messagesSnapshot.result?.documents?.forEach { doc ->
                                    batch.delete(doc.reference)
                                }
                                batch.delete(oldChatDocRef)
                                batch.commit()
                            }
                        }

                    chatMigrationTasks.add(chatMigrationTask)
                }

                //When all chat migrations are complete, commits the batch with other reference updates
                Tasks.whenAllComplete(chatMigrationTasks).addOnCompleteListener{
                    batch.commit().addOnCompleteListener {
                        onComplete()
                    }
                }
            }
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


    //Used to save the token in an array for the FCM push notifications
    fun saveToken(username: String, token: String) {
        userRef.document(username).update("tokens", FieldValue.arrayUnion(token))
    }

    //Used to remove the token from the array when user logs out
    fun removeToken(username: String, token: String) {
        userRef.document(username).update("tokens", FieldValue.arrayRemove(token))
    }

    fun removeFriend(userUsername: String, friendUsername: String, onComplete: () -> Unit) {
        val userRef = db.document("Users/$userUsername")
        val friendRef = db.document("Users/$friendUsername")
        val batch = db.batch()

        //Deletes friendship document
        friendshipRef.whereEqualTo("userUsername", userRef)
            .whereEqualTo("friendUsername", friendRef)
            .get()
            .addOnSuccessListener { userFriendResult ->
                userFriendResult.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                friendshipRef.whereEqualTo("userUsername", friendRef)
                    .whereEqualTo("friendUsername", userRef)
                    .get()
                    .addOnSuccessListener { friendUserResult ->
                        friendUserResult.documents.forEach { doc ->
                            batch.delete(doc.reference)
                        }

                        //Deletes accepted friend requests in both directions, sent first
                        friendRequestRef.whereEqualTo("sender", userRef)
                            .whereEqualTo("receiver", friendRef)
                            .whereEqualTo("status", "accepted")
                            .get()
                            .addOnSuccessListener { sentRequests ->
                                sentRequests.documents.forEach { doc ->
                                    batch.delete(doc.reference)
                                }

                                //Received friend requests
                                friendRequestRef.whereEqualTo("sender", friendRef)
                                    .whereEqualTo("receiver", userRef)
                                    .whereEqualTo("status", "accepted")
                                    .get()
                                    .addOnSuccessListener { receivedRequests ->
                                        receivedRequests.documents.forEach { doc ->
                                            batch.delete(doc.reference)
                                        }

                                        //Commits all deletions in a single batch
                                        batch.commit().addOnCompleteListener {
                                            onComplete()
                                        }
                                    }
                            }
                    }
            }
    }

    fun deleteUser(username: String, onSuccess: () -> Unit) {
        val userRef = userRef.document(username)
        val batch = db.batch()

        //Gets all chats involving the user
        chatRef.whereEqualTo("sender", username).get()
            .addOnSuccessListener { senderChats ->
                senderChats.documents.forEach { chatDoc ->
                    val chatId = chatDoc.id
                    val chatDocRef = chatRef.document(chatId)
                    val messagesCollection = chatDocRef.collection("messages")

                    //Deletes all messages in the chat
                    messagesCollection.get()
                        .addOnSuccessListener { messagesSnapshot ->
                            val messageDeleteBatch = db.batch()
                            messagesSnapshot.documents.forEach { messageDoc ->
                                messageDeleteBatch.delete(messageDoc.reference)
                            }

                            messageDeleteBatch.commit()
                                .addOnSuccessListener {
                                    batch.delete(chatDocRef)
                                }
                        }
                }

                //Same for chats where the user is the receiver
                chatRef.whereEqualTo("receiver", username).get()
                    .addOnSuccessListener { receiverChats ->
                        receiverChats.documents.forEach { chatDoc ->
                            val chatId = chatDoc.id
                            val chatDocRef = chatRef.document(chatId)
                            val messagesCollection = chatDocRef.collection("messages")

                            messagesCollection.get()
                                .addOnSuccessListener { messagesSnapshot ->
                                    val messageDeleteBatch = db.batch()
                                    messagesSnapshot.documents.forEach { messageDoc ->
                                        messageDeleteBatch.delete(messageDoc.reference)
                                    }

                                    messageDeleteBatch.commit()
                                        .addOnSuccessListener {
                                            batch.delete(chatDocRef)
                                        }
                                }
                        }

                        //Deletes all friend requests involving the user
                        friendRequestRef.whereEqualTo("sender", userRef).get()
                            .addOnSuccessListener { sentRequests ->
                                sentRequests.documents.forEach { doc ->
                                    batch.delete(doc.reference)
                                }

                                friendRequestRef.whereEqualTo("receiver", userRef).get()
                                    .addOnSuccessListener { receivedRequests ->
                                        receivedRequests.documents.forEach { doc ->
                                            batch.delete(doc.reference)
                                        }

                                        //Deletes all friendships involving the user
                                        friendshipRef.whereEqualTo("userUsername", userRef).get()
                                            .addOnSuccessListener { userFriendships ->
                                                userFriendships.documents.forEach { doc ->
                                                    batch.delete(doc.reference)
                                                }

                                                friendshipRef.whereEqualTo("friendUsername", userRef).get()
                                                    .addOnSuccessListener { friendFriendships ->
                                                        friendFriendships.documents.forEach { doc ->
                                                            batch.delete(doc.reference)
                                                        }

                                                        //Deletes the user document
                                                        batch.delete(userRef)

                                                        //Commits all operations after deleting chats, messages, and related data
                                                        batch.commit()
                                                            .addOnSuccessListener {
                                                                onSuccess()
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
            }
    }

    fun blockUser(userUsername: String, blockedUsername: String, onSuccess: () -> Unit){
        removeFriend(userUsername, blockedUsername){
            blocksRef.document().set(RemoteBlocks(userUsername, blockedUsername))
                .addOnSuccessListener{
                    onSuccess()
                }
        }
    }

    fun unblockUser(userUsername: String, blockedUsername: String, onSuccess: () -> Unit) {
        blocksRef
            .whereEqualTo("userUsername", userUsername)
            .whereEqualTo("blockedUsername", blockedUsername)
            .get()
            .addOnSuccessListener { result ->
                if(!result.isEmpty){
                    val batch = db.batch()
                    result.documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            onSuccess()
                        }
                }
            }
    }

    fun getChatMessages(userUsername: String, friendUsername: String): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val chatId = getChatId(userUsername, friendUsername)
        val chatDocRef = chatRef.document(chatId)

        chatDocRef.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                val messages = snapshots?.mapNotNull { it.toObject(Message::class.java) } ?: emptyList()
                messagesLiveData.postValue(messages)
            }

        return messagesLiveData
    }

    fun getChats(userUsername: String, friendUsername: String): LiveData<List<Chat>> {
        val chatsLiveData = MutableLiveData<List<Chat>>()
        val chatID = getChatId(userUsername, friendUsername)

        chatRef.document(chatID)
            .addSnapshotListener { snapshot, _ ->
                if(snapshot != null && snapshot.exists()){
                    snapshot.toObject(Chat::class.java)?.let{
                        chatsLiveData.postValue(listOf(it))
                    }
                }else{
                    chatsLiveData.postValue(emptyList())
                }
            }

        return chatsLiveData
    }

    fun createChat(userUsername: String, friendUsername: String) {
        val chatId = getChatId(userUsername, friendUsername)
        val chatDocRef = chatRef.document(chatId)

        chatDocRef.get().addOnSuccessListener { docSnapshot ->
            if(!docSnapshot.exists()){
                val chatMap = hashMapOf(
                    "id" to chatId,
                    "sender" to userUsername,
                    "receiver" to friendUsername,
                    "lastMessage" to "",
                    "timestamp" to FieldValue.serverTimestamp()
                )
                chatDocRef.set(chatMap)
            }
        }
    }

    //Used to create a unique chat ID
    fun getChatId(userA: String, userB: String): String {
        //Sorts the user names lexicographically to ensure consistent ordering
        val sortedUsers = listOf(userA, userB).sorted()
        return "${sortedUsers[0]}-${sortedUsers[1]}"
    }

    fun sendMessage(sender: String, receiver: String, text: String, onSuccess: () -> Unit) {
        val chatId = getChatId(sender, receiver)
        val chatDocRef = chatRef.document(chatId)
        val messagesCollection = chatDocRef.collection("messages")
        val newMessageRef = messagesCollection.document()

        //Prepares message map to use server timestamp
        val messageMap = hashMapOf(
            "id" to newMessageRef.id,
            "sender" to sender,
            "receiver" to receiver,
            "text" to text,
            "timestamp" to FieldValue.serverTimestamp(),
            "seen" to false,
            "seenTimestamp" to null
        )

        newMessageRef.set(messageMap)
            .addOnSuccessListener {
                //Updates chat with last message and server timestamp
                chatDocRef.update(
                    mapOf(
                        "lastMessage" to text,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                )
                onSuccess()
            }
    }

    fun markMessagesAsSeen(userUsername: String, friendUsername: String) {
        val chatId = getChatId(userUsername, friendUsername)
        val chatDocRef = chatRef.document(chatId)

        chatDocRef.collection("messages")
            .whereEqualTo("receiver", userUsername)
            .whereEqualTo("sender", friendUsername)
            .whereEqualTo("seen", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(querySnapshot.isEmpty){
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                val now = Timestamp.now()

                querySnapshot.documents.forEach { document ->
                    val messageRef = chatDocRef.collection("messages").document(document.id)
                    batch.update(messageRef,
                        mapOf(
                            "seen" to true,
                            "seenTimestamp" to now
                        )
                    )
                }

                batch.commit()
            }
    }

    fun deleteChat(userUsername: String, friendUsername: String, onSuccess: () -> Unit) {
        val chatID = getChatId(userUsername, friendUsername)
        val chatDocRef = chatRef.document(chatID)

        chatDocRef.collection("messages")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()

                //Deletes each message in the chat
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                //Deletes the chat document itself
                batch.delete(chatDocRef)

                //Commits the batch delete
                batch.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }
            }
    }

    fun deleteMessage(chatId: String, messageId: String, onSuccess: () -> Unit) {
        chatRef.document(chatId)
            .collection("messages")
            .document(messageId)
            .delete()
            .addOnSuccessListener{
                //Finds the new last message by timestamp
                chatRef.document(chatId)
                    .collection("messages")
                    .whereNotEqualTo("timestamp", null)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if(snapshot.isEmpty){
                            chatRef.document(chatId).update("lastMessage", "")
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                        }else{
                            val lastMessage = snapshot.documents.firstOrNull()?.getString("text") ?: ""
                            chatRef.document(chatId).update("lastMessage", lastMessage)
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                        }
                    }
            }
    }
}