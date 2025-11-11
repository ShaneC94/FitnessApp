package com.fitnessapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.fitnessapp.data.entities.Recipe
import kotlinx.coroutines.flow.Flow

// Data Access Object for interacting with the recipes table
// Provides methods to query, insert, update, and delete recipe entries in the local Room database
@Dao
interface RecipeDao {

    // Retrieves all saved recipes from the local database
    @Query("SELECT * FROM recipes ORDER BY id DESC")
    fun getAllRecipes(): Flow<List<Recipe>>

    // Inserts a new recipe into the database
    // If a duplicate (same primary key) exists, it will replace the old record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    // Updates an existing recipe’s information in the database
    @Update
    suspend fun updateRecipe(recipe: Recipe)

    // Deletes a specific recipe from the database
    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    // Clears all stored recipes from the local database
    @Query("DELETE FROM recipes")
    suspend fun clearAll()
}
