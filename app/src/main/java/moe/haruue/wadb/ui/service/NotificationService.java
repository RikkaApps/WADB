package moe.haruue.wadb.ui.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import moe.haruue.wadb.R;
import moe.haruue.wadb.data.Commands;
import moe.haruue.wadb.ui.activity.MainActivity;
import moe.haruue.wadb.util.IPUtils;

public class NotificationService extends Service {

    Notification notification;
    Listener listener = new Listener();

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Commands.getWadbState(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void showNotification(int port) {
        PendingIntent contentPendingIntent = PendingIntent.getActivity(NotificationService.this, 0 , new Intent(NotificationService.this, MainActivity.class), 0);
        PendingIntent turnOffPendingIntent = PendingIntent.getBroadcast(NotificationService.this, 0, new Intent("moe.haruue.wadb.action.TURN_OFF_WADB"), 0);
        // Notification
        notification = new NotificationCompat.Builder(NotificationService.this)
                .setContentTitle(getResources().getString(R.string.wadb_on))
                .setContentText(IPUtils.getLocalIPAddress(getApplication()) + ":" + port)
                .setSmallIcon(R.drawable.ic_developer_mode_white_24dp)
                .setContentIntent(contentPendingIntent)
                .addAction(R.drawable.ic_close_white_24dp, getString(R.string.turn_off), turnOffPendingIntent)
                .build();
        notification.flags |= NotificationCompat.FLAG_ONGOING_EVENT;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, notification);
    }

    private void cancelNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
    }

    class Listener implements Commands.CommandsListener {

        @Override
        public void onGetSUAvailable(boolean isAvailable) {

        }

        @Override
        public void onGetAdbState(boolean isWadb, int port) {
            if (isWadb) {
                showNotification(port);
            } else {
                stopSelf();
            }
        }

        @Override
        public void onGetAdbStateFailure() {
            stopSelf();
        }

        @Override
        public void onWadbStartListener(boolean isSuccess) {

        }

        @Override
        public void onWadbStopListener(boolean isSuccess) {
            if (!isSuccess) {
                Toast.makeText(NotificationService.this, getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
            Commands.getWadbState(this);
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, NotificationService.class);
        context.startService(starter);
    }

    public static void stop(Context context) {
        Intent stopper = new Intent(context, NotificationService.class);
        context.stopService(stopper);
    }

}
