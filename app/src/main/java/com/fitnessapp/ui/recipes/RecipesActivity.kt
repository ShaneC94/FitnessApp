package com.fitnessapp.ui.recipes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.Recipe
import com.fitnessapp.data.repositories.RecipeRepository
import com.fitnessapp.ui.auth.LoginActivity
import com.fitnessapp.ui.chat.ChatActivity
import com.fitnessapp.ui.main.MainActivity
import com.fitnessapp.ui.map.MapActivity
import com.fitnessapp.ui.popularExercises.PopularExercisesActivity
import com.fitnessapp.ui.workouts.AddWorkoutActivity
import com.fitnessapp.ui.workouts.WorkoutActivity
import com.fitnessapp.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.google.android.material.bottomsheet.BottomSheetDialog

class RecipesActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var rvRecipes: RecyclerView
    private lateinit var repository: RecipeRepository
    private lateinit var tvGreeting: TextView
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        session = SessionManager(this)
        db = AppDatabase.getInstance(this)
        repository = RecipeRepository(db.recipeDao())

        tvGreeting = findViewById(R.id.tvGreeting)
        rvRecipes = findViewById(R.id.rvRecipes)
        rvRecipes.layoutManager = LinearLayoutManager(this)

        adapter = RecipeAdapter(
            emptyList(),
            onItemClicked = { recipe -> onRecipeClicked(recipe) },
            onFavoriteToggled = { recipe, isChecked ->
                lifecycleScope.launch {
                    val updated = recipe.copy(isFavorite = isChecked)
                    db.recipeDao().updateRecipe(updated)
                }
            }
        )
        rvRecipes.adapter = adapter

        lifecycleScope.launch {
            val userId = session.getUserId()
            val user = userId?.let { db.userDao().getUserById(it) }
            tvGreeting.text = if (user != null)
                "Healthy eating is key, ${user.username}!"
            else
                "Eating healthy is key!"
        }

        loadRecipes()
        setupNavigation()
    }

    private fun onRecipeClicked(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
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
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddPopup()
        }

        findViewById<Button>(R.id.btnRecipes).setOnClickListener {
            startActivity(Intent(this, RecipesActivity::class.java))
        }

        findViewById<Button>(R.id.btnWorkouts).setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }
    }

    private fun showAddPopup() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.popup_add_options, null)
        dialog.setContentView(view)

        val buttonIds = listOf(
            R.id.btnAddWorkout,
            R.id.btnAddRecipe,
            R.id.btnLogProgress,
            R.id.btnCamera,
            R.id.btnMap,
            R.id.btnMain,
            R.id.btnChat,
            R.id.btnPopularExercises
        )

        buttonIds.forEach { id ->
            view.findViewById<Button>(id).visibility = View.VISIBLE
        }

        view.findViewById<Button>(R.id.btnAddWorkout).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AddWorkoutActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnAddRecipe).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AddRecipesActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnCamera).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnMap).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, MapActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnMain).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, MainActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnChat).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, ChatActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnPopularExercises).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, PopularExercisesActivity::class.java))
        }

        dialog.show()
    }
}
