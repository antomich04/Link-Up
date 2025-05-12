package com.example.linkup.activities.firestoreDB

import com.google.firebase.Timestamp

data class Chat(
    var id: String = "",
    var sender: String = "",
    var receiver: String = "",
    var lastMessage: String = "",
    var timestamp: Timestamp? = null //Represents the time the last message was sent
)