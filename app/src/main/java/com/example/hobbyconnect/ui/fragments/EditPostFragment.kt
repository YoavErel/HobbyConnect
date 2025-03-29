package com.example.hobbyconnect.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.hobbyconnect.R
import com.example.hobbyconnect.data.AppDatabase
import com.example.hobbyconnect.data.PostEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EditPostFragment : Fragment() {

    private lateinit var postImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var hobbySpinner: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private var selectedImageUri: Uri? = null
    private var postId: String? = null
    private var originalImageUrl: String? = null
    private var userId: String? = null // Store userId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_post, container, false)

        // Initialize Views
        postImageView = view.findViewById(R.id.postImageView)
        uploadImageButton = view.findViewById(R.id.uploadImageButton)
        hobbySpinner = view.findViewById(R.id.hobbySpinner)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        // Initialize Spinner with hobby_list
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.hobby_list,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hobbySpinner.adapter = adapter

        // Get post details from arguments
        arguments?.let {
            postId = it.getString("postId")
            userId = it.getString("userId")
            val postDescription = it.getString("description")
            val postHobby = it.getString("hobby")
            originalImageUrl = it.getString("imageUrl")
            val postUsername = it.getString("username")

            // Load data into views
            loadPostData(postDescription, postHobby)
        }


        // Handle image upload
        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Handle Save button click
        saveButton.setOnClickListener {
            savePost()
        }

        // Handle Cancel button click
        cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun loadPostData(description: String?, hobby: String?) {
        descriptionEditText.setText(description)

        // Select hobby in spinner
        val hobbyIndex = (hobbySpinner.adapter as ArrayAdapter<String>).getPosition(hobby)
        if (hobbyIndex >= 0) {
            hobbySpinner.setSelection(hobbyIndex)
        }

        // Load image using Glide
        if (!originalImageUrl.isNullOrEmpty()) {
            Glide.with(this).load(originalImageUrl).into(postImageView)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            postImageView.setImageURI(selectedImageUri) // Update the image view
        }
    }

    private fun saveImageToInternalStorage(selectedImageUri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri)
            val file = File(requireContext().filesDir, "${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath // Return the file path
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun savePost() {
        val updatedDescription = descriptionEditText.text.toString().trim()
        val updatedHobby = hobbySpinner.selectedItem.toString()
        val updatedImageUrl = if (selectedImageUri != null) {
            saveImageToInternalStorage(selectedImageUri!!)
        } else {
            originalImageUrl
        }

        if (updatedDescription.isEmpty()) {
            Toast.makeText(requireContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (postId == null) {
            Toast.makeText(requireContext(), "Invalid post ID", Toast.LENGTH_SHORT).show()
            return
        }

        if (updatedImageUrl == null) {
            Toast.makeText(requireContext(), "Failed to save image!", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null) {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Create updated PostEntity
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val updatedPost = PostEntity(
                    id = postId!!,
                    userId = userId!!,
                    username = arguments?.getString("username") ?: "Unknown",
                    title = arguments?.getString("title") ?: "Untitled",
                    description = updatedDescription,
                    hobby = updatedHobby,
                    imageUrl = updatedImageUrl,
                    lastUpdated = System.currentTimeMillis()
                )
                db.postDao().updatePost(updatedPost)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Post updated successfully", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to update post: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
