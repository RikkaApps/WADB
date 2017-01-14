package moe.haruue.wadb.util;

import android.os.PowerManager;
import android.preference.PreferenceManager;

import moe.haruue.util.StandardUtils;

import static android.content.Context.POWER_SERVICE;

public class ScreenKeeper {
    private static PowerManager.WakeLock wakeLock;

    public static void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) StandardUtils.getApplication().getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "WADB");
        }
        if (PreferenceManager.getDefaultSharedPreferences(StandardUtils.getApplication()).getBoolean("pref_key_wake_lock", false)) {
            wakeLock.acquire();
        }
    }

    public static void releaseWakeLock() {
        wakeLock.release();
    }

}
