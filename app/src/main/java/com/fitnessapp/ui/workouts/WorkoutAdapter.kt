package com.fitnessapp.ui.workouts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.entities.Recipe
import com.fitnessapp.data.entities.Workout

//adapts data from workouts to the recycler view
//takes in a list of Workout objects called 'workouts'
//it inherits from RecyclerView.Adapter and takes in a RecipeViewHolder
class WorkoutAdapter (var workouts: List<Workout>,
    // Handles clicks and passes the clicked Recipe (object)
                      private val onItemClicked: (Workout) -> Unit
): RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    //holds the views of recycler view from rv_workout_row.xml
    //passing in the the row which is a view and it inherits from RecyclerView class
    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutNameTextView: TextView = itemView.findViewById(R.id.tv_workout_name)
        val workoutDurationTextView: TextView = itemView.findViewById(R.id.tv_workout_duration_time)
        val favoriteCheckBox: CheckBox = itemView.findViewById(R.id.favWorkoutBox)
    }

    //called when user scrolls and it has to create a new row item that is visible
    //returns a RecipeViwHolder item (row)
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): WorkoutViewHolder {
        //allows access to the rv_workout_row.xml file
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_workout_row,
            parent,
            false
        )
        return WorkoutViewHolder(view)
    }

    //binds the data from the list of workouts to the views of the row item
    //it takes in the WorkoutViewHolder (row) to access its views and its position in the list
    override fun onBindViewHolder(
        holder: WorkoutViewHolder,
        position: Int
    ) {
        //access the itemview of the row
        //assigns properties of the Workout object to the views of the row
        holder.workoutNameTextView.text = workouts[position].name
        holder.workoutDurationTextView.text = "${workouts[position].durationMinutes} minutes"
        holder.favoriteCheckBox.isChecked = workouts[position].isFavorite
        holder.itemView.setOnClickListener {
            onItemClicked(workouts[position]) //current workout in the list
        }
    }

    //returns the size of the list of workouts in the recyclerview
    override fun getItemCount(): Int {
        return workouts.size
    }

    fun updateWorkouts(newList: List<Workout>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = workouts.size
            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return workouts[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return workouts[oldItemPosition] == newList[newItemPosition]
            }
        })
        workouts = newList
        diffResult.dispatchUpdatesTo(this)
    }
}