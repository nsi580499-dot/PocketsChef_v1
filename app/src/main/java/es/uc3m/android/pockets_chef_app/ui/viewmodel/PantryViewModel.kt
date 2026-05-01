package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.PantryItem
import es.uc3m.android.pockets_chef_app.data.repository.PantryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import es.uc3m.android.pockets_chef_app.notifications.NotificationHelper

class PantryViewModel(private val repository: PantryRepository = PantryRepository()) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    
    private val _items = MutableStateFlow<List<PantryItem>>(emptyList())
    val itemsState: StateFlow<List<PantryItem>> = _items.asStateFlow()

    // Compatibility property for current UI logic (List instead of StateFlow)
    val items: List<PantryItem> get() = _items.value

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        startObservingPantry()
    }

    private fun startObservingPantry() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            repository.getPantryItems(currentUser.uid).collectLatest { list ->
                _items.value = list
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
                // Fire notification immediately if expiring soon
                val daysLeft = ((item.expiryDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                if (daysLeft <= 2) {
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


