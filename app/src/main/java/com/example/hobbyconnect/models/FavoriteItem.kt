package com.example.hobbyconnect.models


data class FavoriteItem(
    val userName: String,
    val action: String,
    val timestamp: String,
    val profileImageUrl: String,
    val postImageUrl: String? = null, // Nullable if action isn't related to a post
    val isFollowable: Boolean = false // Add this for actions like "Follow"
)
