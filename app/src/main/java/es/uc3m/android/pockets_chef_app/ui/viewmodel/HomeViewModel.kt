package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uc3m.android.pockets_chef_app.data.model.User
import es.uc3m.android.pockets_chef_app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(private val userRepository: UserRepository = UserRepository()) : ViewModel() {

    private val _otherChefs = MutableStateFlow<List<User>>(emptyList())
    val otherChefs: StateFlow<List<User>> = _otherChefs.asStateFlow()

    init {
        loadOtherChefs()
    }

    private fun loadOtherChefs() {
        viewModelScope.launch {
            userRepository.getAllUsersFlow().collectLatest { users ->
                _otherChefs.value = users
            }
        }
    }
}
