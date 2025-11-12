package com.fitnessapp.ui.recipes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.Recipe
import com.fitnessapp.data.repositories.RecipeRepository
import com.fitnessapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.core.view.updatePadding

class AddRecipesActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var etRecipeName: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etInstructions: EditText
    private lateinit var etPreparationTime: EditText
    private lateinit var etCalories: EditText
    private lateinit var btnSaveRecipe: Button
    private lateinit var btnAddPhoto: Button
    private lateinit var ivRecipePreview: ImageView
    private lateinit var buttonRemovePhoto: Button
    private lateinit var imagePreview: ImageView

    private var selectedImageUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedImageUri = data?.data

                selectedImageUri?.let {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    ivRecipePreview.setImageURI(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // Enable edge-to-edge content
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Adjust layout to avoid system bars overlap
        val rootLayout = findViewById<View>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val sysBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = sysBars.top, bottom = sysBars.bottom)
            insets
        }

        etRecipeName = findViewById(R.id.editText_recipe_name)
        etIngredients = findViewById(R.id.editText_ingredients)
        etInstructions = findViewById(R.id.editText_instructions)
        etPreparationTime = findViewById(R.id.editText_preptime)
        etCalories = findViewById(R.id.editText_calories)
        btnSaveRecipe = findViewById(R.id.button_save_recipe)
        btnAddPhoto = findViewById(R.id.button_add_photo)
        buttonRemovePhoto = findViewById(R.id.button_remove_photo)
        ivRecipePreview = findViewById(R.id.image_recipe_preview)
        imagePreview = findViewById(R.id.image_recipe_preview)

        btnAddPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            pickImage.launch(intent)
        }

        // Remove photo functionality
        buttonRemovePhoto.setOnClickListener {
            imagePreview.setImageResource(R.drawable.ic_placeholder_image)
            selectedImageUri = null
            buttonRemovePhoto.isEnabled = false
        }

        btnSaveRecipe.setOnClickListener {
            val newRecipe = Recipe(
                name = etRecipeName.text.toString(),
                ingredients = etIngredients.text.toString(),
                instructions = etInstructions.text.toString(),
                preparationTime = etPreparationTime.text.toString().toIntOrNull() ?: 0,
                calories = etCalories.text.toString().toIntOrNull() ?: 0,
                imageUri = selectedImageUri?.toString()
            )
            lifecycleScope.launch(Dispatchers.IO) {
                RecipeRepository.addRecipe(newRecipe)
            }
            finish()
        }
    }
}
