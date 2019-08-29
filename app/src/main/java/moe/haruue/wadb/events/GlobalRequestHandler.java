package moe.haruue.wadb.events;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import moe.haruue.wadb.BuildConfig;
import moe.haruue.wadb.util.SuShell;
import moe.shizuku.api.RemoteProcess;
import moe.shizuku.api.ShizukuService;

public class GlobalRequestHandler {

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
        int port;
        if ((port = getWadbPort()) != -1) {
            Events.postWadbStateChangedEvent(event -> event.onWadbStarted(port));
        } else {
            Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
        }
    }

    private static boolean isShizukuAvailable() {
        if (ShizukuService.pingBinder()) {
            try {
                ShizukuService.getUid();
                return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static int runShizukuRemoteCommands(String[] cmds) {
        for (String line : cmds) {
            try {
                String[] cmd = line.split(" ");
                RemoteProcess remoteProcess = ShizukuService.newProcess(cmd, null, null);
                int res = remoteProcess.waitFor();
                remoteProcess.destroy();

                if (res != 0) {
                    return res;
                }
            } catch (Throwable tr) {
                tr.printStackTrace();
            }
        }
        return -1;
    }

    public static void startWadb(String port) {
        EXECUTOR.submit(() -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            int exitCode;
            if (isShizukuAvailable()) {
                exitCode = runShizukuRemoteCommands(getStartWadbCommand(port));
            } else {
                if (!SuShell.available()) {
                    Events.postWadbFailureEvent(WadbFailureEvent::onRootPermissionFailure);
                    return;
                }
                SuShell.Result shellResult = SuShell.run(getStartWadbCommand(port));
                exitCode = shellResult.exitCode;
            }

            if (exitCode == 0) {
                Events.postWadbStateChangedEvent(event -> event.onWadbStarted(Integer.parseInt(port)));
            } else {
                Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
            }
        });
    }

    public static void stopWadb() {
        EXECUTOR.submit(() -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            int exitCode;
            if (isShizukuAvailable()) {
                exitCode = runShizukuRemoteCommands(STOP_WADB_COMMANDS);
            } else {
                if (!SuShell.available()) {
                    Events.postWadbFailureEvent(WadbFailureEvent::onRootPermissionFailure);
                    return;
                }
                SuShell.Result shellResult = SuShell.run(STOP_WADB_COMMANDS);
                exitCode = shellResult.exitCode;
            }

            if (exitCode == 0) {
                Events.postWadbStateChangedEvent(WadbStateChangedEvent::onWadbStopped);
            } else {
                Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
            }
        });
    }
}
