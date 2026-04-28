package es.uc3m.android.pockets_chef_app.notifications

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExpiryWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Result.success() // Not logged in, skip

        return try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("users")
                .document(uid)
                .collection("pantry")
                .get()
                .await()

            val now = System.currentTimeMillis()
            val twoDaysMs = 2 * 24 * 60 * 60 * 1000L

            // Check notification permission
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) return Result.success()

            for (doc in snapshot.documents) {
                val name = doc.getString("name") ?: continue
                val expiryDate = doc.getLong("expiryDate") ?: continue
                val daysLeft = ((expiryDate - now) / (1000 * 60 * 60 * 24)).toInt()

                if (daysLeft in 0..2) {
                    NotificationHelper.sendExpiryNotification(context, name, daysLeft)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val TAG = "ExpiryWorker"
    }
}
