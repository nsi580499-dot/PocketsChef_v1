package es.uc3m.android.pockets_chef_app.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.uc3m.android.pockets_chef_app.data.model.Ingredient
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.RecipeStep
import es.uc3m.android.pockets_chef_app.data.repository.RecipeRepository
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Job

class RecipeViewModel(
    private val recipeRepository: RecipeRepository = RecipeRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private var recipesJob: Job? = null
    private var myRecipesJob: Job? = null
    private var currentObservedUid: String? = null

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
    private val _isLoadingRecipes = MutableStateFlow(true)
    val isLoadingRecipes: StateFlow<Boolean> = _isLoadingRecipes.asStateFlow()
    val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        refreshForCurrentUser()
    }

    fun refreshForCurrentUser() {
        val uid = auth.currentUser?.uid

        if (currentObservedUid == uid) return

        recipesJob?.cancel()
        myRecipesJob?.cancel()

        currentObservedUid = uid
        _recipesState.value = emptyList()
        _myRecipes.value = emptyList()
        _isLoadingRecipes.value = true

        searchQuery = ""
        showFavoritesOnly = false

        observeRecipesAndFavorites(uid)
        observeMyRecipes(uid)
    }

    private fun observeRecipesAndFavorites(uid: String?) {
        viewModelScope.launch {
            val recipesFlow = recipeRepository.getLatestPublicRecipes()
            val favoritesFlow = if (uid != null) {
                userRepository.getFavoriteRecipeIdsFlow(uid)
            } else {
                flowOf(emptySet())
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

    private fun observeMyRecipes(uid: String?) {
        if (uid == null) {
            _myRecipes.value = emptyList()
            return
        }

        viewModelScope.launch {
            val recipesFlow = recipeRepository.getRecipesByAuthor(uid)
            val favoritesFlow = userRepository.getFavoriteRecipeIdsFlow(uid)

            combine(recipesFlow, favoritesFlow) { recipes, favIds ->
                recipes.map { recipe ->
                    recipe.copy(isFavorite = favIds.contains(recipe.id))
                }.sortedByDescending { it.createdAt }
            }.collect { combined ->
                _myRecipes.value = combined
            }
        }
    }

    fun getRecipeById(id: String): Recipe? {
        return _recipesState.value.find { it.id == id } ?: _myRecipes.value.find { it.id == id }
    }

    fun toggleFavorite(recipe: Recipe) {
        val uid = auth.currentUser?.uid ?: return
        val newFavoriteStatus = !recipe.isFavorite

        // Optimistically update UI immediately in both states
        _recipesState.value = _recipesState.value.map {
            if (it.id == recipe.id) it.copy(isFavorite = newFavoriteStatus) else it
        }
        _myRecipes.value = _myRecipes.value.map {
            if (it.id == recipe.id) it.copy(isFavorite = newFavoriteStatus) else it
        }

        viewModelScope.launch {
            userRepository.toggleFavoriteRecipe(uid, recipe.id, newFavoriteStatus)
        }
    }

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
        val updateLocally = { recipe: Recipe ->
            if (recipe.id == recipeId) {
                recipe.copy(
                    title = title,
                    description = description,
                    duration = duration,
                    servings = servings,
                    category = category,
                    ingredients = ingredients,
                    steps = steps,
                    isPublic = isPublic
                )
            } else {
                recipe
            }
        }

        _recipesState.value = _recipesState.value.map(updateLocally)
        _myRecipes.value = _myRecipes.value.map(updateLocally)

        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
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
        _recipesState.value = _recipesState.value.filter { it.id != recipeId }
        _myRecipes.value = _myRecipes.value.filter { it.id != recipeId }

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
                    val newRecipeId = result.getOrNull() ?: ""

                    val finalRecipe = recipe.copy(id = newRecipeId)

                    if (isPublic) {
                        _recipesState.value = listOf(finalRecipe) + _recipesState.value
                    }
                    _myRecipes.value = listOf(finalRecipe) + _myRecipes.value

                    _createRecipeSuccess.value = true
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearCreateRecipeSuccess() { _createRecipeSuccess.value = false }
    fun clearUpdateRecipeSuccess() { _updateRecipeSuccess.value = false }
    fun clearDeleteRecipeSuccess() { _deleteRecipeSuccess.value = false }
    fun clearError() { _errorMessage.value = null }

    fun clearData() {
        currentObservedUid = null
        searchQuery = ""
        showFavoritesOnly = false
        _recipesState.value = emptyList()
        _myRecipes.value = emptyList()
        _createRecipeSuccess.value = false
        _updateRecipeSuccess.value = false
        _deleteRecipeSuccess.value = false
        _errorMessage.value = null
    }
}
