package com.fitnessapp.ui.recipes

import androidx.appcompat.app.AppCompatActivity
import com.fitnessapp.R
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.fitnessapp.data.repositories.RecipeRepository
import com.fitnessapp.data.entities.Recipe
import androidx.lifecycle.lifecycleScope

class RecipesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private val recipes = mutableListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        recyclerView = findViewById(R.id.rvRecipes)
        adapter = RecipeAdapter(recipes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadRecipes()
    }

    private fun loadRecipes() {
        lifecycleScope.launch {
            val recipesFromDb = withContext(Dispatchers.IO) {
                RecipeRepository.getAllRecipes()
            }
            recipes.clear()
            recipes.addAll(recipesFromDb)
            adapter.notifyDataSetChanged()
        }
    }
}
