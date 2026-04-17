package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import es.uc3m.android.pockets_chef_app.data.model.PantryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth

class PantryViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _items = MutableStateFlow<List<PantryItem>>(emptyList())
    val items: StateFlow<List<PantryItem>> = _items.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchItems()
    }

    fun fetchItems() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val snapshot = firestore
                    .collection("pantry_items")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                val itemList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PantryItem::class.java)?.copy(id = doc.id)
                }

                _items.value = itemList
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun addItem(item: PantryItem) {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val itemToSave = item.copy(
                    userId = currentUser.uid
                )

                firestore.collection("pantry_items")
                    .add(itemToSave)
                    .await()

                fetchItems()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteItem(item: PantryItem) {
        if (item.id.isBlank()) return

        viewModelScope.launch {
            try {
                firestore.collection("pantry_items")
                    .document(item.id)
                    .delete()
                    .await()

                fetchItems()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}