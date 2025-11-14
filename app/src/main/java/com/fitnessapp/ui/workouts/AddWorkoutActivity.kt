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
import com.fitnessapp.ui.main.MainActivity
import com.fitnessapp.ui.map.MapActivity
import com.fitnessapp.ui.recipes.AddRecipesActivity
import com.fitnessapp.ui.recipes.RecipesActivity
import com.fitnessapp.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class AddWorkoutActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var repository: WorkoutRepository
    private lateinit var session: SessionManager

    private lateinit var etWorkoutName: EditText
    private lateinit var etDuration: EditText
   private lateinit var llExercisesContainer: LinearLayout
    private lateinit var btnAddExercise: Button
    private lateinit var btnSaveWorkout: Button
    private lateinit var btnCloseWorkout: Button
    private lateinit var tvGreeting: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_workout)

        session = SessionManager(this)
        db = AppDatabase.getInstance(this)
        repository = WorkoutRepository(db.workoutDao())

        tvGreeting = findViewById(R.id.tvGreeting)

        etWorkoutName = findViewById(R.id.et_workout_name)
        etDuration = findViewById(R.id.et_duration)
        llExercisesContainer = findViewById(R.id.ll_exercises_container)
        btnAddExercise = findViewById(R.id.btn_add_exercise)
        btnSaveWorkout = findViewById(R.id.btn_save_workout)
        btnCloseWorkout = findViewById(R.id.btn_close_workout)

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

        btnCloseWorkout.setOnClickListener {
            finish()
        }

        // Add one exercise row by default to start
        addExerciseView()
        btnAddExercise.setOnClickListener {
            addExerciseView()
        }

        btnSaveWorkout.setOnClickListener {
            saveWorkout()
        }

        setupNavigation()
    }

    private fun saveWorkout() {
        val workoutName = etWorkoutName.text.toString().trim()
        val duration = etDuration.text.toString().toIntOrNull() ?: 0
        if (workoutName.isBlank()) {
            Toast.makeText(this, "Please enter a workout name.", Toast.LENGTH_SHORT).show()
            return
        }

        if (llExercisesContainer.childCount == 0) {
            Toast.makeText(this, "Please add at least one exercise.", Toast.LENGTH_SHORT).show()
            return
        }

        //make a list of exercises
        val exerciseList = mutableListOf<Exercise>()
        //Go through each child of llExercisesContainer and pick properties of Each Exercise
        for(i in 0 until llExercisesContainer.childCount) {
            val view: View = llExercisesContainer.getChildAt(i)
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
                Toast.makeText(this, "Please fill all necessary fields for each exercise.", Toast.LENGTH_SHORT).show()
                return
            }
            // Add to the list
            exerciseList.add(Exercise(workoutId = 0, name = exerciseName, sets = sets, reps = reps, weight = weight, notes = notes))
        }


        lifecycleScope.launch {
            //create NewWorkout and add it to repository
            val newWorkout = Workout(name = workoutName, durationMinutes = duration, isFavorite = false)
            val newWorkoutID=repository.insertWorkoutAndGetId(newWorkout)

            //get the newly inserted workout's ID and update the exercise's workoutID property
            //add the exercises to the repository
            val updatedExercises = exerciseList.map { it.copy(workoutId = newWorkoutID.toInt()) }
            repository.insertExercises(updatedExercises)


            Toast.makeText(this@AddWorkoutActivity, "Workout Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun addExerciseView() {
        //Create an inflater that inflates the exercise_row_item.xml layout so it shows up in
        //workout scrollview
        val inflater = LayoutInflater.from(this)
        //each click of button will make a new exercise view and add it to the scrollview
        val exerciseView = inflater.inflate(R.layout.exercise_row_item, llExercisesContainer, false)

        //delete exercise from view
        val btnDeleteExercise = exerciseView.findViewById<ImageButton>(R.id.btn_delete_exercise)
        btnDeleteExercise.setOnClickListener {
            llExercisesContainer.removeView(exerciseView)
        }

        //add the instantiated exercise view to the scrollview (llExercisesContainer)
        llExercisesContainer.addView(exerciseView)
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
