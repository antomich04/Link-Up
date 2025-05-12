package com.example.linkup.activities.firestoreDB

import com.google.firebase.Timestamp

data class Message(
    var id: String = "",
    var sender: String = "",
    var receiver: String = "",
    var text: String = "",
    var timestamp: Timestamp? = null,   //Initialized to avoid null conflicts
    var seen: Boolean = false,
    var seenTimestamp: Timestamp? = null
)
