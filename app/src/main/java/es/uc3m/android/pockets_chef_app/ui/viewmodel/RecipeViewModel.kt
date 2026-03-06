package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import es.uc3m.android.pockets_chef_app.data.model.Recipe

class RecipeViewModel : ViewModel() {

    var searchQuery by mutableStateOf("")
    var showFavoritesOnly by mutableStateOf(false)

    private var _recipes by mutableStateOf(
        listOf(
            Recipe(id = 1, title = "Classic Scrambled Eggs",
                description = "Fluffy and creamy scrambled eggs",
                duration = "10 min", servings = 2, category = "Breakfast",
                ingredients = "Eggs, Butter, Milk, Salt, Pepper"),
            Recipe(id = 2, title = "Grilled Chicken Breast",
                description = "Juicy grilled chicken with herbs",
                duration = "25 min", servings = 4, category = "Main",
                ingredients = "Chicken Breast, Olive Oil, Garlic, Salt, Paprika"),
            Recipe(id = 3, title = "Tomato Pasta",
                description = "Simple pasta with fresh tomato sauce",
                duration = "30 min", servings = 4, category = "Main",
                ingredients = "Pasta, Tomatoes, Garlic, Olive Oil, Basil"),
            Recipe(id = 4, title = "Garlic Rice",
                description = "Fragrant garlic rice side dish",
                duration = "20 min", servings = 3, category = "Side",
                ingredients = "Rice, Garlic, Butter, Salt, Chicken Stock")
        )
    )

    val recipes: List<Recipe>
        get() {
            val base = if (showFavoritesOnly) _recipes.filter { it.isFavorite } else _recipes
            return if (searchQuery.isBlank()) base
                   else base.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }

    fun toggleFavorite(recipe: Recipe) {
        _recipes = _recipes.map {
            if (it.id == recipe.id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }
}
