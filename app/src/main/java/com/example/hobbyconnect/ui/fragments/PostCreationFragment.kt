package com.example.hobbyconnect.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.hobbyconnect.R
import com.example.hobbyconnect.data.AppDatabase
import com.example.hobbyconnect.data.PostEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PostCreationFragment : Fragment() {

    private lateinit var postImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var postDescriptionEditText: EditText
    private lateinit var hobbySpinner: Spinner
    private lateinit var savePostButton: Button
    private var imageUri: Uri? = null

    private lateinit var database: AppDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = AppDatabase.getDatabase(requireContext())

        // Initialize views
        postImageView = view.findViewById(R.id.postImageView)
        uploadImageButton = view.findViewById(R.id.uploadImageButton)
        postDescriptionEditText = view.findViewById(R.id.postDescriptionEditText)
        hobbySpinner = view.findViewById(R.id.hobbySpinner)
        savePostButton = view.findViewById(R.id.savePostButton)

        // Handle image upload
        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Handle saving the post
        savePostButton.setOnClickListener {
            savePost()
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(postImageView)
        }
    }

    private fun savePost() {
        val description = postDescriptionEditText.text.toString().trim()
        val hobby = hobbySpinner.selectedItem.toString().trim()

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Description cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(requireContext(), "Please upload an image.", Toast.LENGTH_SHORT).show()
            return
        }

        val savedImagePath = saveImageToLocalStorage(imageUri!!) // Save image locally

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val post = PostEntity(
            id = System.currentTimeMillis().toString(), // Generate unique post ID
            userId = currentUser.uid, // Fetch and assign the user's ID
            username = currentUser.displayName ?: "Anonymous", // Assign the username
            title = "Post Title", // Default or custom title
            description = description,
            hobby = hobby,
            imageUrl = savedImagePath, // Store the local image path
            lastUpdated = System.currentTimeMillis()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                database.postDao().insertPost(post) // Insert post into the database
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Post saved successfully!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack() // Navigate back
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error saving post: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageToLocalStorage(uri: Uri): String {
        val file = File(requireContext().filesDir, "${System.currentTimeMillis()}.jpg")
        requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file.absolutePath // Return the saved image's file path
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
