package com.fitnessapp.ui.recipes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.repositories.RecipeRepository
import com.fitnessapp.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipesActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var rvRecipes: RecyclerView
    private lateinit var repository: RecipeRepository
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        session = SessionManager(this)
        db = AppDatabase.getInstance(this)
        repository = RecipeRepository(db.recipeDao())

        rvRecipes = findViewById(R.id.rvRecipes)
        rvRecipes.layoutManager = LinearLayoutManager(this)

        adapter = RecipeAdapter(emptyList())
        rvRecipes.adapter = adapter

        loadRecipes()
    }

    private fun loadRecipes() {
        lifecycleScope.launch {
            repository.allRecipes.collectLatest { recipeList ->
                adapter.updateRecipes(recipeList)
            }
        }
    }
}
