package com.fitnessapp.ui.main

// This object acts as a simple in-memory database.
// All activities will share this single instance.
object  RecipeRepository {
    private val recipes = mutableListOf<Recipe>()

    init {
        recipes.addAll(
            listOf(
                Recipe(
                    "Recipe 1",
                    "Ingredients 1", "Instructions 1",
                    10, 100
                ),
                Recipe(
                    "Recipe 2", "Ingredients 2",
                    "Instructions 2",
                    20, 200
                ),
                Recipe(
                    "Recipe 3",
                    "Ingredients 3", "Instructions 3",
                    30, 300
                )
            )
        )
    }

    fun getAllRecipes(): List<Recipe> {
        return recipes
    }

    fun addRecipe(recipe: Recipe) {
        recipes.add(recipe)
    }
}