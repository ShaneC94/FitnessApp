package com.fitnessapp.ui.auth

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.User
import kotlinx.coroutines.launch
import java.security.MessageDigest

// Handles user registration
// Collects users name, username, password, and hashes the password
class RegisterActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = AppDatabase.getInstance(this)

        val username = findViewById<EditText>(R.id.etUsername)
        val name = findViewById<EditText>(R.id.etName)
        val password = findViewById<EditText>(R.id.etPassword)
        val registerBtn = findViewById<Button>(R.id.btnRegister)

        registerBtn.setOnClickListener {
            val user = User(
                username = username.text.toString(),
                passwordHash = hash(password.text.toString()), // Securely hashes password
                name = name.text.toString()
            )

            // Launch a coroutine for DB ops (Room can't run on the main thread)
            lifecycleScope.launch {
                try {
                    db.userDao().registerUser(user)
                    Toast.makeText(this@RegisterActivity, "Registered! Please log in.", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Username already exists!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Hashes the password using SHA-256 for secure storage (not in plain text)
    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        // Convert bytes to a lowercase hexadecimal string
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
