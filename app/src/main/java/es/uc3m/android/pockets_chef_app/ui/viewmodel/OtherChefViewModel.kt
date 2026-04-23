package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.User
import es.uc3m.android.pockets_chef_app.data.repository.RecipeRepository
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OtherChefViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val recipeRepository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val _chefProfile = MutableStateFlow<User?>(null)
    val chefProfile: StateFlow<User?> = _chefProfile.asStateFlow()

    private val _chefRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val chefRecipes: StateFlow<List<Recipe>> = _chefRecipes.asStateFlow()

    fun loadChef(userId: String) {
        viewModelScope.launch {
            val result = userRepository.getUserProfile(userId)
            if (result.isSuccess) {
                _chefProfile.value = result.getOrNull()
            }
            // In a real app, we'd have a recipeRepository.getRecipesByAuthor(userId)
            // For now, we filter from all recipes
            recipeRepository.getLatestPublicRecipes().collectLatest { allRecipes ->
                _chefRecipes.value = allRecipes.filter { it.authorId == userId }
            }
        }
    }

    fun followChef(myUid: String, targetUid: String) {
        viewModelScope.launch {
            userRepository.followUser(myUid, targetUid)
            // Reload profile to update counts
            val result = userRepository.getUserProfile(targetUid)
            if (result.isSuccess) {
                _chefProfile.value = result.getOrNull()
            }
        }
    }
}
