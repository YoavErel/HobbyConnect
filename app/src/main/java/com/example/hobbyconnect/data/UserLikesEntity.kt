package com.example.hobbyconnect.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_likes")
data class UserLikesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val postId: String
)
