package moe.haruue.wadb;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import moe.haruue.wadb.events.Events;
import moe.haruue.wadb.events.WadbFailureEvent;
import moe.haruue.wadb.events.WadbStateChangedEvent;
import moe.haruue.wadb.util.NetworksUtils;
import moe.haruue.wadb.util.NotificationHelper;
import moe.haruue.wadb.util.ScreenKeeper;

public class WadbApplication extends Application implements WadbStateChangedEvent, WadbFailureEvent {

    private static final ComponentName LAUNCHER_ACTIVITY = ComponentName.createRelative(BuildConfig.APPLICATION_ID, ".ui.activity.LaunchActivity");

    public static boolean isLauncherActivityEnabled(Context context) {
        int state = context.getPackageManager().getComponentEnabledSetting(LAUNCHER_ACTIVITY);
        return state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT || state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static void disableLauncherActivity(Context context) {
        context.getPackageManager().setComponentEnabledSetting(
                LAUNCHER_ACTIVITY,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    public static void enableLauncherActivity(Context context) {
        context.getPackageManager().setComponentEnabledSetting(
                LAUNCHER_ACTIVITY,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP
        );
    }

    public static String getDefaultSharedPreferenceName() {
        return BuildConfig.APPLICATION_ID + "_preferences";
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(getDefaultSharedPreferenceName(), Context.MODE_PRIVATE);
    }

    public static String getWadbPort(Context context) {
        return getDefaultSharedPreferences(context).getString(WadbPreferences.KEY_WAKE_PORT, "5555");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Events.registerAll(this);
    }

    @Override
    public void onWadbStarted(int port) {
        SharedPreferences preferences = getDefaultSharedPreferences(this);
        preferences.edit().putString(WadbPreferences.KEY_WAKE_PORT, Integer.toString(port)).apply();

        String ip = NetworksUtils.getLocalIPAddress(this);
        if (preferences.getBoolean(WadbPreferences.KEY_NOTIFICATION, true)) {
            NotificationHelper.showNotification(this, ip, port);
        }
        if (preferences.getBoolean(WadbPreferences.KEY_WAKE_LOCK, false)) {
            ScreenKeeper.acquireWakeLock(this);
        }
    }

    @Override
    public void onWadbStopped() {
        NotificationHelper.cancelNotification(this);
        ScreenKeeper.releaseWakeLock();
    }

    @Override
    public void onRootPermissionFailure() {
        onWadbStopped();
    }

    @Override
    public void onOperateFailure() {
        onWadbStopped();
    }
}
