package com.example.linkup.activities.firestoreDB

data class Message(
    var id: String = "",
    var sender: String = "",
    var receiver: String = "",
    var text: String = "",
    var timestamp: Long = System.currentTimeMillis(),   //Initialized to avoid null conflicts
    var seen: Boolean = false,
    var seenTimestamp: Long = 0,
    var isLiked: Boolean = false
)
