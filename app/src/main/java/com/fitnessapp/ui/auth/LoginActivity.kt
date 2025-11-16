package com.fitnessapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.ui.main.MainActivity
import com.fitnessapp.utils.SessionManager
import kotlinx.coroutines.launch
import java.security.MessageDigest

// Handles user auth logic
// Uses Room for DB access and SessionManager to persist login state
class LoginActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        val storedId = sessionManager.getUserId()
        if (storedId != null && storedId > 0) {
            lifecycleScope.launch {
                val user = db.userDao().getUserById(storedId)

                if (user != null) {
                    // User exists -> safe to skip login
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    // User was deleted because DB reset after schema change
                    sessionManager.clearSession()
                }
            }
        }

        if (storedId != null && storedId < 0) {
            sessionManager.clearSession()
        }

        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val registerBtn = findViewById<Button>(R.id.btnRegister)

        loginBtn.setOnClickListener {
                val inputUsername = username.text.toString().trim()
                val inputPassword = password.text.toString()

            lifecycleScope.launch {
                // Ensure both fields are filled
                if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Check if username exists and hash password before comparing with stored hash
                val user = db.userDao().getUserByUsername(inputUsername)
                val hashedInput = hashPassword(inputPassword)

                if (user != null && user.passwordHash == hashedInput) {
                    sessionManager.saveUserSession(user.id)
                    Toast.makeText(this@LoginActivity, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Redirect user to registration screen if they don't have an account
        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Hashes a password using SHA-256 before comparison or storage
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
       // Convert bytes to a lowercase hexadecimal string
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
