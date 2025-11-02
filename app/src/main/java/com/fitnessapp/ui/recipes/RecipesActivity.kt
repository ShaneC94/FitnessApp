package com.fitnessapp.ui.recipes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.repositories.RecipeRepository
import com.fitnessapp.ui.recipes.RecipeAdapter
import com.fitnessapp.utils.SessionManager

class RecipesActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var rvRecipes: RecyclerView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        session = SessionManager(this)
        db = AppDatabase.Companion.getInstance(this)

        rvRecipes = findViewById(R.id.rvRecipes)
        rvRecipes.layoutManager = LinearLayoutManager(this)

    }

    override fun onResume() {
        super.onResume()
        loadRecipes()
    }

    private fun loadRecipes() {

        val recipes = RecipeRepository.getAllRecipes()

        val adapter = RecipeAdapter(recipes)
        rvRecipes.adapter = adapter

    }
}