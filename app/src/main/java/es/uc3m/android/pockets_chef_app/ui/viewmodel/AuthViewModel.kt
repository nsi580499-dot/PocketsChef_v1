package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.User
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(val userRepository: UserRepository = UserRepository()) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun signUp(email: String, password: String, displayName: String = "") {
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = email,
                        displayName = displayName.ifBlank { email.substringBefore("@") },
                        profileCompleted = false
                    )
                    userRepository.createUserProfile(newUser)
                    _authSuccess.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authSuccess.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    fun clearNavigationFlags() {
        _authSuccess.value = false
    }
}
