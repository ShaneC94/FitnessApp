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

class LoginActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)

        // Skip login if already logged in
        sessionManager.getUserId()?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val registerBtn = findViewById<Button>(R.id.btnRegister)

        loginBtn.setOnClickListener {
            lifecycleScope.launch {
                val inputUsername = username.text.toString().trim()
                val inputPassword = password.text.toString()

                if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val user = db.userDao().getUserByUsername(inputUsername)
                val hashedInput = hashPassword(inputPassword)

                if (user != null && user.passwordHash == hashedInput) {
                    sessionManager.saveUserSession(user.id)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Hashes a password using SHA-256 before comparison or storage
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
