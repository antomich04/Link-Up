package com.example.linkup.activities.firestoreDB

import com.google.firebase.firestore.DocumentReference

data class Friendship(
    var userUsername: DocumentReference? = null,        //Username of the user logged in
    var friendUsername: DocumentReference? = null,      //Username of the user being added as friend
)
