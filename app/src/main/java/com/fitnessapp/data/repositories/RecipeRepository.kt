package com.fitnessapp.data.repositories

import com.fitnessapp.App
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.Recipe
import com.fitnessapp.data.dao.RecipeDao
object RecipeRepository {

    private val recipes = mutableListOf<Recipe>()

    init {
        recipes.addAll(
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


    // Add a recipe to Room database
    fun addRecipe(recipe: Recipe) {
        val db = AppDatabase.getInstance(App.instance)
        db.recipeDao().insert(recipe)
    }

    // Retrieve all recipes from Room database
    fun getAllRecipes(): List<Recipe> {
        val db = AppDatabase.getInstance(App.instance)
        return db.recipeDao().getAllRecipes()
    }
}
