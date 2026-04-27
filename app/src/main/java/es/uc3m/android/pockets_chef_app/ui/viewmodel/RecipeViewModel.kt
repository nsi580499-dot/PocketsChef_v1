package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.repository.RecipeRepository
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipeRepository: RecipeRepository = RecipeRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    var searchQuery by mutableStateOf("")
    var showFavoritesOnly by mutableStateOf(false)

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())

    // Combined state: Recipes with the correct isFavorite flag
    private val _recipesState = MutableStateFlow<List<Recipe>>(emptyList())
    val recipesState: StateFlow<List<Recipe>> = _recipesState.asStateFlow()

    init {
        observeRecipesAndFavorites()
    }

    private fun observeRecipesAndFavorites() {
        val uid = auth.currentUser?.uid
        
        viewModelScope.launch {
            // Flow for all public recipes
            val recipesFlow = recipeRepository.getLatestPublicRecipes()
            
            // Flow for user favorites (if logged in)
            val favoritesFlow = if (uid != null) {
                userRepository.getFavoriteRecipeIdsFlow(uid)
            } else {
                MutableStateFlow(emptySet())
            }

            // Combine both flows
            combine(recipesFlow, favoritesFlow) { recipes, favIds ->
                recipes.map { recipe ->
                    recipe.copy(isFavorite = favIds.contains(recipe.id))
                }.sortedByDescending { it.createdAt }
            }.collect { combinedList ->
                _recipesState.value = combinedList
            }
        }
    }

    fun getRecipeById(id: String): Recipe? {
        return _recipesState.value.find { it.id == id }
    }

    fun toggleFavorite(recipe: Recipe) {
        val uid = auth.currentUser?.uid ?: return
        val newFavoriteStatus = !recipe.isFavorite
        
        viewModelScope.launch {
            userRepository.toggleFavoriteRecipe(uid, recipe.id, newFavoriteStatus)
            // The UI will update automatically because we are observing the favorites flow
        }
    }
}
