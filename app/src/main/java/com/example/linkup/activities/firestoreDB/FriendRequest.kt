package com.example.linkup.activities.firestoreDB

import com.google.firebase.firestore.DocumentReference

data class FriendRequest(
    var sender: DocumentReference? = null,
    var receiver: DocumentReference? = null,
    var status: String = FriendRequestStatus.PENDING.value
)
