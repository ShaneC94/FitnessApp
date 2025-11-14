package com.fitnessapp.ui.chat

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

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var adapter: ChatAdapter

    private val apiKey = "API_KEY"
    private val api by lazy { OpenAIApi.create(apiKey) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        adapter = ChatAdapter(mutableListOf())
        rvChat.adapter = adapter
        rvChat.layoutManager = LinearLayoutManager(this)

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etMessage.text.clear()
            }
        }
    }

    private fun sendMessage(text: String) {
        // Add user message
        adapter.addMessage(ChatMessage(text, true))
        rvChat.scrollToPosition(adapter.itemCount - 1)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getChatCompletion(
                    OpenAIRequest(
                        messages = listOf(
                            Message("user", text)
                        )
                    )
                )

                val aiText = response.choices.first().message.content

                withContext(Dispatchers.Main) {
                    adapter.addMessage(ChatMessage(aiText, false))
                    rvChat.scrollToPosition(adapter.itemCount - 1)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    adapter.addMessage(ChatMessage("Error: ${e.message}", false))
                }
            }
        }
    }
}
