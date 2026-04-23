package es.uc3m.android.pockets_chef_app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import es.uc3m.android.pockets_chef_app.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class RecipeRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val recipeCollection = db.collection("recipes")

    suspend fun createRecipe(recipe: Recipe): Result<String> = try {
        val docRef = recipeCollection.document()
        val recipeWithId = recipe.copy(id = docRef.id)
        docRef.set(recipeWithId).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getRecipeById(recipeId: String): Result<Recipe?> = try {
        val snapshot = recipeCollection.document(recipeId).get().await()
        Result.success(snapshot.toObject<Recipe>())
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Devuelve las recetas públicas. 
     * Hemos quitado el orderBy de Firestore para evitar errores de índice 
     * y asegurar que carguen siempre.
     */
    fun getLatestPublicRecipes(): Flow<List<Recipe>> {
        return recipeCollection
            .whereEqualTo("isPublic", true)
            .dataObjects<Recipe>()
    }
}
