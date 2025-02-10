package com.example.hobbyconnect

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hobbyconnect.R
import com.example.hobbyconnect.data.FirebaseModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    // Use your FirebaseModel instance
    private val firebaseModel by lazy { FirebaseModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val emailEditText: EditText = findViewById(R.id.etEmail)
        val usernameEditText: EditText = findViewById(R.id.etUsername)
        val passwordEditText: EditText = findViewById(R.id.etPassword)
        val confirmPasswordEditText: EditText = findViewById(R.id.etConfirmPassword)
        val createAccountButton: Button = findViewById(R.id.btnCreateAccount)
        val loginTextView: TextView = findViewById(R.id.tvLogin)

        createAccountButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            when {
                email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ->
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()

                password != confirmPassword ->
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()

                password.length < 6 ->
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()

                else -> {
                    registerUser(email, username, password)
                }
            }
        }

        // Navigate back to LoginActivity
        loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(email: String, username: String, password: String) {
        // Use coroutines to call suspend functions in FirebaseModel
        lifecycleScope.launch {
            try {
                // 1) Sign up via FirebaseModel
                val newUser: FirebaseUser = firebaseModel.signUpUser(email, password, username)

                // 2) Add user data to Firestore
                //    Typically you'd store the user in "users/{uid}"
                firebaseModel.addOrUpdateUser(
                    newUser.uid,
                    mapOf("username" to username, "email" to email)
                )

                Toast.makeText(
                    this@SignupActivity,
                    "Account created successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                // 3) Navigate to Login
                redirectToLogin()

            } catch (e: Exception) {
                // Handle sign up or Firestore write errors
                val errorMessage = e.localizedMessage ?: "Unknown error"
                Toast.makeText(this@SignupActivity, "Signup failed: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
