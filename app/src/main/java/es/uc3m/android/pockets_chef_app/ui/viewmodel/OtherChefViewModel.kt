package es.uc3m.android.pockets_chef_app.ui.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uc3m.android.pockets_chef_app.data.model.AppNotification
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import es.uc3m.android.pockets_chef_app.data.model.User
import es.uc3m.android.pockets_chef_app.data.repository.NotificationsRepository
import es.uc3m.android.pockets_chef_app.data.repository.RecipeRepository
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import es.uc3m.android.pockets_chef_app.notifications.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OtherChefViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val recipeRepository: RecipeRepository = RecipeRepository(),
    private val notificationsRepository: NotificationsRepository = NotificationsRepository()
) : ViewModel() {

    private val _chefProfile = MutableStateFlow<User?>(null)
    val chefProfile: StateFlow<User?> = _chefProfile.asStateFlow()

    private val _chefRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val chefRecipes: StateFlow<List<Recipe>> = _chefRecipes.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _followActionInProgress = MutableStateFlow(false)
    val followActionInProgress: StateFlow<Boolean> = _followActionInProgress.asStateFlow()

    private val _followStateLoaded = MutableStateFlow(false)
    val followStateLoaded: StateFlow<Boolean> = _followStateLoaded.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Estado visual temporal para evitar el rebote del botón
    private val _followUiOverride = MutableStateFlow<Boolean?>(null)
    val followUiOverride: StateFlow<Boolean?> = _followUiOverride.asStateFlow()

    private var followingJob: Job? = null
    private var recipesJob: Job? = null

    fun loadChef(myUid: String, userId: String) {
        viewModelScope.launch {
            val result = userRepository.getUserProfile(userId)
            if (result.isSuccess) {
                _chefProfile.value = result.getOrNull()
            }
        }

        recipesJob?.cancel()
        recipesJob = viewModelScope.launch {
            recipeRepository.getLatestPublicRecipes().collectLatest { allRecipes ->
                _chefRecipes.value = allRecipes.filter { it.authorId == userId }
            }
        }

        if (myUid != userId) {
            _followStateLoaded.value = false
            _followUiOverride.value = null

            followingJob?.cancel()
            followingJob = viewModelScope.launch {
                userRepository.isFollowingFlow(myUid, userId).collectLatest { following ->
                    _isFollowing.value = following
                    _followStateLoaded.value = true

                    // Si Firestore ya ha llegado al valor esperado, quitamos el override visual
                    val override = _followUiOverride.value
                    if (override != null && override == following) {
                        _followUiOverride.value = null
                    }
                }
            }
        }
    }

    fun toggleFollow(myUid: String, targetUid: String, context: Context) {
        if (myUid == targetUid) return
        if (_followActionInProgress.value) return
        if (!_followStateLoaded.value) return

        viewModelScope.launch {
            _followActionInProgress.value = true
            _errorMessage.value = null

            val wasFollowing = userRepository.isFollowingNow(myUid, targetUid)

            val result = userRepository.toggleFollowUser(
                myUid = myUid,
                targetUid = targetUid,
                isCurrentlyFollowing = wasFollowing
            )

            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
                android.util.Log.e("FOLLOW_DEBUG", "ViewModel toggle failed", result.exceptionOrNull())
                _followActionInProgress.value = false
                return@launch
            }

            if (!wasFollowing) {
                val myProfile = userRepository.getUserProfile(myUid).getOrNull()
                val followerName = myProfile?.displayName?.takeIf { it.isNotBlank() } ?: "A chef"

                notificationsRepository.addNotification(
                    userUid = targetUid,
                    notification = AppNotification(
                        title = "👨‍🍳 New follower!",
                        message = "$followerName started following you.",
                        timestamp = System.currentTimeMillis(),
                        type = "follower",
                        read = false,
                        actorUid = myUid,
                        actorName = followerName
                    )
                )
            }

            val updatedProfile = userRepository.getUserProfile(targetUid)
            if (updatedProfile.isSuccess) {
                _chefProfile.value = updatedProfile.getOrNull()
            }

            _followActionInProgress.value = false
        }
    }
}