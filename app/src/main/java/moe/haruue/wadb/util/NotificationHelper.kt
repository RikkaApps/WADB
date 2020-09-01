package moe.haruue.wadb.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import moe.haruue.wadb.R
import moe.haruue.wadb.WadbApplication
import moe.haruue.wadb.WadbPreferences
import moe.haruue.wadb.component.HomeActivity
import moe.haruue.wadb.receiver.TurnOffReceiver

object NotificationHelper {

    private const val NOTIFICATION_ID = R.string.app_name
    private const val NOTIFICATION_CHANNEL = "state"

    @JvmStatic
    fun showNotification(context: Context, ip: String, port: Int) {
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        val contentPendingIntent = PendingIntent.getActivity(context, 0, Intent(context, HomeActivity::class.java), 0)
        val turnOffIntent = Intent("moe.haruue.wadb.action.TURN_OFF_WADB").apply { component = ComponentName(context, TurnOffReceiver::class.java) }

        val turnOffPendingIntent = PendingIntent.getBroadcast(context, 0, turnOffIntent, 0)
        val turnOffAction = Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_close_white_24dp), context.getString(R.string.notification_wadb_enabled_button_disable), turnOffPendingIntent).build()
        val visibility = if (WadbApplication.getDefaultSharedPreferences().getBoolean(WadbPreferences.KEY_SCREEN_LOCK_SWITCH, false)) Notification.VISIBILITY_PUBLIC else Notification.VISIBILITY_PRIVATE

        // Android Q supports dark status bar, but still uses color restriction algorithm of light background,
        // so we still have to use light color here
        val color: Int = when (ThemeHelper.getTheme(context)) {
            ThemeHelper.THEME_WHITE -> {
                context.getColor(R.color.primary_color)
            }
            ThemeHelper.THEME_PINK -> {
                context.getColor(R.color.pink_primary_light)
            }
            ThemeHelper.THEME_CLASSIC -> {
                context.getColor(R.color.primary_color)
            }
            else -> {
                context.getColor(R.color.primary_color)
            }
        }

        // Notification
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, NOTIFICATION_CHANNEL)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }

        builder.setContentTitle(context.getString(R.string.notification_wadb_enabled_title))
                .setContentText("$ip:$port")
                .setSmallIcon(R.drawable.ic_qs_network_adb_on)
                .setContentIntent(contentPendingIntent)
                .addAction(turnOffAction)
                .setVisibility(visibility)
                .setColor(color)
                .setOngoing(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            builder.setPriority(if (WadbApplication.getDefaultSharedPreferences().getBoolean(WadbPreferences.KEY_NOTIFICATION_LOW_PRIORITY, true)) Notification.PRIORITY_MIN else Notification.PRIORITY_DEFAULT)
        } else {
            createNotificationChannel(context)
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    @JvmStatic
    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (notificationManager != null) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL, context.getString(R.string.notification_channel_state), NotificationManager.IMPORTANCE_DEFAULT)
            channel.setSound(null, null)
            channel.setShowBadge(false)
            channel.setBypassDnd(false)
            channel.enableLights(false)
            channel.enableVibration(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                channel.setAllowBubbles(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}