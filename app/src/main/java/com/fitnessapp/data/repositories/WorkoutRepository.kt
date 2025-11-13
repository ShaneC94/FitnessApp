package com.fitnessapp.data.repositories

import com.fitnessapp.data.dao.WorkoutDao
import com.fitnessapp.data.entities.Exercise
import com.fitnessapp.data.entities.Workout
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    val allWorkouts: Flow<List<Workout>> = workoutDao.getAllWorkouts()

    suspend fun insert(workout: Workout) {
        workoutDao.insertWorkout(workout)
    }

    suspend fun update(workout: Workout) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun delete(workout: Workout) {
        workoutDao.deleteWorkout(workout)
    }

    suspend fun insertWorkoutAndGetId(workout: Workout): Long {
        return workoutDao.insertWorkoutAndGetId(workout)
    }

    suspend fun insertExercises(exercises: List<Exercise>) {
        workoutDao.insertExercises(exercises)
    }


}
