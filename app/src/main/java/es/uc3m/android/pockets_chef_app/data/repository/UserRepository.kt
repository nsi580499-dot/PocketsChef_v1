package es.uc3m.android.pockets_chef_app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import es.uc3m.android.pockets_chef_app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val usersCollection = db.collection("users")

    suspend fun createUserProfile(user: User): Result<Unit> = try {
        usersCollection.document(user.uid).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUserProfile(uid: String): Result<User?> = try {
        val snapshot = usersCollection.document(uid).get().await()
        Result.success(snapshot.toObject<User>())
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getAllUsersFlow(): Flow<List<User>> {
        return usersCollection.limit(10).dataObjects<User>()
    }

    suspend fun updateUserProfile(user: User): Result<Unit> = try {
        usersCollection.document(user.uid).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun toggleFollowUser(myUid: String, targetUid: String, isFollowing: Boolean): Result<Unit> = try {
        val batch = db.batch()
        
        val followingRef = usersCollection.document(myUid).collection("following").document(targetUid)
        val followersRef = usersCollection.document(targetUid).collection("followers").document(myUid)
        
        if (isFollowing) {
            // Unfollow
            batch.delete(followingRef)
            batch.delete(followersRef)
            batch.update(usersCollection.document(myUid), "followingCount", com.google.firebase.firestore.FieldValue.increment(-1))
            batch.update(usersCollection.document(targetUid), "followersCount", com.google.firebase.firestore.FieldValue.increment(-1))
        } else {
            // Follow
            batch.set(followingRef, mapOf("followedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
            batch.set(followersRef, mapOf("followedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
            batch.update(usersCollection.document(myUid), "followingCount", com.google.firebase.firestore.FieldValue.increment(1))
            batch.update(usersCollection.document(targetUid), "followersCount", com.google.firebase.firestore.FieldValue.increment(1))
        }
        
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun isFollowingFlow(myUid: String, targetUid: String): Flow<Boolean> {
        return usersCollection.document(myUid).collection("following").document(targetUid)
            .snapshots()
            .map { it.exists() }
    }

    // --- FAVORITES LOGIC ---

    suspend fun toggleFavoriteRecipe(userId: String, recipeId: String, isFavorite: Boolean): Result<Unit> = try {
        val favRef = usersCollection.document(userId).collection("favorites").document(recipeId)
        if (isFavorite) {
            favRef.set(mapOf("addedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp())).await()
        } else {
            favRef.delete().await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getFavoriteRecipeIdsFlow(userId: String): Flow<Set<String>> {
        return usersCollection.document(userId).collection("favorites")
            .snapshots()
            .map { snapshot -> snapshot.documents.map { it.id }.toSet() }
    }
}
