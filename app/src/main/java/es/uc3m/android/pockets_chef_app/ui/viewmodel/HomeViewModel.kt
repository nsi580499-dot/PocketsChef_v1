package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.PantryItem
import es.uc3m.android.pockets_chef_app.data.model.User
import es.uc3m.android.pockets_chef_app.data.repository.PantryRepository
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val pantryRepository: PantryRepository = PantryRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _otherChefs = MutableStateFlow<List<User>>(emptyList())
    val otherChefs: StateFlow<List<User>> = _otherChefs.asStateFlow()

    private val _expiringItemsCount = MutableStateFlow(0)
    val expiringItemsCount: StateFlow<Int> = _expiringItemsCount.asStateFlow()

    init {
        loadOtherChefs()
        observeExpiringItems()
    }

    private fun loadOtherChefs() {
        viewModelScope.launch {
            userRepository.getAllUsersFlow().collectLatest { users ->
                _otherChefs.value = users
            }
        }
    }

    private fun observeExpiringItems() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            pantryRepository.getPantryItems(uid).collectLatest { items ->
                _expiringItemsCount.value = items.count { it.isExpiringSoon }
            }
        }
    }
}
