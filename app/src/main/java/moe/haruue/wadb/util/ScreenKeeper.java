package moe.haruue.wadb.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;

import moe.haruue.wadb.BuildConfig;

/**
 * @author PinkD
 */

public class ScreenKeeper {

    private static PowerManager.WakeLock wakeLock;

    @SuppressWarnings("deprecation")
    @SuppressLint("WakelockTimeout")
    public static void acquireWakeLock(Context context) {
        if (wakeLock == null) {
            PowerManager powerManager = context.getSystemService(PowerManager.class);
            if (powerManager == null) return;
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":ScreenKeeper");
            wakeLock.setReferenceCounted(false);
        }
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    public static void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
        }
    }

}
