package com.fitnessapp.data.dao

import androidx.room.*
import com.fitnessapp.data.entities.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    // Retrieves all exercises belonging to a specific workout
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId ORDER BY id ASC")
    fun getExercisesForWorkout(workoutId: Int): Flow<List<Exercise>>

    //this one uses specifically to display list of exercises in the Detailed workout activity
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId ORDER BY id ASC")
    suspend fun getExercisesForWorkoutSuspend(workoutId: Int): List<Exercise>

    // Inserts a new exercise into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    // Updates an existing exercise record
    @Update
    suspend fun updateExercise(exercise: Exercise)

    // Deletes a specific exercise from the database
    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    // Clears all stored exercises from the local database
    @Query("DELETE FROM exercises")
    suspend fun clearAll()


}