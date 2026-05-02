package es.uc3m.android.pockets_chef_app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import es.uc3m.android.pockets_chef_app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersCollection = db.collection("users")

    suspend fun createUserProfile(user: User): Result<Unit> = try {
        usersCollection.document(user.uid).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getUserProfileFlow(uid: String): Flow<User?> {
        return usersCollection.document(uid)
            .snapshots()
            .map { snapshot -> snapshot.toObject<User>() }
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

    suspend fun isFollowingNow(myUid: String, targetUid: String): Boolean {
        return try {
            usersCollection
                .document(myUid)
                .collection("following")
                .document(targetUid)
                .get()
                .await()
                .exists()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun toggleFollowUser(
        myUid: String,
        targetUid: String,
        isCurrentlyFollowing: Boolean
    ): Result<Unit> {
        return try {
            if (myUid == targetUid) return Result.success(Unit)

            val followingRef = usersCollection
                .document(myUid)
                .collection("following")
                .document(targetUid)

            val followersRef = usersCollection
                .document(targetUid)
                .collection("followers")
                .document(myUid)

            val myUserRef = usersCollection.document(myUid)
            val targetUserRef = usersCollection.document(targetUid)

            if (isCurrentlyFollowing) {
                followingRef.delete().await()
                followersRef.delete().await()

                myUserRef.update("followingCount", FieldValue.increment(-1)).await()
                targetUserRef.update("followersCount", FieldValue.increment(-1)).await()
            } else {
                followingRef.set(
                    mapOf(
                        "uid" to targetUid,
                        "followedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                followersRef.set(
                    mapOf(
                        "uid" to myUid,
                        "followedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                myUserRef.update("followingCount", FieldValue.increment(1)).await()
                targetUserRef.update("followersCount", FieldValue.increment(1)).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FOLLOW_DEBUG", "toggleFollowUser failed", e)
            Result.failure(e)
        }
    }

    fun isFollowingFlow(myUid: String, targetUid: String): Flow<Boolean> {
        return usersCollection
            .document(myUid)
            .collection("following")
            .document(targetUid)
            .snapshots()
            .map { snapshot -> snapshot.exists() }
    }

    suspend fun toggleFavoriteRecipe(
        userId: String,
        recipeId: String,
        isFavorite: Boolean
    ): Result<Unit> = try {
        val favRef = usersCollection.document(userId)
            .collection("favorites")
            .document(recipeId)

        if (isFavorite) {
            favRef.set(mapOf("addedAt" to FieldValue.serverTimestamp())).await()
        } else {
            favRef.delete().await()
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getFavoriteRecipeIdsFlow(userId: String): Flow<Set<String>> {
        return usersCollection.document(userId)
            .collection("favorites")
            .snapshots()
            .map { snapshot -> snapshot.documents.map { it.id }.toSet() }
    }
}