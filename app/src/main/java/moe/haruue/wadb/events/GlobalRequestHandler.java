package moe.haruue.wadb.events;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import moe.haruue.wadb.BuildConfig;
import moe.haruue.wadb.WadbApplication;
import moe.haruue.wadb.WadbPreferences;
import moe.haruue.wadb.util.SuShell;
import moe.shizuku.api.RemoteProcess;
import moe.shizuku.api.ShizukuService;

public class GlobalRequestHandler {

    private static final String TAG = "GlobalRequestHandler";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

    private final static String[] STOP_WADB_COMMANDS;

    static {
        if (BuildConfig.DONOT_RESTART_ADBD && BuildConfig.DEBUG) {
            STOP_WADB_COMMANDS = new String[]{
                    "setprop service.adb.tcp.port -1"
            };
        } else {
            STOP_WADB_COMMANDS = new String[]{
                    "setprop service.adb.tcp.port -1",
                    "setprop ctl.restart adbd"
            };
        }
    }

    private static String[] getStartWadbCommand(String port) {
        if (BuildConfig.DONOT_RESTART_ADBD && BuildConfig.DEBUG) {
            return new String[]{
                    "setprop service.adb.tcp.port " + port
            };
        } else {
            return new String[]{
                    "setprop service.adb.tcp.port " + port,
                    "setprop ctl.restart adbd"
            };
        }
    }

    public static int getWadbPort() {
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
        Log.d(TAG, "checkWadbState");

        int port;
        if ((port = getWadbPort()) != -1) {
            Events.postWadbStateChangedEvent(event -> event.onWadbStarted(port));
        } else {
            Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
        }
    }

    private static boolean isShizukuAvailable() {
        if (!WadbApplication.getDefaultSharedPreferences().getBoolean(WadbPreferences.KEY_SHIZUKU, false)) {
            return false;
        }

        if (ShizukuService.pingBinder()) {
            try {
                String context = ShizukuService.getSELinuxContext();
                return context != null && !"u:r:shell:s0".equals(context);
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static int runShizukuRemoteCommands(String[] cmds) {
        int res = 0;
        for (String line : cmds) {
            long time = 0;
            try {
                if (DEBUG) {
                    time = System.currentTimeMillis();
                }
                String[] cmd = line.split(" ");
                RemoteProcess remoteProcess = ShizukuService.newProcess(cmd, null, null);
                res = remoteProcess.waitFor();
                remoteProcess.destroy();
            } catch (Throwable tr) {
                tr.printStackTrace();
            } finally {
                if (DEBUG) {
                    Log.d(TAG, "Shizuku [" + line + "] takes " + (System.currentTimeMillis() - time) + "ms");
                }
            }
        }
        return res;
    }

    private static int runCommands(String[] cmds) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        int exitCode = -1;
        if (isShizukuAvailable()) {
            exitCode = runShizukuRemoteCommands(cmds);
        } else {
            long time = 0;
            if (DEBUG) {
                time = System.currentTimeMillis();
            }

            if (!SuShell.available()) {
                Events.postWadbFailureEvent(WadbFailureEvent::onRootPermissionFailure);
                return exitCode;
            }
            SuShell.Result shellResult = SuShell.run(cmds);
            exitCode = shellResult.exitCode;
            if (DEBUG) {
                Log.d(TAG, "su " + Arrays.toString(cmds) + " takes " + (System.currentTimeMillis() - time) + "ms");
            }
        }
        return exitCode;
    }

    public static void startWadb(String port) {
        EXECUTOR.submit(() -> {
            int exitCode = runCommands(getStartWadbCommand(port));

            Log.d(TAG, "startWadb: " + exitCode);

            if (exitCode == 0) {
                Events.postWadbStateChangedEvent(event -> event.onWadbStarted(Integer.parseInt(port)));
            } else {
                Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
            }
        });
    }

    public static void stopWadb() {
        EXECUTOR.submit(() -> {
            int exitCode = runCommands(STOP_WADB_COMMANDS);

            if (exitCode == 0) {
                Events.postWadbStateChangedEvent(WadbStateChangedEvent::onWadbStopped);
            } else {
                Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
            }
        });
    }
}
