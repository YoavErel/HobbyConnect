package com.example.hobbyconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hobbyconnect.R
import com.example.hobbyconnect.data.FirebaseModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import android.widget.CheckBox
import com.example.hobbyconnect.ui.MainActivity

class LoginActivity : AppCompatActivity() {

    private val firebaseModel by lazy { FirebaseModel() }
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rememberMeCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("HobbyConnectPrefs", MODE_PRIVATE)

        val emailEditText: EditText = findViewById(R.id.etEmail)
        val passwordEditText: EditText = findViewById(R.id.etPassword)
        val signInButton: Button = findViewById(R.id.btnSignIn)
        val createAccountTextView: TextView = findViewById(R.id.tvCreateAccount)
        val forgotPasswordTextView: TextView = findViewById(R.id.tvForgotPassword)
        rememberMeCheckBox = findViewById(R.id.checkboxRememberMe)

        // Check if "Remember Me" was selected previously
        val isRemembered = sharedPreferences.getBoolean("REMEMBER_ME", false)
        if (isRemembered) {
            val savedEmail = sharedPreferences.getString("EMAIL", "")
            val savedPassword = sharedPreferences.getString("PASSWORD", "")
            if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                // Automatically log in the user
                loginUser(savedEmail, savedPassword)
            }
        }

        // Handle login
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                val rememberMe = rememberMeCheckBox.isChecked
                if (rememberMe) {
                    saveLoginInfo(email, password)
                } else {
                    clearLoginInfo()
                }
                loginUser(email, password)
            }
        }

        // Navigate to SignupActivity
        createAccountTextView.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Handle forgot password
        forgotPasswordTextView.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                resetPassword(email)
            }
        }
    }

    private fun saveLoginInfo(email: String, password: String) {
        // Save login info and "Remember Me" flag in SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putBoolean("REMEMBER_ME", true)
        editor.putString("EMAIL", email)
        editor.putString("PASSWORD", password)
        editor.apply()
    }

    private fun clearLoginInfo() {
        // Clear saved login info
        val editor = sharedPreferences.edit()
        editor.putBoolean("REMEMBER_ME", false)
        editor.remove("EMAIL")
        editor.remove("PASSWORD")
        editor.apply()
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val user: FirebaseUser? = firebaseModel.logIn(email, password)
                if (user != null) {
                    checkUserProfile(user.uid)
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed: User is null", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Login failed: ${e.message}")
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkUserProfile(userId: String) {
        lifecycleScope.launch {
            try {
                val userData = firebaseModel.getUserData(userId)
                if (userData != null && userData.isNotEmpty()) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "User profile not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error fetching profile: ${e.message}")
                Toast.makeText(this@LoginActivity, "Failed to fetch profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetPassword(email: String) {
        com.google.firebase.auth.FirebaseAuth.getInstance()
            .sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset email sent!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
