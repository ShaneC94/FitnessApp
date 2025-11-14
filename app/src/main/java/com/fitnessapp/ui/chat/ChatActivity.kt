package com.fitnessapp.ui.chat

// Android + Kotlin imports
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Material components
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

// App utilities
import com.fitnessapp.BuildConfig
import com.fitnessapp.R
import com.fitnessapp.utils.SessionManager

// Navigation targets (Activities)
import com.fitnessapp.ui.auth.LoginActivity
import com.fitnessapp.ui.main.MainActivity
import com.fitnessapp.ui.map.MapActivity
import com.fitnessapp.ui.recipes.AddRecipesActivity
import com.fitnessapp.ui.recipes.RecipesActivity
import com.fitnessapp.ui.workouts.AddWorkoutActivity
import com.fitnessapp.ui.workouts.WorkoutActivity

// Coroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class ChatActivity : AppCompatActivity() {

    // UI elements for chat screen
    private lateinit var rvChat: RecyclerView // Chat message list
    private lateinit var etMessage: EditText // Input box
    private lateinit var btnSend: ImageButton // Send button
    private lateinit var adapter: ChatAdapter // Recycler View Adapter

    private lateinit var session: SessionManager  // <--- ADD THIS HERE

    // Loads our OpenAI API key securely from BuildConfig generated using secret.properties
    private val apiKey = BuildConfig.OPENAI_API_KEY

    // Lazy initialize of our OpenAI Retrofit API Client
    private val api by lazy { OpenAIApi.create(apiKey) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        session = SessionManager(this)   // <--- ADD THIS
        setupNavigation()                // <--- ADD THIS


        // Initialize UI elements
        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        // Set up the RecyclerView with an empty chat list
        adapter = ChatAdapter(mutableListOf())
        rvChat.adapter = adapter
        rvChat.layoutManager = LinearLayoutManager(this)

        // Actions when send button is clicked
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etMessage.text.clear()
            }
        }
    }


    // Runs in the background thread using coroutines to send a message to the OpenAI API
    private fun sendMessage(text: String) {
        // Add user message
        adapter.addMessage(ChatMessage(text, true))
        rvChat.scrollToPosition(adapter.itemCount - 1)

        // Run API call in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Build the request for OpenAI
                val response = api.getChatCompletion(
                    OpenAIRequest(
                        messages = listOf(
                            Message("user", text)
                        )
                    )
                )

                // Extract AI message from the response
                val aiText = response.choices.first().message.content

                // Switch back to Main thread to update UI
                withContext(Dispatchers.Main) {
                    adapter.addMessage(ChatMessage(aiText, false))
                    rvChat.scrollToPosition(adapter.itemCount - 1)
                }
            } catch (e: Exception) {
                // Handles errors such as 401, timeout, no internet
                withContext(Dispatchers.Main) {
                    adapter.addMessage(ChatMessage("Error: ${e.message}", false))
                }
            }
        }

    }
    // Method to setup Navigation (buttons and actions)
    private fun setupNavigation() {
        // Logout button (top right)
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Floating action button (+)
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddPopup()
        }

        // Recipes button
        findViewById<Button>(R.id.btnRecipes).setOnClickListener {
            startActivity(Intent(this, RecipesActivity::class.java))
        }

        // Workouts button
        findViewById<Button>(R.id.btnWorkouts).setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }
    }

    // ===== ADD BUTTON POPUP =====
    private fun showAddPopup() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.popup_add_options, null)
        dialog.setContentView(view)

        // Make ALL buttons visible
        val buttonIds = listOf(
            R.id.btnAddWorkout,
            R.id.btnAddRecipe,
            R.id.btnLogProgress,
            R.id.btnCamera,
            R.id.btnMap,
            R.id.btnMain,
            R.id.btnChat,
            R.id.btnPopularExercises
        )

        buttonIds.forEach { id ->
            view.findViewById<Button>(id).visibility = View.VISIBLE
        }

        // === Button Click Handlers ===
        view.findViewById<Button>(R.id.btnAddWorkout).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AddWorkoutActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnAddRecipe).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AddRecipesActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnLogProgress).setOnClickListener {
            dialog.dismiss()
            // startActivity(Intent(this, LogProgressActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnCamera).setOnClickListener {
            dialog.dismiss()
            // startActivity(Intent(this, CameraIntegration::class.java))
        }

        view.findViewById<Button>(R.id.btnMap).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, MapActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnMain).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, MainActivity::class.java))
        }
        view.findViewById<Button>(R.id.btnPopularExercises)?.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, com.fitnessapp.ui.popularExercises.PopularExercisesActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnChat).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, com.fitnessapp.ui.chat.ChatActivity::class.java))
        }


        dialog.show()
    }


}



