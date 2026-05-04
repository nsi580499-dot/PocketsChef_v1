package es.uc3m.android.pockets_chef_app.data.repository

import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import es.uc3m.android.pockets_chef_app.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ShoppingListRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun getCollection(userId: String) =
        db.collection("users").document(userId).collection("shopping_list")

    fun getShoppingList(userId: String): Flow<List<ShoppingItem>> {
        return getCollection(userId).dataObjects<ShoppingItem>()
    }

    suspend fun addItem(userId: String, item: ShoppingItem): Result<Unit> = try {
        val docRef = getCollection(userId).document()
        val itemWithId = item.copy(id = docRef.id, userId = userId)
        docRef.set(itemWithId).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun toggleItem(userId: String, item: ShoppingItem): Result<Unit> = try {
        getCollection(userId).document(item.id)
            .update("isChecked", !item.isChecked).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }


    suspend fun deleteItem(userId: String, itemId: String): Result<Unit> = try {
        getCollection(userId).document(itemId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}