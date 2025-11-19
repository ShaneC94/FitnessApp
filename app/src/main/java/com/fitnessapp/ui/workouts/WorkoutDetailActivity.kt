package com.fitnessapp.ui.workouts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.Exercise
import com.fitnessapp.data.entities.Workout
import com.fitnessapp.data.repositories.WorkoutRepository
import com.fitnessapp.ui.auth.LoginActivity
import com.fitnessapp.ui.chat.ChatActivity
import com.fitnessapp.ui.main.MainActivity
import com.fitnessapp.ui.map.MapActivity
import com.fitnessapp.ui.popularExercises.PopularExercisesActivity
import com.fitnessapp.ui.recipes.AddRecipesActivity
import com.fitnessapp.ui.recipes.RecipesActivity
import com.fitnessapp.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch


class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var session: SessionManager
    private var currentWorkoutId: Int = -1

    // Views from activity_add_workout.xml
    private lateinit var etWorkoutName: EditText
    private lateinit var etWorkoutDuration: EditText
    private lateinit var llExercisesContainer: LinearLayout
    private lateinit var btnAddExercise: Button
    private lateinit var btnSaveOrUpdate: Button
    private lateinit var btnClose: Button
    private lateinit var tvGreeting: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reuse the layout from the "Add Workout" screen
        setContentView(R.layout.activity_add_workout)

        session = SessionManager(this)
        db = AppDatabase.getInstance(this)
        workoutRepository = WorkoutRepository(db.workoutDao())
        tvGreeting = findViewById(R.id.tvGreeting)

        // Initialize Views
        etWorkoutName = findViewById(R.id.et_workout_name)
        etWorkoutDuration = findViewById(R.id.et_duration)
        llExercisesContainer = findViewById(R.id.ll_exercises_container)
        btnAddExercise = findViewById(R.id.btn_add_exercise)
        btnSaveOrUpdate = findViewById(R.id.btn_save_workout)
        btnClose = findViewById(R.id.btn_close_workout)

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

        btnSaveOrUpdate.text = "Update Workout"

        // Get the workout ID passed from WorkoutActivity
        currentWorkoutId = intent.getIntExtra("WORKOUT_ID", -1)

        if (currentWorkoutId == -1) {
            Toast.makeText(this, "Error: Workout not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadWorkoutData()
        setupClickListeners()
    }

    private fun loadWorkoutData() {
        lifecycleScope.launch {
            // Get the workout using the existing getWorkoutById function
            val workout = db.workoutDao().getWorkoutById(currentWorkoutId)
            if (workout == null) {
                Toast.makeText(this@WorkoutDetailActivity, "Error loading workout.", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            etWorkoutName.setText(workout.name)
            etWorkoutDuration.setText(workout.durationMinutes.toString())


            //Get the exercises using the existing getExercisesForWorkoutSuspend function
            val exercises = db.exerciseDao().getExercisesForWorkoutSuspend(currentWorkoutId)

            // Clear any default views and populate the exercises
            llExercisesContainer.removeAllViews()
            exercises.forEach { exercise ->
                addExerciseView(exercise) // Pass existing exercise data
            }
        }
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener {
            finish()
        }

        btnAddExercise.setOnClickListener {
            addExerciseView(null) // Add a new, blank exercise row
        }

        btnSaveOrUpdate.setOnClickListener {
            updateWorkout()
        }

        setupNavigation()
    }


    private fun addExerciseView(exercise: Exercise?) {
        val inflater = LayoutInflater.from(this)

        val exerciseView = inflater.inflate(R.layout.exercise_row_item, llExercisesContainer, false)

        val etExerciseName = exerciseView.findViewById<EditText>(R.id.et_exercise_name)
        val etSets = exerciseView.findViewById<EditText>(R.id.et_sets)
        val etReps = exerciseView.findViewById<EditText>(R.id.et_reps)
        val etWeight = exerciseView.findViewById<EditText>(R.id.et_weight)
        val etNotes = exerciseView.findViewById<EditText>(R.id.et_notes)

        // If exercise data exists, fill the fields
        exercise?.let {
            etExerciseName.setText(it.name)
            etSets.setText(it.sets.toString())
            etReps.setText(it.reps.toString())
            etWeight.setText(it.weight.toString())
            etNotes.setText(it.notes)
        }

        val btnDeleteExercise = exerciseView.findViewById<ImageButton>(R.id.btn_delete_exercise)
        btnDeleteExercise.setOnClickListener {
            llExercisesContainer.removeView(exerciseView)
        }
        llExercisesContainer.addView(exerciseView)
    }

    private fun updateWorkout() {
        val workoutName = etWorkoutName.text.toString().trim()
        val duration = findViewById<EditText>(R.id.et_duration).text.toString().toIntOrNull() ?: 0

        if (workoutName.isBlank()) {
            Toast.makeText(this, "Workout name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a list of the current exercises from the UI
        val updatedExercises = mutableListOf<Exercise>()
        for (i in 0 until llExercisesContainer.childCount) {
            val view = llExercisesContainer.getChildAt(i)
            val etExerciseName = view.findViewById<EditText>(R.id.et_exercise_name)
            val etSets = view.findViewById<EditText>(R.id.et_sets)
            val etReps = view.findViewById<EditText>(R.id.et_reps)
            val etWeight = view.findViewById<EditText>(R.id.et_weight)
            val etNotes = view.findViewById<EditText>(R.id.et_notes)

            val exerciseName = etExerciseName.text.toString().trim()
            val sets = etSets.text.toString().toIntOrNull()
            val reps = etReps.text.toString().toIntOrNull()
            val weight = etWeight.text.toString().toFloatOrNull()
            val notes = etNotes.text.toString().trim()

            if (exerciseName.isBlank() || sets == null || reps == null || weight == null) {
                Toast.makeText(this, "Please fill all fields for each exercise.", Toast.LENGTH_SHORT).show()
                return
            }
            updatedExercises.add(Exercise(workoutId = currentWorkoutId, name = exerciseName, sets = sets, reps = reps, weight = weight, notes = notes))
        }

        lifecycleScope.launch {
            // Update the workout name using the existing updateWorkout function
            val updatedWorkout = Workout(id = currentWorkoutId, name = workoutName, isFavorite = false, durationMinutes = duration)
            workoutRepository.update(updatedWorkout)

            // Clear the old exercises using the existing deleteExercisesByWorkoutID function
            db.workoutDao().deleteExercisesByWorkoutID(currentWorkoutId)

            //Insert the new, updated list of exercises
            workoutRepository.insertExercises(updatedExercises)

            Toast.makeText(this@WorkoutDetailActivity, "Workout Updated!", Toast.LENGTH_SHORT).show()
            finish()
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
            startActivity(Intent(this, ChatActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnPopularExercises)?.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, PopularExercisesActivity::class.java))
        }

        dialog.show()
    }
}
