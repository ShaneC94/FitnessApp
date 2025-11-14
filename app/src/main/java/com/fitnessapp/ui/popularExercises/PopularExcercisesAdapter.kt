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
class PopularExercisesAdapter(
    private val lifecycle: Lifecycle,
    private val exerciseList: List<PopularExercise>
) : RecyclerView.Adapter<PopularExercisesAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(itemView: View, lifecycle: Lifecycle) :
        RecyclerView.ViewHolder(itemView) {

        val exerciseNameTextView: TextView = itemView.findViewById(R.id.exerciseName)
        val youtubePlayerView: YouTubePlayerView = itemView.findViewById(R.id.exerciseVideo)

        init {
            lifecycle.addObserver(youtubePlayerView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular_exercise, parent, false)
        return ExerciseViewHolder(view, lifecycle)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val currentExercise = exerciseList[position]

        holder.exerciseNameTextView.text = currentExercise.name

        holder.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.cueVideo(currentExercise.videoUrl, 0f)
            }
        })
    }

    override fun getItemCount(): Int = exerciseList.size




}