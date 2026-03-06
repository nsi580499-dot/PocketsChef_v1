package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import es.uc3m.android.pockets_chef_app.data.model.PantryItem

class PantryViewModel : ViewModel() {

    var items by mutableStateOf<List<PantryItem>>(emptyList())
        private set

    fun addItem(item: PantryItem) {
        val newId = (items.maxOfOrNull { it.id } ?: 0) + 1
        items = items + item.copy(id = newId)
    }

    fun deleteItem(item: PantryItem) {
        items = items - item
    }
}
