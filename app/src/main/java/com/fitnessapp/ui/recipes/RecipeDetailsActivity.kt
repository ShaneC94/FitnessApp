package com.fitnessapp.ui.recipes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import com.fitnessapp.ui.auth.LoginActivity
import com.fitnessapp.ui.chat.ChatActivity
import com.fitnessapp.ui.main.MainActivity
import com.fitnessapp.ui.map.MapActivity
import com.fitnessapp.ui.popularExercises.PopularExercisesActivity
import com.fitnessapp.ui.workouts.AddWorkoutActivity
import com.fitnessapp.ui.workouts.WorkoutActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
    private lateinit var tvGreeting: TextView
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
        tvGreeting = findViewById(R.id.tvGreeting)

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

        lifecycleScope.launch {
            val userId = session.getUserId()
            val user = userId?.let { db.userDao().getUserById(it) }
            tvGreeting.text = if (user != null)
                "Healthy eating is key, ${user.username}!"
            else
                "Eating healthy is key!"
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

        setupNavigation()
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
            startActivity(Intent(this, WorkoutActivity::class.java))
        }
    }

    // ===== ADD BUTTON POPUP =====
    private fun showAddPopup() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.popup_add_options, null)
        dialog.setContentView(view)

        // Make ALL buttons visible
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

        // === Button Click Handlers ===
        view.findViewById<Button>(R.id.btnAddWorkout).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AddWorkoutActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnAddRecipe).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AddRecipesActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnLogProgress).setOnClickListener {
            dialog.dismiss()
            // startActivity(Intent(this, LogProgressActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnCamera).setOnClickListener {
            dialog.dismiss()
            // startActivity(Intent(this, CameraIntegration::class.java))
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

        view.findViewById<Button>(R.id.btnPopularExercises)?.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, PopularExercisesActivity::class.java))
        }

        dialog.show()
    }
}
