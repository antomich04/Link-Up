package com.example.linkup.activities.roomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "User_Preferences",
        foreignKeys = [ForeignKey(entity = LocalUser::class,parentColumns = ["username"],childColumns = ["username"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)])
data class UserPreferences(
    @PrimaryKey
    var username: String,
    @ColumnInfo
    var isLoggedIn : Boolean
)