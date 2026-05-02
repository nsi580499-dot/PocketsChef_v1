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
        val myUid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            userRepository.getAllUsersFlow().collectLatest { users ->
                _otherChefs.value = users.filter { it.uid != myUid }
            }
        }
    }

    private fun observeExpiringItems() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            pantryRepository.getPantryItems(uid).collectLatest { items ->
                val currentTime = System.currentTimeMillis()
                // Define 5 days in milliseconds: 5 days * 24h * 60m * 60s * 1000ms
                val fiveDaysInMillis = 5L * 24 * 60 * 60 * 1000

                val count = items.count { item ->
                    val diff = item.expiryDate - currentTime
                    // Item is expiring soon if it expires in less than 5 days
                    diff > 0 && diff < fiveDaysInMillis
                }

                _expiringItemsCount.value = count
            }
        }
    }
}
