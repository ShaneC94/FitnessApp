package com.fitnessapp.ui.main

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
import com.fitnessapp.ui.auth.LoginActivity
import com.fitnessapp.ui.map.MapActivity
import com.fitnessapp.ui.popularExercises.PopularExercisesActivity
import com.fitnessapp.ui.recipes.AddRecipesActivity
import com.fitnessapp.ui.recipes.RecipesActivity
import com.fitnessapp.ui.workouts.AddWorkoutActivity
import com.fitnessapp.ui.workouts.WorkoutActivity
import com.fitnessapp.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Dependencies
    private lateinit var session: SessionManager
    private lateinit var db: AppDatabase

    // UI Components
    private lateinit var tvGreeting: TextView
    private lateinit var tvQuote: TextView
    private lateinit var rvFavorites: RecyclerView

    // Motivational quotes loaded from strings.xml for easy localization
    private lateinit var quotes: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        session = SessionManager(this)
        db = AppDatabase.getInstance(this)

        // Redirect if user not logged in
        val userId = session.getUserId() ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Init UI components
        tvGreeting = findViewById(R.id.tvGreeting)
        tvQuote = findViewById(R.id.tvQuote)
        rvFavorites = findViewById(R.id.rvFavorites)

        // Load quotes and display a random one
        quotes = resources.getStringArray(R.array.quotes)
        tvQuote.text = quotes.randomOrNull() ?: ""

        // Personalized greeting
        lifecycleScope.launch {
            val user = db.userDao().getUserById(userId)
            if (user != null) {
                tvGreeting.text = "Hello, ${user.username}!"
            } else {
                tvGreeting.text = "Hello!"
            }
        }

        // Random motivational quote
        tvQuote.text = quotes.random()

        setupRecycler()
        setupNavigation()
    }

    // Inits RecyclerView with static sample data - should be replaced with dynamic content from DB or API
    private fun setupRecycler() {
        rvFavorites.layoutManager = LinearLayoutManager(this)
        rvFavorites.adapter = FavoriteAdapter(
            listOf(
                FavoriteItem("Push Day", "60 min • 7 exercises"),
                FavoriteItem("Pull Day", "60 min • 6 exercises"),
                FavoriteItem("Leg Day", "75 min • 5 exercises"),
                FavoriteItem("Cardio + Core", "30 min •     HIIT")
            )
        )
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
            R.id.btnPopularExercises,
            R.id.btnAddWorkout,
            R.id.btnAddRecipe,
            R.id.btnLogProgress,
            R.id.btnCamera,
            R.id.btnMap
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

        view.findViewById<Button>(R.id.btnPopularExercises).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, PopularExercisesActivity::class.java))
        }

        dialog.show()
    }

}



