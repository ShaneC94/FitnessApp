package com.fitnessapp.ui.recipes

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File

class AddRecipesActivity : AppCompatActivity() {

    // Database + repository + session manager
    private lateinit var db: AppDatabase
    private lateinit var repository: RecipeRepository
    private lateinit var session: SessionManager

    // UI fields
    private lateinit var etRecipeName: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etInstructions: EditText
    private lateinit var etPreparationTime: EditText
    private lateinit var etCalories: EditText
    private lateinit var btnSaveRecipe: Button
    private lateinit var btnCloseRecipe: Button
    private lateinit var tvGreeting: TextView

    // Image controls
    private lateinit var imagePreview: ImageView
    private lateinit var btnAddPhoto: Button
    private lateinit var btnAddGallery: Button
    private lateinit var btnRemovePhoto: Button

    // Activity result launchers for image input
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickPhotoLauncher: ActivityResultLauncher<String>

    private var imageUri: Uri? = null
    private var selectedImageUri: String? = null

    companion object {
        private const val PICK_IMAGE = 1001
    }

    // Permission launcher for camera usage
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) launchCamera()
            else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // Initialize session + database + repository
        session = SessionManager(this)
        db = AppDatabase.getInstance(this)
        repository = RecipeRepository(db.recipeDao())

        // Grab UI references
        tvGreeting = findViewById(R.id.tvGreeting)
        etRecipeName = findViewById(R.id.editText_recipe_name)
        etIngredients = findViewById(R.id.editText_ingredients)
        etInstructions = findViewById(R.id.editText_instructions)
        etPreparationTime = findViewById(R.id.editText_preptime)
        etCalories = findViewById(R.id.editText_calories)
        btnSaveRecipe = findViewById(R.id.button_save_recipe)
        btnCloseRecipe = findViewById(R.id.button_close_recipe)
        imagePreview = findViewById(R.id.image_recipe_preview)
        btnAddPhoto = findViewById(R.id.button_add_photo)
        btnAddGallery = findViewById(R.id.button_add_gallery)
        btnRemovePhoto = findViewById(R.id.button_remove_photo)

        // === CAMERA RESULT HANDLER ===
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // Save image from camera into internal storage
                val savedPath = saveImageToInternalStorage(imageUri!!)
                if (savedPath != null) {
                    selectedImageUri = savedPath
                    imagePreview.setImageBitmap(BitmapFactory.decodeFile(savedPath))
                    updatePhotoUI()
                } else {
                    Toast.makeText(this, "Failed to save camera image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // === GALLERY RESULT HANDLER ===
        pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val savedPath = saveImageToInternalStorage(it)
                if (savedPath != null) {
                    selectedImageUri = savedPath
                    imagePreview.setImageBitmap(BitmapFactory.decodeFile(savedPath))
                    updatePhotoUI()
                } else {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Personalized greeting for user
        lifecycleScope.launch {
            val userId = session.getUserId()
            val user = userId?.let { db.userDao().getUserById(it) }
            tvGreeting.text = user?.let { "Healthy eating is key, ${it.username}!" }
                ?: "Eating healthy is key!"
        }

        // Close button
        btnCloseRecipe.setOnClickListener { finish() }

        // === SAVE RECIPE ===
        btnSaveRecipe.setOnClickListener { saveRecipe() }

        // Trigger gallery image selector
        btnAddGallery.setOnClickListener {
            pickPhotoLauncher.launch("image/*")
        }

        // Request camera permission (or open camera)
        btnAddPhoto.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                launchCamera()
            else
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // Remove selected photo
        btnRemovePhoto.setOnClickListener {
            selectedImageUri = null
            imagePreview.setImageDrawable(null)
            updatePhotoUI()
        }

        updatePhotoUI()
        setupNavigation()
    }

    // Launches camera and creates temporary file for image
    private fun launchCamera() {
        val photoFile = File.createTempFile("image_", ".jpg", cacheDir)
        val uri = FileProvider.getUriForFile(
            this, "${packageName}.provider", photoFile
        )
        imageUri = uri
        takePhotoLauncher.launch(uri)
    }

    // Updates UI visibility depending on whether user selected a picture
    private fun updatePhotoUI() {
        val hasPhoto = selectedImageUri != null
        btnAddPhoto.visibility = if (hasPhoto) View.GONE else View.VISIBLE
        btnAddGallery.visibility = if (hasPhoto) View.GONE else View.VISIBLE
        btnRemovePhoto.visibility = if (hasPhoto) View.VISIBLE else View.GONE
        imagePreview.visibility = if (hasPhoto) View.VISIBLE else View.GONE
    }

    // Save image from camera/gallery into app’s internal storage
    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val input = contentResolver.openInputStream(uri) ?: return null
            val fileName = "recipe_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)

            file.outputStream().use { output -> input.copyTo(output) }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Validates user input and inserts recipe into DB
    private fun saveRecipe() {
        val recipeName = etRecipeName.text.toString().trim()
        val ingredients = etIngredients.text.toString().trim()
        val instructions = etInstructions.text.toString().trim()
        val prepTime = etPreparationTime.text.toString().trim().toIntOrNull()
        val calories = etCalories.text.toString().trim().toIntOrNull()

        // Validate required fields
        when {
            recipeName.isBlank() -> toast("Please enter a recipe name.")
            ingredients.isBlank() -> toast("Please enter ingredients.")
            instructions.isBlank() -> toast("Please enter instructions.")
            prepTime == null || prepTime <= 0 -> toast("Enter a valid preparation time.")
            calories == null || calories < 0 -> toast("Enter valid calories.")
            else -> {
                val recipe = Recipe(
                    name = recipeName,
                    ingredients = ingredients,
                    instructions = instructions,
                    preparationTime = prepTime,
                    calories = calories,
                    imageUri = selectedImageUri
                )
                lifecycleScope.launch {
                    repository.insert(recipe)
                    toast("Recipe saved!")
                    startActivity(Intent(this@AddRecipesActivity, RecipesActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // Navigation bar setup
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

    // Floating action button bottom-sheet menu
    private fun showAddPopup() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.popup_add_options, null)
        dialog.setContentView(view)

        // Show all buttons
        val ids = listOf(
            R.id.btnAddWorkout, R.id.btnAddRecipe, R.id.btnLogProgress,
            R.id.btnCamera, R.id.btnMap, R.id.btnMain,
            R.id.btnChat, R.id.btnPopularExercises
        )
        ids.forEach { view.findViewById<Button>(it).visibility = View.VISIBLE }

        // Setup click listeners
        view.findViewById<Button>(R.id.btnAddWorkout).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AddWorkoutActivity::class.java))
        }
        view.findViewById<Button>(R.id.btnAddRecipe).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AddRecipesActivity::class.java))
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
