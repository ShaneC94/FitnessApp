package com.fitnessapp.data.dao

import androidx.room.*
import com.fitnessapp.data.entities.Exercise
import com.fitnessapp.data.entities.Workout
import kotlinx.coroutines.flow.Flow

// Data Access Object for interacting with the workouts table
// Provides methods to query, insert, update, and delete workout entries in the local Room database
@Dao
interface WorkoutDao {

    // Retrieves all saved workouts from the local database
    @Query("SELECT * FROM workouts ORDER BY id DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    //get single workout with that ID
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Int): Workout?
    //delete all exercises from that workoutID
    @Query("Delete FROM exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesByWorkoutID(workoutId: Int)

    // Inserts a new workout into the database
    // If a duplicate (same primary key) exists, it will replace the old record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    // Updates an existing workout’s information in the database
    @Update
    suspend fun updateWorkout(workout: Workout)

    // Deletes a specific workout from the database
    @Delete
    suspend fun deleteWorkout(workout: Workout)

    // Clears all stored workouts from the local database
    @Query("DELETE FROM workouts")
    suspend fun clearAll()

    @Insert
    suspend fun insertWorkoutAndGetId(workout: Workout): Long // Room returns the new row ID as a Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Query("SELECT * FROM workouts WHERE isFavorite = 1")
    fun getFavoriteWorkouts(): Flow<List<Workout>>
}
