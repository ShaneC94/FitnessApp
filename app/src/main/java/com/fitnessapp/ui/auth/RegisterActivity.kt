package com.fitnessapp.ui.auth

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.User
import com.google.android.material.snackbar.Snackbar
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

            val inputUsername = username.text.toString().trim().lowercase()
            val inputName = name.text.toString().trim()
            val inputPassword = password.text.toString()

            if (inputUsername.isEmpty() || inputName.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (inputUsername.length < 4) {
                Toast.makeText(this, "Username must be at least 4 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isStrongPassword(inputPassword)) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Password must be at least 8 characters," +
                            "include at least 1 uppercase, 1 lowercase, " +
                            "1 number, and 1 special character.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val user = User(
                username = inputUsername,
                passwordHash = hash(inputPassword),
                name = inputName
            )

            lifecycleScope.launch {
                try {
                    db.userDao().registerUser(user)
                    Toast.makeText(this@RegisterActivity, "Registered! Please log in.", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    if (e.message?.contains("UNIQUE", ignoreCase = true) == true ||
                        e.message?.contains("unique", ignoreCase = true) == true) {
                        Toast.makeText(this@RegisterActivity, "Username is already taken", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun isStrongPassword(password: String): Boolean {
        val strongPasswordRegex =
            Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$")
        return strongPasswordRegex.matches(password)
    }

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
