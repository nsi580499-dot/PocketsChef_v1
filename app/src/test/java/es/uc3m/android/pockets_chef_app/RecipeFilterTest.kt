package es.uc3m.android.pockets_chef_app

import es.uc3m.android.pockets_chef_app.data.model.Recipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecipeFilterTest {

    private lateinit var recipes: List<Recipe>

    // Setup: create a sample list of recipes
    @Before
    fun setup() {
        recipes = listOf(
            Recipe(id = "1", title = "Pasta Carbonara", isFavorite = true, source = ""),
            Recipe(id = "2", title = "Chicken Salad", isFavorite = false, source = ""),
            Recipe(id = "3", title = "Pasta Bolognese", isFavorite = false, source = ""),
            Recipe(id = "4", title = "CookAI Omelette", isFavorite = false, source = "cookai")
        )
    }

    // Test 1: search by keyword filters correctly
    @Test
    fun searchFilter_returnsCorrectRecipes_forKeyword() {
        val query = "pasta"
        val result = recipes.filter { it.title.contains(query, ignoreCase = true) }

        assertEquals(2, result.size)
    }

    // Test 2: favorites filter returns only favorites
    @Test
    fun favoritesFilter_returnsOnlyFavoriteRecipes() {
        val result = recipes.filter { it.isFavorite }

        assertEquals(1, result.size)
        assertEquals("Pasta Carbonara", result.first().title)
    }

    // Test 3: CookAI filter returns only CookAI recipes
    @Test
    fun cookAIFilter_returnsOnlyCookAIRecipes() {
        val result = recipes.filter { it.source == "cookai" }

        assertEquals(1, result.size)
        assertEquals("CookAI Omelette", result.first().title)
    }

    // Test 4: empty search returns all non-CookAI recipes
    @Test
    fun emptySearch_returnsAllNormalRecipes() {
        val result = recipes.filter { it.source != "cookai" }

        assertEquals(3, result.size)
    }
}
