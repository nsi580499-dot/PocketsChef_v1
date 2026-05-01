package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // <-- ADDED THIS
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
import kotlinx.coroutines.tasks.await // <-- ADDED THIS
import android.net.Uri
import kotlinx.coroutines.flow.first

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

    private val _updateRecipeSuccess = MutableStateFlow(false)
    val updateRecipeSuccess = _updateRecipeSuccess.asStateFlow()

    private val _deleteRecipeSuccess = MutableStateFlow(false)
    val deleteRecipeSuccess = _deleteRecipeSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Expose current user ID so the UI can check ownership
    val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        observeRecipesAndFavorites()
        observeMyRecipes()
    }

    fun refreshRecipes() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                recipeRepository.getLatestPublicRecipes().collect { recipes ->
                    val favIds = userRepository.getFavoriteRecipeIdsFlow(uid).first()
                    _recipesState.value = recipes.map { recipe ->
                        recipe.copy(isFavorite = favIds.contains(recipe.id))
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    private var recipesJobStarted = false

    private fun observeRecipesAndFavorites() {
        if (recipesJobStarted) return
        recipesJobStarted = true

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
                }
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

        // Optimistically update UI immediately
        _recipesState.value = _recipesState.value.map {
            if (it.id == recipe.id) it.copy(isFavorite = newFavoriteStatus) else it
        }

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
        isPublic: Boolean,
        imageUrl: String = ""
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
            isPublic = isPublic,
            imageUrl = imageUrl
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

    // The update function
    fun updateRecipe(
        recipeId: String,
        title: String,
        description: String,
        duration: String,
        servings: Int,
        category: String,
        ingredients: List<Ingredient>,
        steps: List<RecipeStep>,
        isPublic: Boolean
    ) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()

                // Note: We don't update the authorId or createdAt fields
                val updates = mapOf(
                    "title" to title,
                    "description" to description,
                    "duration" to duration,
                    "servings" to servings,
                    "category" to category,
                    "ingredients" to ingredients,
                    "steps" to steps,
                    "isPublic" to isPublic
                )

                db.collection("recipes").document(recipeId).update(updates).await()
                _updateRecipeSuccess.value = true

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update recipe"
            }
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("recipes").document(recipeId).delete().await()
                _deleteRecipeSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete recipe"
            }
        }
    }

    fun createRecipeWithOptionalImage(
        title: String,
        description: String,
        duration: String,
        servings: Int,
        category: String,
        ingredients: List<Ingredient>,
        steps: List<RecipeStep>,
        isPublic: Boolean,
        imageUri: Uri?
    ) {
        val currentUser = auth.currentUser ?: run {
            _errorMessage.value = "No user logged in"
            return
        }

        viewModelScope.launch {
            try {
                val uploadedImageUrl = if (imageUri != null) {
                    val uploadResult = recipeRepository.uploadRecipeImage(currentUser.uid, imageUri)
                    if (uploadResult.isFailure) {
                        _errorMessage.value = uploadResult.exceptionOrNull()?.message
                        return@launch
                    }
                    uploadResult.getOrNull().orEmpty()
                } else {
                    ""
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
                    isPublic = isPublic,
                    imageUrl = uploadedImageUrl
                )

                val result = recipeRepository.createRecipeForUser(recipe, currentUser.uid)

                if (result.isSuccess) {
                    _createRecipeSuccess.value = true
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearCreateRecipeSuccess() {
        _createRecipeSuccess.value = false
    }

    fun clearUpdateRecipeSuccess() {
        _updateRecipeSuccess.value = false
    }

    fun clearDeleteRecipeSuccess() {
        _deleteRecipeSuccess.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}