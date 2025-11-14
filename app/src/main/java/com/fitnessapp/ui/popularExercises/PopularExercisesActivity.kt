package com.fitnessapp.ui.popularExercises

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.entities.PopularExercise
import com.fitnessapp.ui.auth.LoginActivity
import com.fitnessapp.ui.chat.ChatActivity
import com.fitnessapp.ui.main.MainActivity
import com.fitnessapp.ui.map.MapActivity
import com.fitnessapp.ui.recipes.AddRecipesActivity
import com.fitnessapp.ui.recipes.RecipesActivity
import com.fitnessapp.ui.workouts.AddWorkoutActivity
import com.fitnessapp.ui.workouts.WorkoutActivity
import com.fitnessapp.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PopularExercisesActivity : AppCompatActivity() {

    // Declare Session Manager for User Session Handling
    private lateinit var session: SessionManager

    // Declare TextViews for displaying greeting and quote
    private lateinit var tvGreeting: TextView
    private lateinit var tvQuote: TextView

    // Properties for the RecyclerView and its Adapter
    private lateinit var popularExercisesRecyclerView: RecyclerView
    private lateinit var popularExercisesAdapter: PopularExercisesAdapter
    private var exerciseList = mutableListOf<PopularExercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the popular exercises layout for this activity
        setContentView(R.layout.activity_popular_exercises)

        // Initialize the Session Manager
        session = SessionManager(this)

        // Set the text for greeting and motivational quote
        tvGreeting = findViewById(R.id.tvGreeting)
        tvQuote = findViewById(R.id.tvQuote)

        tvGreeting.text = "Popular Exercises"
        tvQuote.text = "Excel in these Exercises with Proper Form"

        // Initialize the RecyclerView by finding its ID
        popularExercisesRecyclerView = findViewById(R.id.recyclerViewExercises)

        // Prepare data displayed in Recycler View
        prepareExerciseData()

        // Initialize adapter for the RecyclerView
        popularExercisesAdapter = PopularExercisesAdapter(lifecycle, exerciseList)

        // Set up the RecyclerView's layout manager and attach the adapter
        popularExercisesRecyclerView.layoutManager = LinearLayoutManager(this)
        popularExercisesRecyclerView.adapter = popularExercisesAdapter

        setupNavigation()
    }

    // Add the Different Exercise names and the Youtube Video URLs
    private fun prepareExerciseData() {
        exerciseList.add(PopularExercise("Jumping Jacks", "CWpmIW6l-YA"))
        exerciseList.add(PopularExercise("Push Ups", "WDIpL0pjun0"))
        exerciseList.add(PopularExercise("Squats", "YaXPRqUwItQ"))
        exerciseList.add(PopularExercise("Plank", "6LqqeBtFn9M"))
        exerciseList.add(PopularExercise("Burpees", "G2hv_NYhM-A"))

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

        view.findViewById<Button>(R.id.btnChat).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, ChatActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnPopularExercises)?.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, PopularExercisesActivity::class.java))
        }

        dialog.show()
    }


}
