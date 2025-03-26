package com.example.hobbyconnect.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostRepository(
    private val postDao: PostDao,
    private val firebaseModel: FirebaseModel
) {

    /**
     * Return all posts as LiveData from the local DB (Room).
     */
    fun getAllPosts(): LiveData<List<PostEntity>> = postDao.getAllPosts()

    /**
     * Return posts for the home feed: current user's posts + followed users' posts.
     */
    fun getPostsForHomeFeed(currentUsername: String): LiveData<List<PostEntity>> {
        return postDao.getPostsForHomeFeed(currentUsername)
    }

    /**
     * Insert a post locally, then also push it to Firestore.
     */
    suspend fun insertPost(post: PostEntity) {
        withContext(Dispatchers.IO) {
            // Insert into local DB
            postDao.insertPost(post)
        }
        // Insert/Update in Firestore
        firebaseModel.addOrUpdatePost(post)
    }

    /**
     * Delete a post locally, then also delete it from Firestore.
     */
    suspend fun deletePost(post: PostEntity) {
        withContext(Dispatchers.IO) {
            // Delete locally
            postDao.deletePost(post)
        }
        // Delete in Firestore
        firebaseModel.deletePostById(post.id)
    }

    /**
     * Pull down all posts from Firestore and replace them in the local DB.
     */
    suspend fun syncPostsFromFirestore() {
        // Get remote posts from Firestore
        val remotePosts = firebaseModel.getAllPosts()

        withContext(Dispatchers.IO) {
            // Insert/replace in the local DB
            postDao.insertPosts(remotePosts)
        }
    }
}
