package es.uc3m.android.pockets_chef_app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import es.uc3m.android.pockets_chef_app.MainActivity
import es.uc3m.android.pockets_chef_app.R


object NotificationHelper {

    // Channel IDs
    const val EXPIRY_CHANNEL_ID = "expiry_channel"
    const val FOLLOWER_CHANNEL_ID = "follower_channel"

    // Notification IDs
    private const val EXPIRY_NOTIFICATION_ID = 1001
    private const val FOLLOWER_NOTIFICATION_ID = 1002

    // Create both notification channels
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Expiry channel — default importance
            val expiryChannel = NotificationChannel(
                EXPIRY_CHANNEL_ID,
                "Expiry Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for pantry items expiring soon"
            }

            // Follower channel — high importance (heads-up)
            val followerChannel = NotificationChannel(
                FOLLOWER_CHANNEL_ID,
                "New Followers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new followers"
            }

            notificationManager.createNotificationChannel(expiryChannel)
            notificationManager.createNotificationChannel(followerChannel)
        }
    }

    // Pending intent — opens app when notification is tapped
    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Send expiry notification
    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    fun sendExpiryNotification(context: Context, itemName: String, daysLeft: Int) {
        val title = "⚠️ Item Expiring Soon"
        val content = when (daysLeft) {
            0 -> "$itemName expires today!"
            1 -> "$itemName expires tomorrow!"
            else -> "$itemName expires in $daysLeft days"
        }

        val notification = NotificationCompat.Builder(context, EXPIRY_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_pocketschef)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getPendingIntent(context))
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(EXPIRY_NOTIFICATION_ID + itemName.hashCode(), notification)
        }
    }

    // Send follower notification
    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    fun sendFollowerNotification(context: Context, followerName: String) {
        val notification = NotificationCompat.Builder(context, FOLLOWER_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_pocketschef)
            .setContentTitle("👨‍🍳 You started to follow $followerName!!!")
            .setContentText("You can now see all of $followerName's recipes and cooking creations in your feed!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent(context))
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(FOLLOWER_NOTIFICATION_ID, notification)
        }
    }
}
