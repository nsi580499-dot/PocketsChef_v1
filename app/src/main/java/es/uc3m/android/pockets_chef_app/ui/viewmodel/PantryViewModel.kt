package es.uc3m.android.pockets_chef_app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import es.uc3m.android.pockets_chef_app.data.model.PantryItem

class PantryViewModel : ViewModel() {

    var items by mutableStateOf<List<PantryItem>>(
        listOf(
            PantryItem(
                id = 1,
                name = "Milk",
                quantity = "1",
                unit = "L",
                category = "Dairy",
                expiryDate = System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000, // 2 days
                isExpiringSoon = true
            ),
            PantryItem(
                id = 2,
                name = "Chicken Breast",
                quantity = "500",
                unit = "g",
                category = "Meat",
                expiryDate = System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000, // 1 day
                isExpiringSoon = true
            ),
            PantryItem(
                id = 3,
                name = "Carrots",
                quantity = "5",
                unit = "units",
                category = "Vegetables",
                expiryDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000, // 7 days
                isExpiringSoon = false
            ),
            PantryItem(
                id = 4,
                name = "Pasta",
                quantity = "1",
                unit = "kg",
                category = "Grains",
                expiryDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000, // 30 days
                isExpiringSoon = false
            ),
            PantryItem(
                id = 5,
                name = "Ketchup",
                quantity = "1",
                unit = "bottle",
                category = "Condiments",
                expiryDate = System.currentTimeMillis() + 60 * 24 * 60 * 60 * 1000, // 60 days
                isExpiringSoon = false
            )
        )
    )
        private set

    fun addItem(item: PantryItem) {
        val newId = (items.maxOfOrNull { it.id } ?: 0) + 1
        items = items + item.copy(id = newId)
    }

    fun deleteItem(item: PantryItem) {
        items = items - item
    }
}
