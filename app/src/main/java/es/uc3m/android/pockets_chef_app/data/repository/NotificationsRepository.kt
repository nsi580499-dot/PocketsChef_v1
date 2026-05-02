package es.uc3m.android.pockets_chef_app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import es.uc3m.android.pockets_chef_app.data.model.AppNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class NotificationsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun notificationsCollection(userUid: String) =
        db.collection("users")
            .document(userUid)
            .collection("notifications")

    fun getNotificationsFlow(userUid: String): Flow<List<AppNotification>> {
        return notificationsCollection(userUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .dataObjects<AppNotification>()
    }

    suspend fun addNotification(
        userUid: String,
        notification: AppNotification
    ): Result<Unit> {
        return try {
            notificationsCollection(userUid)
                .add(notification)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(
        userUid: String,
        notificationId: String
    ): Result<Unit> {
        return try {
            notificationsCollection(userUid)
                .document(notificationId)
                .update("read", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}