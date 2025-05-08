package com.example.linkup.activities.firestoreDB

data class Chat(
    var id: String = "",
    var sender: String = "",
    var receiver: String = "",
    var lastMessage: String = "",
    var timestamp: Long = System.currentTimeMillis() //Represents the time the last message was sent
)