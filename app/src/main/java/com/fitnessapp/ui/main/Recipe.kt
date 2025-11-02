package com.fitnessapp.ui.main
//Just a data class for Recipe
data class Recipe(
                  val name: String,
                  val ingredients: String,
                  val instructions: String,
                  val preparationTime: Int,
                  val calories: Int,
                  var isFavorite: Boolean = false
) {

}