package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import es.uc3m.android.pockets_chef_app.data.model.Ingredient
import es.uc3m.android.pockets_chef_app.data.model.Recipe

class RecipeViewModel : ViewModel() {

    var searchQuery by mutableStateOf("")
    var showFavoritesOnly by mutableStateOf(false)

    private var _recipes by mutableStateOf(
        listOf(
            Recipe(
                id = 1,
                title = "Classic Scrambled Eggs",
                description = "Fluffy and creamy scrambled eggs",
                duration = "10 min",
                servings = 2,
                category = "Breakfast",
                ingredients = listOf(
                    Ingredient("Eggs", "4 large"),
                    Ingredient("Butter", "1 tbsp"),
                    Ingredient("Milk", "2 tbsp"),
                    Ingredient("Salt", "to taste"),
                    Ingredient("Pepper", "to taste")
                )
            ),
            Recipe(
                id = 2,
                title = "Grilled Chicken Breast",
                description = "Juicy grilled chicken with herbs",
                duration = "25 min",
                servings = 4,
                category = "Main",
                ingredients = listOf(
                    Ingredient("Chicken Breast", "4 pieces"),
                    Ingredient("Olive Oil", "2 tbsp"),
                    Ingredient("Garlic", "2 cloves"),
                    Ingredient("Salt", "1 tsp"),
                    Ingredient("Paprika", "1 tsp")
                )
            ),
            Recipe(
                id = 3,
                title = "Tomato Pasta",
                description = "Simple pasta with fresh tomato sauce",
                duration = "30 min",
                servings = 4,
                category = "Main",
                ingredients = listOf(
                    Ingredient("Pasta", "400g"),
                    Ingredient("Tomatoes", "500g"),
                    Ingredient("Garlic", "3 cloves"),
                    Ingredient("Olive Oil", "3 tbsp"),
                    Ingredient("Basil", "fresh leaves")
                )
            ),
            Recipe(
                id = 4,
                title = "Garlic Rice",
                description = "Fragrant garlic rice side dish",
                duration = "20 min",
                servings = 3,
                category = "Side",
                ingredients = listOf(
                    Ingredient("Rice", "2 cups"),
                    Ingredient("Garlic", "5 cloves"),
                    Ingredient("Butter", "2 tbsp"),
                    Ingredient("Salt", "1 tsp"),
                    Ingredient("Chicken Stock", "3 cups")
                )
            )
        )
    )

    val recipes: List<Recipe>
        get() {
            val base = if (showFavoritesOnly) _recipes.filter { it.isFavorite } else _recipes
            return if (searchQuery.isBlank()) base
                   else base.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }

    fun getRecipeById(id: Int): Recipe? {
        return _recipes.find { it.id == id }
    }

    fun toggleFavorite(recipe: Recipe) {
        _recipes = _recipes.map {
            if (it.id == recipe.id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }
}
