package com.example.hobbyconnect.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val title: String,
    val userId: String, // Add this field to reference the user
    val username: String,
    val hobby: String,
    val description: String,
    val imageUrl: String?,
    val lastUpdated: Long,
    val likedByCurrentUser: Boolean = false
)
