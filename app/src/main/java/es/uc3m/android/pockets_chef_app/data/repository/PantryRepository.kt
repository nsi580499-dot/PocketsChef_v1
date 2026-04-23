package es.uc3m.android.pockets_chef_app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import es.uc3m.android.pockets_chef_app.data.model.PantryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class PantryRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private fun getPantryCollection(userId: String) = 
        db.collection("users").document(userId).collection("pantry")

    suspend fun addItem(userId: String, item: PantryItem): Result<String> = try {
        val docRef = getPantryCollection(userId).document()
        val itemWithId = item.copy(id = docRef.id, userId = userId)
        docRef.set(itemWithId).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteItem(userId: String, itemId: String): Result<Unit> = try {
        getPantryCollection(userId).document(itemId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getPantryItems(userId: String): Flow<List<PantryItem>> {
        return getPantryCollection(userId).dataObjects<PantryItem>()
    }
}
