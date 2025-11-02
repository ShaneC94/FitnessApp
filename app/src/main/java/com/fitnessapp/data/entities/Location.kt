package com.fitnessapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents a gym or fitness location on the map
@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)
