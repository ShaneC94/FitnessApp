package com.fitnessapp.ui.chat

import com.fitnessapp.BuildConfig
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
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

    // Loads our OpenAI API key securely from BuildConfig generated using secret.properties
    private val apiKey = BuildConfig.OPENAI_API_KEY

    // Lazy initialize of our OpenAI Retrofit API Client
    private val api by lazy { OpenAIApi.create(apiKey) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

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
}
