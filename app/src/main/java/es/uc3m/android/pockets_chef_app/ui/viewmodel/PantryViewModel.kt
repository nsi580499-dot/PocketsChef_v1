package es.uc3m.android.pockets_chef_app.ui.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.AppNotification
import es.uc3m.android.pockets_chef_app.data.model.PantryItem
import es.uc3m.android.pockets_chef_app.data.repository.NotificationsRepository
import es.uc3m.android.pockets_chef_app.data.repository.PantryRepository
import es.uc3m.android.pockets_chef_app.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PantryViewModel(
    private val repository: PantryRepository = PantryRepository(),
    private val notificationsRepository: NotificationsRepository = NotificationsRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _items = MutableStateFlow<List<PantryItem>>(emptyList())
    val itemsState: StateFlow<List<PantryItem>> = _items.asStateFlow()

    val items: List<PantryItem> get() = _items.value

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        startObservingPantry()
    }

    private fun startObservingPantry() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
//            repository.getPantryItems(currentUser.uid).collectLatest { list ->
//                _items.value = list
//            }
            repository.getPantryItems(currentUser.uid).collectLatest { list ->
                val currentTime = System.currentTimeMillis()
                // 5 days in milliseconds
                val fiveDaysInMillis = 5L * 24 * 60 * 60 * 1000

                // Map the list to update the 'isExpiringSoon' status dynamically
                val updatedList = list.map { item ->
                    val diff = item.expiryDate - currentTime
                    // Mark as true if it expires in less than 5 days
                    item.copy(isExpiringSoon = diff > 0 && diff < fiveDaysInMillis)
                }

                _items.value = updatedList
            }
        }
    }

    fun addItem(item: PantryItem, context: Context) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            val result = repository.addItem(currentUser.uid, item)

            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            } else {
                val daysLeft =
                    ((item.expiryDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()

                if (daysLeft <= 5) {
                    val title = "⚠️ Expiry Alert"
                    val message = when (daysLeft) {
                        0 -> "${item.name} expires today!"
                        1 -> "${item.name} expires tomorrow!"
                        else -> "${item.name} expires in $daysLeft days"
                    }

                    notificationsRepository.addNotification(
                        userUid = currentUser.uid,
                        notification = AppNotification(
                            title = title,
                            message = message,
                            timestamp = System.currentTimeMillis(),
                            type = "expiry",
                            read = false
                        )
                    )

                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        NotificationHelper.sendExpiryNotification(context, item.name, daysLeft)
                    }
                }
            }
        }
    }

    fun deleteItem(item: PantryItem) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            val result = repository.deleteItem(currentUser.uid, item.id)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
}