package com.example.hobbyconnect.utils

import com.example.hobbyconnect.data.PostEntity
import com.example.hobbyconnect.models.Post

fun PostEntity.toPost(): Post {
    return Post(
        id = this.id,
        userId = this.userId,
        username = this.username,
        title = this.title,
        hobby = this.hobby,
        description = this.description,
        imageUrl = this.imageUrl,
        lastUpdated = this.lastUpdated,
        likedByCurrentUser = this.likedByCurrentUser
    )
}
