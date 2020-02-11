package moe.haruue.wadb;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import java.lang.reflect.Method;

import moe.haruue.wadb.events.Events;
import moe.haruue.wadb.events.WadbFailureEvent;
import moe.haruue.wadb.events.WadbStateChangedEvent;
import moe.haruue.wadb.util.NetworksUtils;
import moe.haruue.wadb.util.NotificationHelper;
import moe.haruue.wadb.util.ScreenKeeper;
import moe.shizuku.preference.SimpleMenuPreference;
import rikka.material.app.DayNightDelegate;

import static android.os.Build.VERSION.SDK_INT;

public class WadbApplication extends Application implements WadbStateChangedEvent, WadbFailureEvent {

    static {
        if (SDK_INT >= 28) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
                //noinspection ConstantConditions
                Object vmRuntime = getRuntime.invoke(null);
                //noinspection ConstantConditions
                setHiddenApiExemptions.invoke(vmRuntime, new Object[]{new String[]{"L"}});
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        SimpleMenuPreference.setLightFixEnabled(true);
    }

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

        DayNightDelegate.setApplicationContext(this);
        DayNightDelegate.setDefaultNightMode(DayNightDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
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
