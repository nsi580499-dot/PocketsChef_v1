package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.AppNotification
import es.uc3m.android.pockets_chef_app.data.repository.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: NotificationsRepository = NotificationsRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            repository.getNotificationsFlow(currentUser.uid).collectLatest { list ->
                _notifications.value = list
            }
        }
    }

    fun markAsRead(notificationId: String) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            val result = repository.markAsRead(currentUser.uid, notificationId)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
}