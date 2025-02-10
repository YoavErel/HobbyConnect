package com.example.hobbyconnect.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val photoPath: String, // Local path to the photo
    val timestamp: Long
)
