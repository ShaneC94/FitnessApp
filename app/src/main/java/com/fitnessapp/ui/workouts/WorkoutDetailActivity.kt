package com.fitnessapp.ui.workouts

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.Exercise
import com.fitnessapp.data.entities.Workout
import com.fitnessapp.data.repositories.WorkoutRepository
import kotlinx.coroutines.launch


class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var workoutRepository: WorkoutRepository

    private var currentWorkoutId: Int = -1

    // Views from activity_add_workout.xml
    private lateinit var etWorkoutName: EditText
    private lateinit var etWorkoutDuration: EditText
    private lateinit var llExercisesContainer: LinearLayout
    private lateinit var btnAddExercise: Button
    private lateinit var btnSaveOrUpdate: Button
    private lateinit var btnClose: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reuse the layout from the "Add Workout" screen
        setContentView(R.layout.activity_add_workout)

        db = AppDatabase.getInstance(this)

        workoutRepository = WorkoutRepository(db.workoutDao())

        // Initialize Views
        etWorkoutName = findViewById(R.id.et_workout_name)
        etWorkoutDuration = findViewById(R.id.et_duration)
        llExercisesContainer = findViewById(R.id.ll_exercises_container)
        btnAddExercise = findViewById(R.id.btn_add_exercise)
        btnSaveOrUpdate = findViewById(R.id.btn_save_workout)
        btnClose = findViewById(R.id.btn_close_workout)


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
}
