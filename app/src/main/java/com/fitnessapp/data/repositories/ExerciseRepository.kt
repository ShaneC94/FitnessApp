package com.fitnessapp.data.repositories

import com.fitnessapp.data.dao.ExerciseDao
import com.fitnessapp.data.entities.Exercise
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val exerciseDao: ExerciseDao) {

    fun getExercisesForWorkout(workoutId: Int): Flow<List<Exercise>> {
        return exerciseDao.getExercisesForWorkout(workoutId)
    }

    suspend fun insert(exercise: Exercise) {
        exerciseDao.insertExercise(exercise)
    }

    suspend fun update(exercise: Exercise) {
        exerciseDao.updateExercise(exercise)
    }

    suspend fun delete(exercise: Exercise) {
        exerciseDao.deleteExercise(exercise)
    }
}
