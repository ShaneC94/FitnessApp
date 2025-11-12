package com.fitnessapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fitnessapp.data.entities.Recipe

@Dao
interface RecipeDao {
    @Insert
    fun insert(recipe: Recipe)

    @Insert
    fun insertAll(recipes: List<Recipe>)

    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): List<Recipe>
}

