package com.fitnessapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ingredients: String,
    val instructions: String,
    val preparationTime: Int,
    val calories: Int,
    val imageUri: String? = null,
    var isFavorite: Boolean = false
)


