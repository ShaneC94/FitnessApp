package com.fitnessapp.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.Location
import com.fitnessapp.ui.auth.LoginActivity
import com.fitnessapp.ui.recipes.AddRecipesActivity
import com.fitnessapp.ui.recipes.RecipesActivity
import com.fitnessapp.ui.workouts.AddWorkoutActivity
import com.fitnessapp.ui.workouts.WorkoutActivity
import com.fitnessapp.utils.DatabaseInitializer
import com.fitnessapp.utils.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocationAdapter
    private val gymList = mutableListOf<Location>()
    private val userLocation = LatLng(43.9442, -78.8964) // Ontario Tech default

    // Add session for logout and consistency
    private lateinit var tvGreeting: TextView
    private lateinit var session: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Initialize session for logout button
        session = SessionManager(this)
        db = AppDatabase.getInstance(this)

        tvGreeting = findViewById(R.id.tvGreeting)

        recyclerView = findViewById(R.id.recyclerViewGyms)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LocationAdapter(emptyList()) { gym -> focusOnGym(gym) }
        recyclerView.adapter = adapter

        // Personalized greeting
        lifecycleScope.launch {
            val userId = session.getUserId()
            val user = userId?.let { db.userDao().getUserById(it) }
            if (user != null) {
                tvGreeting.text = "Explore nearby gyms, ${user.username}!"
            } else {
                tvGreeting.text = "Explore nearby gyms!"
            }
        }

        // Seed DB with gyms if the table is empty
        DatabaseInitializer.seedGyms(this)

        // Setup Map Fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize navigation bar buttons
        setupNavigation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableLocation()
        loadGyms()
    }

    private fun enableLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun loadGyms() {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = db.locationDao()
            val gyms = dao.getAllLocations()
            val sortedGyms = gyms.map {
                val distance = haversine(
                    userLocation.latitude, userLocation.longitude,
                    it.latitude, it.longitude
                )
                it to distance
            }.sortedBy { it.second }

            runOnUiThread {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f))
                sortedGyms.forEach { (gym, distance) ->
                    val pos = LatLng(gym.latitude, gym.longitude)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title("${gym.name} (${String.format("%.2f", distance)} km)")
                    )
                }

                gymList.clear()
                gymList.addAll(sortedGyms.map { it.first })
                adapter.updateGyms(sortedGyms)
            }
        }
    }

    // Calculates distance between two points
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun focusOnGym(gym: Location) {
        if (!::mMap.isInitialized) return

        // Define the selected gym's coordinates
        val gymLatLng = LatLng(gym.latitude, gym.longitude)

        // Clear previous markers to avoid clutter
        mMap.clear()

        // Re-add all gym markers
        gymList.forEach { g ->
            val pos = LatLng(g.latitude, g.longitude)
            mMap.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(g.name)
            )
        }

        // Highlight selected gym marker
        val selectedMarker = mMap.addMarker(
            MarkerOptions()
                .position(gymLatLng)
                .title("${gym.name} (${String.format("%.2f", haversine(userLocation.latitude, userLocation.longitude, gym.latitude, gym.longitude))} km away)")
        )

        // Move and zoom camera
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gymLatLng, 16f))

        // Show the marker info window
        selectedMarker?.showInfoWindow()
    }


    // ===== NAVIGATION BAR FUNCTIONALITY =====
    private fun setupNavigation() {
        // Logout button
        findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Floating Action Button (+)
        findViewById<FloatingActionButton>(R.id.fabAdd)?.setOnClickListener {
            showAddPopup()
        }

        // Recipes Button
        findViewById<Button>(R.id.btnRecipes)?.setOnClickListener {
            startActivity(Intent(this, RecipesActivity::class.java))
        }

        // Workouts Button
        findViewById<Button>(R.id.btnWorkouts).setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }
    }

    // ===== ADD BUTTON POPUP =====
    private fun showAddPopup() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.popup_add_options, null)
        dialog.setContentView(view)

        // Make buttons visible
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

        view.findViewById<Button>(R.id.btnChat).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, com.fitnessapp.ui.chat.ChatActivity::class.java))
        }
        view.findViewById<Button>(R.id.btnPopularExercises)?.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, com.fitnessapp.ui.popularExercises.PopularExercisesActivity::class.java))
        }

        dialog.show()
    }
}
