package com.fitnessapp.ui.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.entities.Location

class LocationAdapter(
    private var gyms: List<Pair<Location, Double>> = emptyList(),
    private val onClick: (Location) -> Unit
) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    // Track selected item
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.cardView)
        val name: TextView = view.findViewById(R.id.gymName)
        val distance: TextView = view.findViewById(R.id.gymDistance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gym_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = gyms.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (gym, distance) = gyms[position]
        holder.name.text = gym.name
        holder.distance.text = String.format("%.2f km away", distance)

        val context = holder.itemView.context

        // Highlight if selected
        if (position == selectedPosition) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            holder.name.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            holder.distance.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        } else {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary))
            holder.distance.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary))
        }

        // Handle clicks safely using bindingAdapterPosition
        holder.card.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onClick(gyms[adapterPosition].first)
            }
        }
    }

    fun updateGyms(list: List<Pair<Location, Double>>) {
        gyms = list
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }
}
