package io.track.habit.workers.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import io.track.habit.R
import io.track.habit.data.local.database.entities.Habit
import io.track.habit.data.local.database.entities.Habit.Companion.getName
import io.track.habit.ui.MainActivity

object StreakNotificationUtils {
    const val CHANNEL_ID = "streak_notifier_channel"
    const val NOTIFICATION_ID = 1

    fun Context.getNotificationManager() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    fun Context.createNotificationChannel() {
        val name = getString(R.string.notification_channel_name)
        val descriptionText = getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)

        val audioAttributes = AudioAttributes
            .Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        mChannel.description = descriptionText
        mChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)

        val notificationManager = getNotificationManager()
        notificationManager.createNotificationChannel(mChannel)
    }

    private fun Context.createBaseNotification(): NotificationCompat.Builder {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            // context =
            this,
            // requestCode =
            0,
            // intent =
            intent,
            // flags =
            PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.onboarding_step2)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    fun Context.createNotification(habits: Int): Notification {
        return createBaseNotification()
            .setContentTitle(resources.getQuantityString(R.plurals.streak_notification_title, habits))
            .setContentText(resources.getQuantityString(R.plurals.streak_notification_text, habits, habits))
            .build()
    }

    fun Context.createNotification(
        habit: Habit,
        isCensored: Boolean,
    ): Notification {
        return createBaseNotification()
            .setContentTitle(resources.getQuantityString(R.plurals.streak_notification_title, 1))
            .setContentText(
                resources.getQuantityString(R.plurals.streak_notification_text, 1, habit.getName(isCensored)),
            ).build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Context.deleteNotificationChannel(id: String) {
        val notificationManager = getNotificationManager()
        notificationManager.deleteNotificationChannel(id)
    }
}
