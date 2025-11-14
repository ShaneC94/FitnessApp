package com.fitnessapp.ui.workouts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase

import com.fitnessapp.data.entities.Workout

import com.fitnessapp.data.repositories.WorkoutRepository
import com.fitnessapp.ui.auth.LoginActivity
import com.fitnessapp.ui.main.MainActivity
import com.fitnessapp.ui.map.MapActivity
import com.fitnessapp.ui.recipes.AddRecipesActivity
import com.fitnessapp.ui.recipes.RecipesActivity
import com.fitnessapp.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.fitnessapp.ui.workouts.WorkoutAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog


class WorkoutActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var rvWorkouts: RecyclerView
    private lateinit var repository: WorkoutRepository
    private lateinit var tvGreeting: TextView
    private lateinit var adapter: WorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        session = SessionManager(this)
        db = AppDatabase.getInstance(this)
        repository = WorkoutRepository(db.workoutDao())

        tvGreeting = findViewById(R.id.tvGreeting)

        rvWorkouts = findViewById(R.id.rvWorkouts)
        rvWorkouts.layoutManager = LinearLayoutManager(this)

        //lambda function passed in handles clicks on the workout
        adapter = WorkoutAdapter(
            workouts = emptyList(),
            onItemClicked = { workout ->
                onWorkoutClicked(workout)
        },
            onFavoriteToggled = { workout, isChecked ->
                toggleFavorite(workout, isChecked)
            }
        )

        rvWorkouts.adapter = adapter

        // Personalized greeting
        lifecycleScope.launch {
            val userId = session.getUserId()
            val user = userId?.let { db.userDao().getUserById(it) }
            if (user != null) {
                tvGreeting.text = "Pump iron, ${user.username}!"
            } else {
                tvGreeting.text = "Pump iron!"
            }
        }

        loadWorkouts()
        setupNavigation()
    }

    private fun toggleFavorite(workout: Workout, isChecked: Boolean) {
        lifecycleScope.launch {
            val updatedWorkout = workout.copy(isFavorite = isChecked)
            db.workoutDao().updateWorkout(updatedWorkout)
        }
    }

    private fun onWorkoutClicked(workout: Workout) {
        // intent to navigate to your new details activity
        val intent = Intent(this, WorkoutDetailActivity::class.java)

        // Pass the unique ID of the clicked recipe to the next activity
        intent.putExtra("WORKOUT_ID", workout.id)

        startActivity(intent)
    }

    private fun loadWorkouts() {
        lifecycleScope.launch {
            repository.allWorkouts.collectLatest { workoutList ->
                adapter.updateWorkouts(workoutList)
            }
        }
    }

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
        view.findViewById<Button>(R.id.btnChat).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, com.fitnessapp.ui.chat.ChatActivity::class.java))
        }
        view.findViewById<Button>(R.id.btnPopularExercises)?.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, com.fitnessapp.ui.popularExercises.PopularExercisesActivity::class.java))
        }

        dialog.show()
    }
}


