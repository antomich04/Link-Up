package com.example.linkup.activities.firestoreDB

enum class FriendRequestStatus(val value: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected");

    //Used to convert status given from Firestore to the corresponding enum
    companion object {
        fun fromValue(value: String): FriendRequestStatus {
            return FriendRequestStatus.entries.find { it.value == value.lowercase() }
                ?: throw IllegalArgumentException("Unknown FriendRequestStatus: $value")
        }
    }
}