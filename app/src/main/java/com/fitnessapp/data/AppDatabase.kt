package com.fitnessapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fitnessapp.data.dao.LocationDao
import com.fitnessapp.data.dao.RecipeDao
import com.fitnessapp.data.dao.UserDao
import com.fitnessapp.data.entities.User
import com.fitnessapp.data.entities.Recipe
import com.fitnessapp.data.entities.Location
import java.util.concurrent.Executors

@Database(
    entities = [User::class, Recipe::class, Location::class],
    version = 2,  // increment version if database has changed
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun recipeDao(): RecipeDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newSingleThreadExecutor().execute {
                                getInstance(context).recipeDao().insertAll(
                                    listOf(
                                        Recipe(
                                            name = "Recipe 1",
                                            ingredients = "Ingredients 1",
                                            instructions = "Instructions 1",
                                            preparationTime = 10,
                                            calories = 100
                                        ),
                                        Recipe(
                                            name = "Recipe 2",
                                            ingredients = "Ingredients 2",
                                            instructions = "Instructions 2",
                                            preparationTime = 20,
                                            calories = 200
                                        ),
                                        Recipe(
                                            name = "Recipe 3",
                                            ingredients = "Ingredients 3",
                                            instructions = "Instructions 3",
                                            preparationTime = 30,
                                            calories = 300
                                        )
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}

