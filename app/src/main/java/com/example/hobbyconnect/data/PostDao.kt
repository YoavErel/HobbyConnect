package com.example.hobbyconnect.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PostDao {

    // (1) Insert a single post, suspend
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertPost(post: PostEntity)

    // (2) Insert multiple
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(posts: List<PostEntity>)

    // (3) Get all posts, returning LiveData (NOT suspend)
    @Query("SELECT * FROM posts ORDER BY lastUpdated DESC")
    fun getAllPosts(): LiveData<List<PostEntity>>

    // OR if you prefer a direct List (suspend):

    @Query("SELECT * FROM posts ORDER BY lastUpdated DESC")
    fun getAllPostsOnce(): List<PostEntity>


    // (4) Search by string, returning LiveData
    @Query("""
       SELECT * FROM posts
       WHERE title LIKE '%' || :query || '%'
          OR hobby LIKE '%' || :query || '%'
          OR description LIKE '%' || :query || '%'
          OR username LIKE '%' || :query || '%'
       ORDER BY lastUpdated DESC
    """)
    fun searchPosts(query: String): LiveData<List<PostEntity>>

    // (5) Delete an entity
    @Delete
    fun deletePost(post: PostEntity)


        @Query("""
        SELECT * FROM posts 
        WHERE username IN (
            SELECT followedUser FROM followers WHERE currentUser = :currentUsername
        ) 
        OR username = :currentUsername
        ORDER BY lastUpdated DESC
    """)
        fun getPostsForHomeFeed(currentUsername: String): LiveData<List<PostEntity>>


    // (6) Delete by ID, returning # of rows deleted
    @Query("DELETE FROM posts WHERE id = :postId")
    fun deletePostById(postId: String): Int

    // (7) Example: get post by ID, returning a single LiveData
    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostById(postId: String): LiveData<PostEntity>

    // or a suspend single fetch:
    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostByIdOnce(postId: String): PostEntity?

    @Query("SELECT * FROM posts WHERE username = :username ORDER BY lastUpdated DESC")
    fun getPostsByUser(username: String): LiveData<List<PostEntity>>

    @Query("UPDATE posts SET likedByCurrentUser = :isLiked WHERE id = :postId")
    fun updatePostLikeStatus(postId: String, isLiked: Boolean)

    @Query("SELECT * FROM posts WHERE likedByCurrentUser = 1")
    fun getLikedPosts(): List<PostEntity>

    @Query("""
    SELECT * FROM posts WHERE id IN (
        SELECT postId FROM user_likes WHERE userId = :userId
    ) ORDER BY lastUpdated DESC
""")
    fun getLikedPostsByUser(userId: String): List<PostEntity>



    @Update
    fun updatePost(post: PostEntity)

}
