package com.fitnessapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R

// ChatAdapter manages the display of chat messages using RecyclerView
// Supports do sides: User messages on right side and AI messages on the right side
class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        // View types for RecyclerView
    private val USER = 1
    private val AI = 2

    // Determines whether message at a given position should use the USER or AI Layout
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) USER else AI
    }

    // Creates the correct ViewHOlder depending on the message type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == USER) {
           // Inflate user's chat bubble layout
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            // Inflate AI's chat bubble layout
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_ai, parent, false)
            AiViewHolder(view)
        }
    }

    // Returns total number of messages displayed to RecyclerView
    override fun getItemCount(): Int = messages.size

    // Binds message text to correct view
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]

        if (holder is UserViewHolder) {
            holder.message.text = msg.text
        } else if (holder is AiViewHolder) {
            holder.message.text = msg.text
        }
    }

    // Adds a new chat message to the list and refreshes the RecyclerView
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // Viewholder for USER message
    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.tvUserMessage)
    }

    // View Holder for AI Messages
    class AiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.tvAiMessage)
    }
}
