package com.fitnessapp.ui.recipes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.Recipe
import com.fitnessapp.data.repositories.RecipeRepository
import com.fitnessapp.utils.SessionManager
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var repository: RecipeRepository
    private lateinit var session: SessionManager

    private lateinit var etRecipeName: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etInstructions: EditText
    private lateinit var etPreparationTime: EditText
    private lateinit var etCalories: EditText
    private lateinit var btnUpdateRecipe: Button
    private lateinit var btnCloseRecipe: Button
    private var currentRecipeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_recipe)

        session = SessionManager(this)
        db = AppDatabase.getInstance(this)
        repository = RecipeRepository(db.recipeDao())

        etRecipeName = findViewById(R.id.editText_recipe_name)
        etIngredients = findViewById(R.id.editText_ingredients)
        etInstructions = findViewById(R.id.editText_instructions)
        etPreparationTime = findViewById(R.id.editText_preptime)
        etCalories = findViewById(R.id.editText_calories)
        btnUpdateRecipe = findViewById(R.id.button_update_recipe)
        btnCloseRecipe = findViewById(R.id.button_close_recipe)

        //get recipe Id from intent
        currentRecipeId = intent.getIntExtra("RECIPE_ID", -1)

        if (currentRecipeId != -1) {
            //Load the recipe details into the fields from the database
            lifecycleScope.launch {
                val recipe = repository.getRecipeById(currentRecipeId)
                recipe?.let {
                    etRecipeName.setText(it.name)
                    etIngredients.setText(it.ingredients)
                    etInstructions.setText(it.instructions)
                    etPreparationTime.setText(it.preparationTime.toString())
                    etCalories.setText(it.calories.toString())
                }
            }
            } else {
            // Handle error case where ID is not passed correctly
            Toast.makeText(this, "Error: Recipe not found.", Toast.LENGTH_LONG).show()
            finish()
        }
        btnCloseRecipe.setOnClickListener {
            finish()
        }

        btnUpdateRecipe.setOnClickListener {
            val recipeName = etRecipeName.text.toString()
            val ingredients = etIngredients.text.toString()
            val instructions = etInstructions.text.toString()
            val preparationTime = etPreparationTime.text.toString().toIntOrNull()
            val calories = etCalories.text.toString().toIntOrNull()

            if (recipeName.isBlank() || ingredients.isBlank() || instructions.isBlank() || preparationTime == null || calories == null) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
            }

            val updatedRecipe = Recipe(
                id = currentRecipeId,
                name = recipeName,
                ingredients = ingredients,
                instructions = instructions,
                preparationTime = preparationTime,
                calories = calories
            )

            lifecycleScope.launch {
                repository.update(updatedRecipe)
                Toast.makeText(this@RecipeDetailActivity, "Recipe updated successfully.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}


