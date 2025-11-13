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

class AddRecipesActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var repository: RecipeRepository
    private lateinit var session: SessionManager

    private lateinit var etRecipeName: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etInstructions: EditText
    private lateinit var etPreparationTime: EditText
    private lateinit var etCalories: EditText
    private lateinit var btnSaveRecipe: Button
    private lateinit var btnCloseRecipe: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        session = SessionManager(this)
        db = AppDatabase.getInstance(this)
        repository = RecipeRepository(db.recipeDao())

        etRecipeName = findViewById(R.id.editText_recipe_name)
        etIngredients = findViewById(R.id.editText_ingredients)
        etInstructions = findViewById(R.id.editText_instructions)
        etPreparationTime = findViewById(R.id.editText_preptime)
        etCalories = findViewById(R.id.editText_calories)
        btnSaveRecipe = findViewById(R.id.button_save_recipe)
        btnCloseRecipe = findViewById(R.id.button_close_recipe)

        btnCloseRecipe.setOnClickListener {
            finish()
        }

        btnSaveRecipe.setOnClickListener {
            val recipeName = etRecipeName.text.toString()
            val ingredients = etIngredients.text.toString()
            val instructions = etInstructions.text.toString()
            val preparationTime = etPreparationTime.text.toString().toInt()
            val calories = etCalories.text.toString().toInt()

            if (recipeName.isBlank() || ingredients.isBlank() || instructions.isBlank() || preparationTime == null || calories == null) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newRecipe = Recipe(
                name = recipeName,
                ingredients = ingredients,
                instructions = instructions,
                preparationTime = preparationTime,
                calories = calories
            )

            lifecycleScope.launch {
                repository.insert(newRecipe)
                finish()
            }
        }
    }
}


