package com.fitnessapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R

// Data model class that represents a single favorite item
// Each favorite contains a title and subtitle (workout name and description)
data class FavoriteItem(val title: String, val subtitle: String)

// RecyclerView displays a list of favorite items
// Binds data from a list of FavoriteItem objects to item_favorite layout views
class FavoriteAdapter(private val items: List<FavoriteItem>) :
    RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    // ViewHolder holds references to the views for each item - improving performance by avoiding repeated view lookups
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val subtitle: TextView = view.findViewById(R.id.tvSubtitle)
    }

    // Inflates the layout for each list item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    // Binds data to the ViewHolders views based on the current position in the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.subtitle.text = item.subtitle
    }

    override fun getItemCount(): Int = items.size
}
