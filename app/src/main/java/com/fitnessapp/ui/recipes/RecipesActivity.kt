package com.fitnessapp.ui.recipes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.Recipe
import com.fitnessapp.data.repositories.RecipeRepository
import com.fitnessapp.ui.auth.LoginActivity
import com.fitnessapp.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.fitnessapp.ui.recipes.RecipesActivity
import com.google.android.material.bottomsheet.BottomSheetDialog


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

        //lambda function passed in handles clicks on the recipe
        adapter = RecipeAdapter(emptyList()) {recipe ->
            onRecipeClicked(recipe)
        }
        rvRecipes.adapter = adapter

        loadRecipes()
        setupNavigation()
    }
    private fun onRecipeClicked(recipe: Recipe) {
        // intent to navigate to your new details activity
        val intent = Intent(this, RecipeDetailActivity::class.java)

        // Pass the unique ID of the clicked recipe to the next activity
        intent.putExtra("RECIPE_ID", recipe.id)

        startActivity(intent)
    }



    private fun loadRecipes() {
        lifecycleScope.launch {
            repository.allRecipes.collectLatest { recipeList ->
                adapter.updateRecipes(recipeList)
            }
        }
    }
    private fun setupNavigation() {
        // Logout button (top right)
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Floating action button (+)
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddPopup()
        }

        // Recipes button
        findViewById<Button>(R.id.btnRecipes).setOnClickListener {
            startActivity(Intent(this, RecipesActivity::class.java))
        }

        // Workouts button
        findViewById<Button>(R.id.btnWorkouts).setOnClickListener {
            // startActivity(Intent(this, WorkoutsActivity::class.java))
        }
    }

    // A BottomSheetDialog with multiple options for features that can be implemented later
    private fun showAddPopup() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.popup_add_options, null)
        dialog.setContentView(view)

        view.findViewById<Button>(R.id.btnAddWorkout).setOnClickListener {
            dialog.dismiss()
            // open AddWorkoutActivity()
        }

        view.findViewById<Button>(R.id.btnAddRecipe).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AddRecipesActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnLogProgress).setOnClickListener {
            dialog.dismiss()
            // open LogProgressActivity()
        }

        view.findViewById<Button>(R.id.btnCamera).setOnClickListener {
            dialog.dismiss()
            // open CameraIntegration()
        }

        val btnMap = view.findViewById<Button>(R.id.btnMap)
        btnMap.text = "Return to Main Page"
        btnMap.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, com.fitnessapp.ui.main.MainActivity::class.java)
            startActivity(intent)
            finish()
        }



        dialog.show()
    }
}


