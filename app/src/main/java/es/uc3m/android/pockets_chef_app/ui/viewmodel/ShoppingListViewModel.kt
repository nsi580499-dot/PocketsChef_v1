package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.ShoppingItem
import es.uc3m.android.pockets_chef_app.data.repository.ShoppingListRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val repository: ShoppingListRepository = ShoppingListRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items.asStateFlow()

    private var shoppingJob: Job? = null
    private var currentObservedUid: String? = null

    init {
        refreshForCurrentUser()
    }

    // 🔥 ESTA ES LA CLAVE
    fun refreshForCurrentUser() {
        val uid = auth.currentUser?.uid

        if (currentObservedUid == uid) return

        shoppingJob?.cancel()
        currentObservedUid = uid
        _items.value = emptyList()

        if (uid != null) {
            startObservingShoppingList(uid)
        }
    }

    private fun startObservingShoppingList(uid: String) {
        shoppingJob = viewModelScope.launch {
            repository.getShoppingList(uid).collectLatest {
                _items.value = it
            }
        }
    }

    fun addItem(name: String, amount: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.addItem(uid, ShoppingItem(name = name, amount = amount))
        }
    }

    fun toggleItem(item: ShoppingItem) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.toggleItem(uid, item)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.deleteItem(uid, item.id)
        }
    }

    // 🔥 LIMPIAR AL LOGOUT
    fun clearData() {
        shoppingJob?.cancel()
        shoppingJob = null
        currentObservedUid = null
        _items.value = emptyList()
    }
}