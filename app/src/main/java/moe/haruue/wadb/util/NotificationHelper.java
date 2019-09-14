package moe.haruue.wadb.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.RequiresApi;

import moe.haruue.wadb.R;
import moe.haruue.wadb.WadbApplication;
import moe.haruue.wadb.WadbPreferences;
import moe.haruue.wadb.component.home.HomeActivity;
import moe.haruue.wadb.receiver.TurnOffReceiver;

public class NotificationHelper {

    private static final int NOTIFICATION_ID = R.string.app_name;
    private static final String NOTIFICATION_CHANNEL = "state";

    public static void showNotification(Context context, String ip, int port) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager == null) {
            return;
        }

        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), 0);

        Intent turnOffIntent = new Intent("moe.haruue.wadb.action.TURN_OFF_WADB");
        turnOffIntent.setComponent(new ComponentName(context, TurnOffReceiver.class));
        PendingIntent turnOffPendingIntent = PendingIntent.getBroadcast(context, 0, turnOffIntent, 0);
        Notification.Action turnOffAction = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_close_white_24dp), context.getString(R.string.turn_off), turnOffPendingIntent).build();

        // Android Q supports dark status bar, but still uses color restriction algorithm of light background,
        // so we still have to use light color here
        int color = context.getColor(R.color.primary_color_light);
        // int color = ResourceUtils.resolveColor(context.getTheme(), android.R.attr.colorAccent);

        // Notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, NOTIFICATION_CHANNEL);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setContentTitle(context.getString(R.string.wadb_on))
                .setContentText(ip + ":" + port)
                .setSmallIcon(R.drawable.ic_qs_network_adb_on)
                .setContentIntent(contentPendingIntent)
                .addAction(turnOffAction)
                .setColor(color)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(WadbApplication.getDefaultSharedPreferences().getBoolean(WadbPreferences.KEY_NOTIFICATION_LOW_PRIORITY, true) ?
                    Notification.PRIORITY_MIN : Notification.PRIORITY_DEFAULT);
        } else {
            createNotificationChannel(context);
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannel(Context context) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, context.getString(R.string.notification_channel_state), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            channel.setShowBadge(false);
            channel.setBypassDnd(false);
            channel.enableLights(false);
            channel.enableVibration(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                channel.setAllowBubbles(false);
            }
            notificationManager.createNotificationChannel(channel);
        }
    }
}
