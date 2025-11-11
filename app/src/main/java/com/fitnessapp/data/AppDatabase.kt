package com.fitnessapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fitnessapp.data.dao.UserDao
import com.fitnessapp.data.dao.LocationDao
import com.fitnessapp.data.entities.User
import com.fitnessapp.data.entities.Location

// The main Room database for the Fitness App
// Defines DB config and is the main access point for connecting to persisted data
// Manages DAO instances and DB creation
@Database(entities = [User::class, Location::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // Provides access to DAO methods for the user entity
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao

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
