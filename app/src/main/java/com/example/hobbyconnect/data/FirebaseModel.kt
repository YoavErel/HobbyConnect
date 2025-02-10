package com.example.hobbyconnect.data

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

class FirebaseModel {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // -------------------
    // AUTHENTICATION
    // -------------------

    /**
     * Register a new user with optional display name.
     * Example usage:
     *   val newUser = signUpUser("test@test.com", "123456", "John Doe")
     */
    suspend fun signUpUser(
        email: String,
        password: String,
        displayName: String? = null
    ): FirebaseUser {
        val authResult: AuthResult = auth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: error("User registration failed")
        if (!displayName.isNullOrEmpty()) {
            // Update display name if provided
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()
        }
        return user
    }

    /**
     * Log in an existing user with Firebase Authentication.
     */
    suspend fun logIn(email: String, password: String): FirebaseUser {
        val authResult: AuthResult = auth.signInWithEmailAndPassword(email, password).await()
        return authResult.user ?: error("Login failed")
    }

    /**
     * Check if a user is currently logged in.
     */
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get the currently logged-in user.
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Log out the current user and clear any lingering session data.
     */
    fun logOut() {
        try {
            // Sign out from Firebase Auth
            auth.signOut()

            // Clear cached user data in Firestore or locally, if any
            clearCachedUserData()

            // Additional cleanup if needed (e.g., Firebase Realtime Database references)
            clearDatabaseReferences()

            // Notify the app that the user has been logged out
            println("User successfully logged out.")
        } catch (e: Exception) {
            throw Exception("Logout failed: ${e.message}")
        }
    }

    /**
     * Example function to clear cached user data (if applicable).
     */
    private fun clearCachedUserData() {
        // Clear any user-related data stored locally or in-memory
        // Example: SharedPreferences, local cache, etc.
        println("Cached user data cleared.")
    }

    /**
     * Example function to clear Firebase database references (if applicable).
     */
    private fun clearDatabaseReferences() {
        // If you're using Firebase Realtime Database or other services,
        // ensure any references to the user data are also cleaned up.
        println("Database references cleared.")
    }





    // -------------------
    // FIRESTORE POSTS
    // -------------------
    // Make sure you have a "posts" collection in your Firestore
    // and your PostEntity is structured to match.

    /**
     * Create or update a PostEntity in Firestore.
     */
    suspend fun addOrUpdatePost(post: PostEntity) {
        db.collection("posts")
            .document(post.id)
            .set(post)
            .await()
    }

    /**
     * Fetch all posts from Firestore.
     */
    suspend fun getAllPosts(): List<PostEntity> {
        val snapshot = db.collection("posts").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(PostEntity::class.java)
        }
    }



    /**
     * Delete a post from Firestore by its ID.
     */
    suspend fun deletePostById(postId: String) {
        db.collection("posts")
            .document(postId)
            .delete()
            .await()
    }

    // -------------------
    // IMAGE UPLOAD (Firebase Storage)
    // -------------------
    // If you need to upload images like in the Java example.



    // -------------------
    // USER PROFILE (Firestore)
    // -------------------
    // If you want to store additional user info in Firestore,
    // create a UserEntity data class and do something like:

    /**
     * Example: Write user data to Firestore (by userId or displayName).
     * Suppose you have a "users" collection in Firestore.
     */
    suspend fun addOrUpdateUser(userId: String, userData: Map<String, Any>) {
        db.collection("users")
            .document(userId)
            .set(userData)
            .await()
    }

    /**
     * Example: Get user data from Firestore.
     */
    suspend fun getUserData(userId: String): Map<String, Any>? {
        val docSnap = db.collection("users").document(userId).get().await()
        return docSnap.data // or docSnap.toObject(YourUserClass::class.java)
    }



    suspend fun deleteAccount(userId: String) {
        try {
            // Delete user's data from Firestore
            db.collection("users").document(userId).delete().await()

            // Get the currently authenticated user
            val currentUser = auth.currentUser ?: throw IllegalStateException("No user is currently logged in")

            // Delete the user's account from Firebase Authentication
            currentUser.delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to delete account: ${e.message}")
        }
    }

    /**
     * Update the displayName of the current user in Firebase Authentication.
     */
    suspend fun updateFirebaseDisplayName(newDisplayName: String) {
        val currentUser = auth.currentUser ?: throw IllegalStateException("No user is currently logged in")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newDisplayName)
            .build()
        currentUser.updateProfile(profileUpdates).await()
    }

}
