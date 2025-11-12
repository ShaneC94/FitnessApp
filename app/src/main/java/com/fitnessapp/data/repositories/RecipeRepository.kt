package com.fitnessapp.data.repositories

import com.fitnessapp.data.dao.RecipeDao
import com.fitnessapp.data.entities.Recipe
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {

    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun insert(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun update(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun delete(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe)
    }
    suspend fun getRecipeById(recipeId: Int): Recipe? {
        return recipeDao.getRecipeById(recipeId)
    }
}
