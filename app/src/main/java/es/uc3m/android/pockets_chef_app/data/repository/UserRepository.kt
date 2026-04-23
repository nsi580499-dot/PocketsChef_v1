package es.uc3m.android.pockets_chef_app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import es.uc3m.android.pockets_chef_app.data.model.User
import kotlinx.coroutines.flow.Flow
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

    suspend fun followUser(myUid: String, targetUid: String): Result<Unit> = try {
        val batch = db.batch()
        
        // Add to my following
        val followingRef = db.collection("users").document(myUid).collection("following").document(targetUid)
        batch.set(followingRef, mapOf("followedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
        
        // Add to target's followers
        val followersRef = db.collection("users").document(targetUid).collection("followers").document(myUid)
        batch.set(followersRef, mapOf("followedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
        
        // Increment counts
        batch.update(db.collection("users").document(myUid), "followingCount", com.google.firebase.firestore.FieldValue.increment(1))
        batch.update(db.collection("users").document(targetUid), "followersCount", com.google.firebase.firestore.FieldValue.increment(1))
        
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
