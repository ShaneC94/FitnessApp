package com.fitnessapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fitnessapp.data.dao.UserDao
import com.fitnessapp.data.dao.LocationDao
import com.fitnessapp.data.dao.RecipeDao
import com.fitnessapp.data.dao.WorkoutDao
import com.fitnessapp.data.dao.ExerciseDao
import com.fitnessapp.data.entities.User
import com.fitnessapp.data.entities.Location
import com.fitnessapp.data.entities.Recipe
import com.fitnessapp.data.entities.Workout
import com.fitnessapp.data.entities.Exercise

// The main Room database for the Fitness App
// Defines DB config and is the main access point for connecting to persisted data
// Manages DAO instances and DB creation
@Database(entities = [User::class, Location::class, Recipe::class, Workout::class, Exercise::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // Provides access to DAO methods for the user entity
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
    abstract fun recipeDao(): RecipeDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        // Volatile ensures that changes to this variable are immediately visible to all threads
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_app_db"
                )
                    .fallbackToDestructiveMigration() // if a schema mismatch occurs, drop and recreate table.
                    // Fine for testing/debugging. Should be changed to safe migration for project finalization
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
