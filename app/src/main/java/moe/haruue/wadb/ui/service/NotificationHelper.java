package moe.haruue.wadb.ui.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import moe.haruue.util.StandardUtils;
import moe.haruue.wadb.R;
import moe.haruue.wadb.presenter.Commander;
import moe.haruue.wadb.ui.activity.MainActivity;
import moe.haruue.wadb.util.ScreenKeeper;

public class NotificationHelper {

    private static final String TAG = NotificationHelper.class.getSimpleName();

    private static Listener listener;

    private static void showNotification(Context context, String ip, int port) {
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        PendingIntent turnOffPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("moe.haruue.wadb.action.TURN_OFF_WADB"), 0);
        // Notification
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.wadb_on))
                .setContentText(ip + ":" + port)
                .setSmallIcon(R.drawable.ic_qs_network_adb_on)
                .setContentIntent(contentPendingIntent)
                .addAction(R.drawable.ic_close_white_24dp, context.getString(R.string.turn_off), turnOffPendingIntent)
                .setOngoing(true)
                .setPriority(PreferenceManager.getDefaultSharedPreferences(StandardUtils.getApplication()).getBoolean("pref_key_notification_low_priority", true) ?
                        NotificationCompat.PRIORITY_MIN : NotificationCompat.PRIORITY_DEFAULT)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(R.string.app_name, notification);
    }

    private static void cancelNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(R.string.app_name);
    }

    private static class Listener implements Commander.WadbStateChangeListener {

        @Override
        public void onWadbStart(String ip, int port) {
            showNotification(StandardUtils.getApplication(), ip, port);
            ScreenKeeper.acquireWakeLock();
        }

        @Override
        public void onWadbStop() {
            cancelNotification(StandardUtils.getApplication());
            ScreenKeeper.releaseWakeLock();
        }
    }

    public static void start(Context context) {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_notification", true)) {
            return;
        }

        if (listener == null) {
            listener = new Listener();
        }
        Commander.addChangeListener(listener);
        Commander.checkWadbState();
    }

    public static void stop(Context context) {
        Commander.removeChangeListener(listener);
        cancelNotification(context);
    }

    public static void refresh(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_notification", true)) {
            start(context);
        } else {
            stop(context);
        }

    }

}
