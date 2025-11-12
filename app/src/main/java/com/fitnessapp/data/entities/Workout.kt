package com.fitnessapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val durationMinutes: Int,
    var isFavorite: Boolean = false
)