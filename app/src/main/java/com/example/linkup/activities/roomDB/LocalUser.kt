package com.example.linkup.activities.roomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Users")
data class LocalUser(
    @PrimaryKey
    var username: String,
    @ColumnInfo
    var password: String,
    @ColumnInfo
    var email: String,
    @ColumnInfo
    var name: String
)