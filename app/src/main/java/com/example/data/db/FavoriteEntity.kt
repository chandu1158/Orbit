package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val lookupKey: String,
    val displayName: String,
    val phoneNumber: String,
    val photoUri: String?,
    val orderIndex: Int = 0
)
