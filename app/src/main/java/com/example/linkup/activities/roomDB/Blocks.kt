package com.example.linkup.activities.roomDB

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "Blocks", primaryKeys = ["userUsername", "blockedUsername"],
    foreignKeys = [ForeignKey(
        entity = LocalUser::class,
        parentColumns = ["username"],
        childColumns = ["userUsername"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class Blocks(
    val userUsername: String,
    val blockedUsername: String,
    val blockedAt: Long
)