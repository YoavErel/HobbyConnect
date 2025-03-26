package com.example.hobbyconnect.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.hobbyconnect.R
import com.example.hobbyconnect.data.AppDatabase
import com.example.hobbyconnect.data.UserEntity
import com.example.hobbyconnect.data.FirebaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var updateUsernameButton: Button
    private lateinit var updateImageButton: Button
    private lateinit var logoutButton: Button
    private lateinit var backArrow: ImageView





    private val firebaseModel = FirebaseModel()
    private var selectedImageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        // Initialize views
        profileImageView = view.findViewById(R.id.profilePicture)
        usernameTextView = view.findViewById(R.id.usernameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        updateUsernameButton = view.findViewById(R.id.updateUsernameButton)
        updateImageButton = view.findViewById(R.id.updateImageButton)
        logoutButton = view.findViewById(R.id.logoutButton)
        backArrow = view.findViewById(R.id.backArrow)

        // Load current user data
        loadUserData()

        // Handle Back Arrow Click
        backArrow.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Update username
        updateUsernameButton.setOnClickListener {
            showUpdateUsernameDialog()
        }

        // Update profile picture
        updateImageButton.setOnClickListener {
            openImagePicker()
        }

        // Logout
        logoutButton.setOnClickListener {
            logout()
        }

        val updateHobbiesButton = view.findViewById<Button>(R.id.updateHobbiesButton)
        updateHobbiesButton.setOnClickListener {
            showUpdateHobbiesDialog()
        }

        return view
    }

    private fun loadUserData() {
        val currentUser = firebaseModel.getCurrentUser()
        if (currentUser != null) {
            usernameTextView.text = currentUser.displayName ?: "Username"
            emailTextView.text = currentUser.email ?: "user@gmail.com"

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(requireContext())
                val userEntity = db.userDao().getUserById(currentUser.uid)
                withContext(Dispatchers.Main) {
                    if (userEntity?.avatarUri != null) {
                        profileImageView.setImageURI(Uri.parse(userEntity.avatarUri))
                    } else {
                        profileImageView.setImageResource(R.drawable.profile_avatar)
                    }
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            profileImageView.setImageURI(selectedImageUri) // Temporarily display the selected image
            saveAvatarLocally(selectedImageUri.toString())
        }
    }

    private fun saveAvatarLocally(uri: String) {
        val currentUser = firebaseModel.getCurrentUser()
        if (currentUser != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val localUri = saveImageToInternalStorage(uri)

                    val db = AppDatabase.getDatabase(requireContext())
                    val userEntity = db.userDao().getUserById(currentUser.uid)
                    val updatedUser = userEntity?.copy(avatarUri = localUri)
                        ?: UserEntity(
                            id = currentUser.uid,
                            username = currentUser.displayName ?: "Unknown",
                            email = currentUser.email ?: "Unknown",
                            avatarUri = localUri
                        )
                    db.userDao().insertUser(updatedUser)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Avatar updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadUserData() // Refresh UI
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to save avatar: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun saveImageToInternalStorage(uri: String): String {
        val inputStream = requireContext().contentResolver.openInputStream(Uri.parse(uri))
        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, fileName)

        inputStream.use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }

        return file.absolutePath // Return the file's absolute path
    }

    private fun showUpdateUsernameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_username, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val usernameInput = dialogView.findViewById<EditText>(R.id.usernameInput)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        saveButton.setOnClickListener {
            val newUsername = usernameInput.text.toString().trim()
            if (newUsername.isNotEmpty()) {
                updateUsername(newUsername)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a valid username", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateUsername(newUsername: String) {
        val currentUser = firebaseModel.getCurrentUser()
        if (currentUser != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(requireContext())
                val userEntity = db.userDao().getUserById(currentUser.uid)
                if (userEntity != null) {
                    val updatedUser = userEntity.copy(username = newUsername)
                    db.userDao().insertUser(updatedUser)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Username updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadUserData()
                    }
                }
            }
        }
    }

    private fun logout() {
        val sharedPreferences = requireContext().getSharedPreferences("HobbyConnectPrefs", AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("REMEMBER_ME", false)
        editor.remove("EMAIL")
        editor.remove("PASSWORD")
        editor.apply()

        firebaseModel.logOut()

        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showUpdateHobbiesDialog() {
        // Step 1: Load the hobby list from strings.xml
        val hobbyList = resources.getStringArray(R.array.hobby_list)

        // Step 2: Create a boolean array for which items are selected
        val selectedHobbies = BooleanArray(hobbyList.size)

        // Step 3: Optionally, fetch the user's current hobbies from the database
        //         so we can pre-select them in the dialog.
        lifecycleScope.launch(Dispatchers.IO) {
            val currentUser = firebaseModel.getCurrentUser()
            var existingHobbies: List<String> = emptyList()
            if (currentUser != null) {
                val db = AppDatabase.getDatabase(requireContext())
                val userEntity = db.userDao().getUserById(currentUser.uid)
                existingHobbies = userEntity?.hobbies?.split(", ") ?: emptyList()
            }

            // Pre-select items if they are in existingHobbies
            for (i in hobbyList.indices) {
                if (existingHobbies.contains(hobbyList[i])) {
                    selectedHobbies[i] = true
                }
            }

            // Switch to Main thread to show the dialog
            withContext(Dispatchers.Main) {
                val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                builder.setTitle("Select Hobbies")

                builder.setMultiChoiceItems(hobbyList, selectedHobbies) { _, which, isChecked ->
                    selectedHobbies[which] = isChecked
                }

                builder.setPositiveButton("OK") { dialog, _ ->
                    // Gather selected hobbies
                    val chosenHobbyList = mutableListOf<String>()
                    for (i in hobbyList.indices) {
                        if (selectedHobbies[i]) {
                            chosenHobbyList.add(hobbyList[i])
                        }
                    }
                    val chosenHobbiesString = chosenHobbyList.joinToString(", ")
                    // Save to DB
                    updateHobbiesInDatabase(chosenHobbiesString)
                    dialog.dismiss()
                }

                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                builder.show()
            }
        }
    }

    private fun updateHobbiesInDatabase(hobbies: String) {
        val currentUser = firebaseModel.getCurrentUser()
        if (currentUser != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(requireContext())
                val userEntity = db.userDao().getUserById(currentUser.uid)

                // Update existing or create new
                val updatedUser = userEntity?.copy(hobbies = hobbies)
                    ?: UserEntity(
                        id = currentUser.uid,
                        username = currentUser.displayName ?: "Unknown",
                        email = currentUser.email ?: "Unknown",
                        avatarUri = null,
                        hobbies = hobbies
                    )

                db.userDao().insertUser(updatedUser)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Hobbies updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }



    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
