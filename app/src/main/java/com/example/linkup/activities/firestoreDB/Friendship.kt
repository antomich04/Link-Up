package com.example.linkup.activities.firestoreDB

import com.google.firebase.firestore.DocumentReference

data class Friendship(
    var id: String = "",
    var userUsername: DocumentReference? = null,        //Username of the logged in user
    var friendUsername: DocumentReference? = null,      //Username of the user being added as friend
)
