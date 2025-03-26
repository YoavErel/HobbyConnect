package com.example.hobbyconnect.models

data class Post(
    val id: String,
    val title: String,
    val userId: String,
    val username: String,
    val hobby: String,
    val description: String,
    val imageUrl: String?,
    val lastUpdated: Long,
    val avatarUrl: String? = null,
    val likedByCurrentUser: Boolean = false
)


