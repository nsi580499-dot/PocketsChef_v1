package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.uc3m.android.pockets_chef_app.data.model.User
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Job

class UserProfileViewModel(
    private val userRepository: UserRepository = UserRepository()) : ViewModel()
{

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var profileJob: Job? = null
    private var currentObservedUid: String? = null

    private val _profile = MutableStateFlow(User())
    val profile: StateFlow<User> = _profile.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = auth.currentUser?.uid

        if (currentObservedUid == uid) return

        profileJob?.cancel()
        currentObservedUid = uid
        _profile.value = User()

        if (uid == null) return

        profileJob = viewModelScope.launch {
            try {
                userRepository.getUserProfileFlow(uid).collect { loadedProfile ->
                    _profile.value = loadedProfile ?: User()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
    fun saveProfile(
        name: String,
        description: String,
        level: String,
        photoUrl: String,
        diet: List<String>,
        favoriteCuisine: String
    ) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid
        val email = currentUser.email ?: ""

        viewModelScope.launch {
            try {
                val updatedProfile = User(
                    uid = uid,
                    displayName = name,
                    email = email,
                    bio = description,
                    profileImageUrl = photoUrl,
                    cookingLevel = level,
                    dietaryPreferences = diet,
                    favoriteCuisine = favoriteCuisine,
                    myRecipeIds = _profile.value.myRecipeIds,
                    followersCount = _profile.value.followersCount,
                    followingCount = _profile.value.followingCount,
                    profileCompleted = true
                )

                firestore.collection("users")
                    .document(uid)
                    .set(updatedProfile)
                    .await()

                _profile.value = updatedProfile
                _saveSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }

    fun clearData() {
        profileJob?.cancel()
        profileJob = null
        currentObservedUid = null
        _profile.value = User()
        _saveSuccess.value = false
        _errorMessage.value = null
    }
}