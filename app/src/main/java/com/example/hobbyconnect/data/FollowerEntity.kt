package com.example.hobbyconnect.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followers")
data class FollowerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val currentUser: String,
    val followedUser: String
)
