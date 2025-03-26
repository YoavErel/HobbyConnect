package com.example.hobbyconnect.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserLikesDao {

    @Query("SELECT postId FROM user_likes WHERE userId = :userId")
    fun getLikedPostsByUser(userId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLike(userLike: UserLikesEntity)

    @Query("DELETE FROM user_likes WHERE userId = :userId AND postId = :postId")
    fun removeLike(userId: String, postId: String)

    @Query("SELECT COUNT(*) FROM user_likes WHERE userId = :userId AND postId = :postId")
    fun isPostLiked(userId: String, postId: String): Int
}
