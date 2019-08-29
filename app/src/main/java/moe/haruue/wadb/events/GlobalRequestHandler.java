package moe.haruue.wadb.events;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import moe.haruue.wadb.BuildConfig;
import moe.haruue.wadb.util.SuShell;

public class GlobalRequestHandler {

    public static final String TAG = GlobalRequestHandler.class.getSimpleName();

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

    private final static String[] STOP_WADB_COMMANDS;

    static {
        if (BuildConfig.DONOT_RESTART_ADBD && BuildConfig.DEBUG) {
            STOP_WADB_COMMANDS = new String[]{
                    // don't restart adbd for debug
                    "setprop service.adb.tcp.port -1"
            };
        } else {
            STOP_WADB_COMMANDS = new String[]{
                    "setprop service.adb.tcp.port -1",
                    "stop adbd",
                    "start adbd"
            };
        }
    }

    private static String[] getCommandFromPort(String port) {
        if (BuildConfig.DONOT_RESTART_ADBD && BuildConfig.DEBUG) {
            return new String[]{
                    // don't restart adbd for debug
                    "setprop service.adb.tcp.port " + port
            };
        } else {
            return new String[]{
                    "setprop service.adb.tcp.port " + port,
                    "stop adbd",
                    "start adbd"
            };
        }
    }

    private static int getWadbPort() {
        String port = SystemProperties.get("service.adb.tcp.port");
        if (!TextUtils.isEmpty(port)) {
            try {
                return Integer.parseInt(port);
            } catch (Throwable tr) {
                tr.printStackTrace();
            }
        }
        return -1;
    }

    public static void checkWadbState() {
        int port;
        if ((port = getWadbPort()) != -1) {
            Events.postWadbStateChangedEvent(event -> event.onWadbStarted(port));
        } else {
            Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
        }
    }

    public static void startWadb(String port) {
        EXECUTOR.submit(() -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            if (!SuShell.available()) {
                log("Check SU", false);
                Events.postWadbFailureEvent(WadbFailureEvent::onRootPermissionFailure);
                return;
            }
            SuShell.Result shellResult = SuShell.run(getCommandFromPort(port));
            log("Wadb start", shellResult.exitCode == 0);


            if (shellResult.exitCode == 0) {
                GlobalRequestHandler.checkWadbState();
            } else {
                GlobalRequestHandler.checkWadbState();
                Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
            }
        });
    }

    public static void stopWadb() {
        EXECUTOR.submit(() -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            if (!SuShell.available()) {
                log("Check SU", false);
                Events.postWadbFailureEvent(WadbFailureEvent::onRootPermissionFailure);
                return;
            }
            SuShell.Result shellResult = SuShell.run(STOP_WADB_COMMANDS);
            log("Wadb stop", shellResult.exitCode == 0);
            if (shellResult.exitCode == 0) {
                GlobalRequestHandler.checkWadbState();
            } else {
                GlobalRequestHandler.checkWadbState();
                Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
            }
        });
    }

    private static void log(String message, boolean isSuccess) {
        if (BuildConfig.DEBUG) {
            if (isSuccess) {
                Log.d(TAG, message + ", succeed");
            } else {
                Log.e(TAG, message + ", failed");
            }
        }
    }
}
