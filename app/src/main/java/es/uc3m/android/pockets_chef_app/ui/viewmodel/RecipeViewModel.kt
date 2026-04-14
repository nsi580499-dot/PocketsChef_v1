package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import es.uc3m.android.pockets_chef_app.data.model.Ingredient
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.RecipeStep

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
                ),
                steps = listOf(
                    RecipeStep(1, "Crack the eggs into a bowl and whisk with milk, salt, and pepper until combined."),
                    RecipeStep(2, "Melt butter in a non-stick skillet over medium-low heat."),
                    RecipeStep(3, "Pour in the egg mixture and let it sit for a few seconds."),
                    RecipeStep(4, "Gently pull the eggs across the pan with a spatula to form large curds."),
                    RecipeStep(5, "Remove from heat when the eggs are still slightly moist.")
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
                ),
                steps = listOf(
                    RecipeStep(1, "Season the chicken breasts with olive oil, minced garlic, salt, and paprika."),
                    RecipeStep(2, "Preheat your grill or grill pan to medium-high heat."),
                    RecipeStep(3, "Grill the chicken for 6-8 minutes per side until cooked through (internal temp 75°C)."),
                    RecipeStep(4, "Let the chicken rest for 5 minutes before slicing.")
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
                ),
                steps = listOf(
                    RecipeStep(1, "Boil a large pot of salted water and cook the pasta according to package directions."),
                    RecipeStep(2, "Meanwhile, heat olive oil in a pan and sauté minced garlic until fragrant."),
                    RecipeStep(3, "Add chopped tomatoes and simmer for 15 minutes until thickened."),
                    RecipeStep(4, "Drain the pasta and toss with the tomato sauce and fresh basil.")
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
                ),
                steps = listOf(
                    RecipeStep(1, "Rinse the rice until the water runs clear."),
                    RecipeStep(2, "Melt butter in a pot and sauté minced garlic until golden brown."),
                    RecipeStep(3, "Add the rice and stir to coat with butter for 1-2 minutes."),
                    RecipeStep(4, "Pour in the chicken stock and salt, bring to a boil, then cover and simmer for 15-18 minutes.")
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
