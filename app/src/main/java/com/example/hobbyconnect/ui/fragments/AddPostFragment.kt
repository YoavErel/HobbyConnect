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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hobbyconnect.R
import com.example.hobbyconnect.data.AppDatabase
import com.example.hobbyconnect.data.FirebaseModel
import com.example.hobbyconnect.data.PostEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AddPostFragment : Fragment() {

    private lateinit var postImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var postDescriptionEditText: EditText
    private lateinit var hobbySpinner: Spinner
    private lateinit var savePostButton: Button
    private val firebaseModel = FirebaseModel()

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        // Initialize UI elements
        postImageView = view.findViewById(R.id.postImageView)
        uploadImageButton = view.findViewById(R.id.uploadImageButton)
        postDescriptionEditText = view.findViewById(R.id.postDescriptionEditText)
        hobbySpinner = view.findViewById(R.id.hobbySpinner)
        savePostButton = view.findViewById(R.id.savePostButton)

        // Handle "Upload Image" button click
        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        // Handle save button click
        savePostButton.setOnClickListener {
            savePost()
        }

        return view
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Suppress("DEPRECATION") // For demonstration (use Activity Result API in modern code)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST &&
            resultCode == AppCompatActivity.RESULT_OK &&
            data?.data != null
        ) {
            selectedImageUri = data.data
            Glide.with(requireContext())
                .load(selectedImageUri)
                .placeholder(R.color.gray)
                .into(postImageView)
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
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun savePost() {
        val description = postDescriptionEditText.text.toString().trim()
        val hobby = hobbySpinner.selectedItem.toString()

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Description cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select an image!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = firebaseModel.getCurrentUser()
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val username = currentUser.displayName ?: "DefaultUsername"
        val userId = currentUser.uid
        val imagePath = saveImageToInternalStorage(selectedImageUri!!)

        if (imagePath == null) {
            Toast.makeText(requireContext(), "Failed to save image!", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the post object
        val post = PostEntity(
            id = System.currentTimeMillis().toString(),
            userId = userId,
            username = username,
            title = "Default Title",
            description = description,
            hobby = hobby,
            imageUrl = imagePath,
            lastUpdated = System.currentTimeMillis()
        )

        // Insert into Room database
        insertPost(post)
    }

    private fun insertPost(post: PostEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                db.postDao().insertPost(post)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Post saved successfully!", Toast.LENGTH_SHORT).show()
                    view?.let { Navigation.findNavController(it).navigate(R.id.action_addPostFragment_to_homeFragment) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error saving post: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
