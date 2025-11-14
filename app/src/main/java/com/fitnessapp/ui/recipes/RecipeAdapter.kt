package com.fitnessapp.ui.recipes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fitnessapp.R
import com.fitnessapp.data.entities.Recipe

//adapts data from recipes to the recycler view
//takes in a list of recipes called 'recipes'
//it inherits from RecyclerView.Adapter and takes in a RecipeViewHolder
class RecipeAdapter(
    var recipes: List<Recipe>,
    private val onItemClicked: (Recipe) -> Unit,
    private val onFavoriteToggled: (Recipe, Boolean) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    //holds the views of recycler view from recipe_row.xml
    //passing in the the row which is a view and it inherits from RecyclerView class
    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeNameTextView: TextView = itemView.findViewById(R.id.tv_recipe_name)
        val recipePrepTimeTextView: TextView = itemView.findViewById(R.id.tv_recipe_preptime)
        val favoriteCheckBox: CheckBox = itemView.findViewById(R.id.favRecipeBox)

    }

    //called when user scrolls and it has to create a new row item that is visible
    //returns a RecipeViwHolder item (row)
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecipeViewHolder {
        //allows access to the rv_recipe_row.xml file
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_recipe_row,
            parent,
            false
        )
        return RecipeViewHolder(view)
    }

    //binds the data from the list of recipes to the views of the row item
    //it takes in the recipeViewHolder (row) to access its views and its position in the list
    override fun onBindViewHolder(
        holder: RecipeViewHolder,
        position: Int
    ) {
        val recipe = recipes[position]

        //access the itemview of the row
        //assigns properties of the Recipe object to the views of the row
        holder.recipeNameTextView.text = recipe.name
        holder.recipePrepTimeTextView.text = recipe.preparationTime.toString()

        holder.favoriteCheckBox.setOnCheckedChangeListener (null)
        holder.favoriteCheckBox.isChecked = recipe.isFavorite

        holder.favoriteCheckBox.setOnCheckedChangeListener { _, isChecked ->
            onFavoriteToggled(recipe, isChecked)
        }

        holder.itemView.setOnClickListener {
            onItemClicked(recipes[position]) //current recipe in the list
        }
    }

        //returns the size of the list of recipes in the recyclerview
        override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newList: List<Recipe>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = recipes.size
            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(old: Int, new: Int) =
                recipes[old].id == newList[new].id

            override fun areContentsTheSame(old: Int, new: Int) =
                recipes[old] == newList[new]
        })
        recipes = newList
        diffResult.dispatchUpdatesTo(this)
    }
}