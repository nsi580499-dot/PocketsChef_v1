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

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    fun loadChef(myUid: String, userId: String) {
        viewModelScope.launch {
            val result = userRepository.getUserProfile(userId)
            if (result.isSuccess) {
                _chefProfile.value = result.getOrNull()
            }
            
            // Observe following status
            userRepository.isFollowingFlow(myUid, userId).collectLatest {
                _isFollowing.value = it
            }
        }
        
        viewModelScope.launch {
            recipeRepository.getLatestPublicRecipes().collectLatest { allRecipes ->
                _chefRecipes.value = allRecipes.filter { it.authorId == userId }
            }
        }
    }

    fun toggleFollow(myUid: String, targetUid: String) {
        viewModelScope.launch {
            val currentStatus = _isFollowing.value
            userRepository.toggleFollowUser(myUid, targetUid, currentStatus)
            // Profile counts will be updated when the profile document changes in Firestore if we add snapshots
            // For now, reload manually
            val result = userRepository.getUserProfile(targetUid)
            if (result.isSuccess) {
                _chefProfile.value = result.getOrNull()
            }
        }
    }
}
