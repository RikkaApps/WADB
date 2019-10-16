package moe.haruue.wadb;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

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

    public static SharedPreferences getDefaultSharedPreferences() {
        Context context= getInstance();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context = getInstance().createDeviceProtectedStorageContext();
            context.moveSharedPreferencesFrom(getInstance(),getDefaultSharedPreferenceName());
        }
        return context.getSharedPreferences(getDefaultSharedPreferenceName(), Context.MODE_PRIVATE);
    }

    public static String getWadbPort() {
        String port = getDefaultSharedPreferences().getString(WadbPreferences.KEY_WAKE_PORT, "5555");
        int p;
        try {
            p = Integer.parseInt(port);
            if (p < 1025 || p > 65535) {
                p = 5555;
                getDefaultSharedPreferences().edit().putString(WadbPreferences.KEY_WAKE_PORT, "5555").apply();
            }
        } catch (NumberFormatException e) {
            p = 5555;
            getDefaultSharedPreferences().edit().putString(WadbPreferences.KEY_WAKE_PORT, "5555").apply();
        }

        return Integer.toString(p);
    }

    public static WadbApplication sApplication;

    public static WadbApplication getInstance() {
        return sApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Events.registerAll(this);
    }

    @Override
    public void onWadbStarted(int port) {
        SharedPreferences preferences = getDefaultSharedPreferences();
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sApplication = this;
    }
}
