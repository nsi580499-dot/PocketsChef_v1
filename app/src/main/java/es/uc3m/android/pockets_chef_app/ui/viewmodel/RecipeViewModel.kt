package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.Ingredient
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.RecipeStep
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

    private val _recipesState = MutableStateFlow<List<Recipe>>(emptyList())
    val recipesState: StateFlow<List<Recipe>> = _recipesState.asStateFlow()

    private val _myRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val myRecipes: StateFlow<List<Recipe>> = _myRecipes.asStateFlow()

    private val _createRecipeSuccess = MutableStateFlow(false)
    val createRecipeSuccess: StateFlow<Boolean> = _createRecipeSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeRecipesAndFavorites()
        observeMyRecipes()
    }

    private fun observeRecipesAndFavorites() {
        val uid = auth.currentUser?.uid

        viewModelScope.launch {
            val recipesFlow = recipeRepository.getLatestPublicRecipes()
            val favoritesFlow = if (uid != null) {
                userRepository.getFavoriteRecipeIdsFlow(uid)
            } else {
                MutableStateFlow(emptySet())
            }

            combine(recipesFlow, favoritesFlow) { recipes, favIds ->
                recipes.map { recipe ->
                    recipe.copy(isFavorite = favIds.contains(recipe.id))
                }.sortedByDescending { it.createdAt }
            }.collect { combinedList ->
                _recipesState.value = combinedList
            }
        }
    }

    private fun observeMyRecipes() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            recipeRepository.getRecipesByAuthor(uid).collect { recipes ->
                _myRecipes.value = recipes.sortedByDescending { it.createdAt }
            }
        }
    }

    fun getRecipeById(id: String): Recipe? {
        return _recipesState.value.find { it.id == id } ?: _myRecipes.value.find { it.id == id }
    }

    fun toggleFavorite(recipe: Recipe) {
        val uid = auth.currentUser?.uid ?: return
        val newFavoriteStatus = !recipe.isFavorite

        viewModelScope.launch {
            userRepository.toggleFavoriteRecipe(uid, recipe.id, newFavoriteStatus)
        }
    }

    fun createRecipe(
        title: String,
        description: String,
        duration: String,
        servings: Int,
        category: String,
        ingredients: List<Ingredient>,
        steps: List<RecipeStep>,
        isPublic: Boolean
    ) {
        val currentUser = auth.currentUser ?: run {
            _errorMessage.value = "No user logged in"
            return
        }

        val recipe = Recipe(
            title = title,
            description = description,
            duration = duration,
            servings = servings,
            category = category,
            ingredients = ingredients,
            steps = steps,
            authorId = currentUser.uid,
            authorName = currentUser.displayName ?: currentUser.email?.substringBefore("@").orEmpty(),
            isPublic = isPublic
        )

        viewModelScope.launch {
            val result = recipeRepository.createRecipeForUser(recipe, currentUser.uid)
            if (result.isSuccess) {
                _createRecipeSuccess.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun clearCreateRecipeSuccess() {
        _createRecipeSuccess.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}