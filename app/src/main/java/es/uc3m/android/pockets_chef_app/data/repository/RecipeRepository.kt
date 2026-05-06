package es.uc3m.android.pockets_chef_app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.map
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.dataObjects
class RecipeRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val recipeCollection = db.collection("recipes")
    private val usersCollection = db.collection("users")
    private val storage = FirebaseStorage.getInstance()

    suspend fun createRecipeForUser(recipe: Recipe, userId: String): Result<String> = try {
        val docRef = recipeCollection.document()
        val recipeId = docRef.id
        val recipeWithId = recipe.copy(id = recipeId)

        val batch = db.batch()
        batch.set(docRef, recipeWithId)
        batch.update(
            usersCollection.document(userId),
            "myRecipeIds",
            FieldValue.arrayUnion(recipeId)
        )
        batch.commit().await()

        Result.success(recipeId)
    } catch (e: Exception) {
        Result.failure(e)
    }



    suspend fun getRecipeById(recipeId: String): Result<Recipe?> = try {
        val snapshot = recipeCollection.document(recipeId).get().await()
        Result.success(snapshot.toObject<Recipe>())
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getRecipesByAuthor(authorId: String): Flow<List<Recipe>> {
        return recipeCollection
            .whereEqualTo("authorId", authorId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    document.toObject<Recipe>()?.copy(id = document.id)
                }
            }
    }

    fun getLatestPublicRecipes(): Flow<List<Recipe>> {
        return recipeCollection
            .whereEqualTo("isPublic", true)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    document.toObject<Recipe>()?.copy(id = document.id)
                }
            }
    }
    suspend fun uploadRecipeImage(userId: String, imageUri: Uri): Result<String> = try {
        val fileName = "recipes/${userId}/${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(fileName)

        ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()

        Result.success(downloadUrl)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getCookAIRecipes(authorId: String): Flow<List<Recipe>> {
        return recipeCollection
            .whereEqualTo("authorId", authorId)
            .whereEqualTo("source", "cookai")
            .dataObjects<Recipe>()
    }
}
