package com.fitnessapp.utils

import android.content.Context
import com.fitnessapp.data.AppDatabase
import com.fitnessapp.data.entities.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Seeds Ontario Tech–area gyms into the local Room database
object DatabaseInitializer {
    fun seedGyms(context: Context) {
        val db = AppDatabase.getInstance(context)
        val gyms = listOf(
            Location(1, "Campus Recreation Centre", "2000 Simcoe St N, Oshawa", 43.9445, -78.8969),
            Location(2, "LA Fitness Oshawa", "1189 Ritson Rd N #4a, Oshawa", 43.9314, -78.8653),
            Location(3, "GoodLife Fitness Oshawa Centre", "419 King St W, Oshawa", 43.9004, -78.8701),
            Location(4, "Crossfit Totality", "79 Taunton Rd W, Oshawa", 43.9319, -78.8819),
            Location(5, "F45 Training Oshawa North", "1383 Wilson Rd N, Oshawa", 43.9390, -78.8573),
            Location(6, "LA Fitness Whitby", "350 Taunton Rd E, Whitby", 43.9217, -78.9474),
            Location(7, "Planet Fitness Whitby", "4160 Baldwin St S, Whitby", 43.9191, -78.9602)
        )
        CoroutineScope(Dispatchers.IO).launch {
            gyms.forEach { db.locationDao().insertLocation(it) }
        }
    }
}
