package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.uc3m.android.pockets_chef_app.data.model.ShoppingItem
import es.uc3m.android.pockets_chef_app.data.repository.ShoppingListRepository
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

    init {
        loadItems()
    }

    private fun loadItems() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
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
}