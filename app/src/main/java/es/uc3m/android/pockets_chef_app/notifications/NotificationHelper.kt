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

    const val EXPIRY_CHANNEL_ID = "expiry_channel"
    const val FOLLOWER_CHANNEL_ID = "follower_channel"

    private const val EXPIRY_NOTIFICATION_ID = 1001
    private const val FOLLOWER_NOTIFICATION_ID = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val expiryChannel = NotificationChannel(
                EXPIRY_CHANNEL_ID,
                context.getString(R.string.channel_expiry_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_expiry_desc)
            }

            val followerChannel = NotificationChannel(
                FOLLOWER_CHANNEL_ID,
                context.getString(R.string.channel_follower_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_follower_desc)
            }

            notificationManager.createNotificationChannel(expiryChannel)
            notificationManager.createNotificationChannel(followerChannel)
        }
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    fun sendExpiryNotification(context: Context, itemName: String, daysLeft: Int) {
        val title = context.getString(R.string.notification_expiry_title)
        val content = when (daysLeft) {
            0 -> context.getString(R.string.notification_expiry_today, itemName)
            1 -> context.getString(R.string.notification_expiry_tomorrow, itemName)
            else -> context.getString(R.string.notification_expiry_days, itemName, daysLeft)
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

    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    fun sendFollowerNotification(context: Context, followedChefName: String) {
        val notification = NotificationCompat.Builder(context, FOLLOWER_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_pocketschef)
            .setContentTitle(context.getString(R.string.notification_follower_title))
            .setContentText(context.getString(R.string.notification_follower_msg, followedChefName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent(context))
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(FOLLOWER_NOTIFICATION_ID, notification)
        }
    }
}
