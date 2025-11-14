package com.fitnessapp.ui.popularExercises

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.entities.PopularExercise

// The Adapter
// The Adapter

// Adapter for populating the RecyclerView with exercise data and Youtube video players
class PopularExercisesAdapter(
    private val lifecycle: Lifecycle,
    private val exerciseList: List<PopularExercise>
) : RecyclerView.Adapter<PopularExercisesAdapter.ExerciseViewHolder>() {

    // ViewHolder class to hold the views for each item in the RecyclerView
    class ExerciseViewHolder(itemView: View, lifecycle: Lifecycle) :
        RecyclerView.ViewHolder(itemView) {

        // Views to display Text and the embedded youtube video
        val exerciseNameTextView: TextView = itemView.findViewById(R.id.exerciseName)
        val youtubePlayerView: YouTubePlayerView = itemView.findViewById(R.id.exerciseVideo)

        init {
            lifecycle.addObserver(youtubePlayerView)
        }
    }

    // Inflates the layout for each item in the RecyclerView and returns a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular_exercise, parent, false)
        return ExerciseViewHolder(view, lifecycle)
    }

    // Binds Exercise name and youtube video to their corresponding views
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val currentExercise = exerciseList[position]

        holder.exerciseNameTextView.text = currentExercise.name

        // Add a listener to the YoutubePlayerView to load the video
        holder.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.cueVideo(currentExercise.videoUrl, 0f)
            }
        })
    }

    //Returns number of items (exercises) in the list
    override fun getItemCount(): Int = exerciseList.size




}