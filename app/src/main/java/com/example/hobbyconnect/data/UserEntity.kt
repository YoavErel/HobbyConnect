package com.example.hobbyconnect.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val avatarUri: String?,
    val hobbies: String? = null


)
