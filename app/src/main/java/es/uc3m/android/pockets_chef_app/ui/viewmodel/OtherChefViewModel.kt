package es.uc3m.android.pockets_chef_app.ui.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.User
import es.uc3m.android.pockets_chef_app.data.repository.RecipeRepository
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import es.uc3m.android.pockets_chef_app.notifications.NotificationHelper
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

            if (myUid == userId) {
                _isFollowing.value = false
            } else {
                userRepository.isFollowingFlow(myUid, userId).collectLatest {
                    _isFollowing.value = it
                }
            }
        }

        viewModelScope.launch {
            recipeRepository.getLatestPublicRecipes().collectLatest { allRecipes ->
                _chefRecipes.value = allRecipes.filter { it.authorId == userId }
            }
        }
    }

    fun toggleFollow(myUid: String, targetUid: String, context: Context) {
        if (myUid == targetUid) return

        viewModelScope.launch {
            val currentStatus = _isFollowing.value
            userRepository.toggleFollowUser(myUid, targetUid, currentStatus)

            // Send notification only when following (not unfollowing)
            if (!currentStatus) {
                val myProfile = userRepository.getUserProfile(myUid).getOrNull()
                val myName = myProfile?.displayName ?: "Someone"

                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                val targetProfile = userRepository.getUserProfile(targetUid).getOrNull()
                val targetName = targetProfile?.displayName ?: "this Chef"
                if (hasPermission) {
                    NotificationHelper.sendFollowerNotification(
                        context = context,
                        followerName = targetName
                    )
                }
            }

            val result = userRepository.getUserProfile(targetUid)
            if (result.isSuccess) {
                _chefProfile.value = result.getOrNull()
            }
        }
    }
}