package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.uc3m.android.pockets_chef_app.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()

                val currentUser = auth.currentUser ?: return@launch
                val uid = currentUser.uid
                val userEmail = currentUser.email ?: email

                val initialProfile = UserProfile(
                    userId = uid,
                    email = userEmail,
                    name = "",
                    age = 18,
                    description = "",
                    level = "Beginner",
                    diet = emptyList(),
                    allergies = emptyList(),
                    favoriteRecipes = emptyList(),
                    pantryItemIds = emptyList(),
                    photoUrl = "",
                    favoriteCuisine = "",
                    createdAt = System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(uid)
                    .set(initialProfile)
                    .await()

                _signUpSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _loginSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun logout() {
        auth.signOut()
        _loginSuccess.value = false
        _signUpSuccess.value = false
    }

    fun clearNavigationFlags() {
        _loginSuccess.value = false
        _signUpSuccess.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}