package com.fitnessapp.ui.recipes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.Recipe
import com.fitnessapp.data.repositories.RecipeRepository
import com.fitnessapp.utils.SessionManager
import kotlinx.coroutines.launch
import androidx.core.net.toUri

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
    private lateinit var imagePreview: ImageView
    private lateinit var btnAddPhoto: Button
    private lateinit var btnRemovePhoto: Button

    private var currentRecipeId: Int = -1
    private var currentImageUri: String? = null

    companion object {
        private const val PICK_IMAGE = 1001
    }

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

        imagePreview = findViewById(R.id.image_recipe_preview)
        btnAddPhoto = findViewById(R.id.button_add_photo)
        btnRemovePhoto = findViewById(R.id.button_remove_photo)

        currentRecipeId = intent.getIntExtra("RECIPE_ID", -1)

        if (currentRecipeId != -1) {
            // Load data from database
            lifecycleScope.launch {
                val recipe = repository.getRecipeById(currentRecipeId)
                recipe?.let {
                    etRecipeName.setText(it.name)
                    etIngredients.setText(it.ingredients)
                    etInstructions.setText(it.instructions)
                    etPreparationTime.setText(it.preparationTime.toString())
                    etCalories.setText(it.calories.toString())

                    currentImageUri = it.imageUri
                    loadImagePreview(currentImageUri)
                }
            }
        } else {
            Toast.makeText(this, "Error: Recipe not found.", Toast.LENGTH_LONG).show()
            finish()
        }

        btnCloseRecipe.setOnClickListener { finish() }

        btnUpdateRecipe.setOnClickListener { updateRecipe() }

        btnAddPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        btnRemovePhoto.setOnClickListener {
            currentImageUri = null
            imagePreview.setImageResource(R.drawable.ic_placeholder_image)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                currentImageUri = uri.toString()
                imagePreview.setImageURI(uri)
            }
        }
    }

    private fun loadImagePreview(uri: String?) {
        if (!uri.isNullOrEmpty()) {
            imagePreview.setImageURI(uri.toUri())
        } else {
            imagePreview.setImageResource(R.drawable.ic_placeholder_image)
        }
    }

    private fun updateRecipe() {
        val recipeName = etRecipeName.text.toString()
        val ingredients = etIngredients.text.toString()
        val instructions = etInstructions.text.toString()
        val preparationTime = etPreparationTime.text.toString().toIntOrNull()
        val calories = etCalories.text.toString().toIntOrNull()

        if (recipeName.isBlank() || ingredients.isBlank() ||
            instructions.isBlank() || preparationTime == null || calories == null
        ) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val oldRecipe = repository.getRecipeById(currentRecipeId)

            if (oldRecipe == null) {
                Toast.makeText(this@RecipeDetailActivity, "Error loading recipe.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val updatedRecipe = oldRecipe.copy(
                name = recipeName,
                ingredients = ingredients,
                instructions = instructions,
                preparationTime = preparationTime,
                calories = calories,
                imageUri = currentImageUri
            )

            repository.update(updatedRecipe)
            Toast.makeText(this@RecipeDetailActivity, "Recipe updated successfully.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
