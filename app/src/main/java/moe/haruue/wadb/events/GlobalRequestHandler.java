package moe.haruue.wadb.events;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import moe.haruue.wadb.BuildConfig;
import moe.haruue.wadb.util.SuShell;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuSystemProperties;
import rikka.sui.Sui;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

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

    private static int runCommands(String[] cmds) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        int exitCode = -1;
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
        return exitCode;
    }

    public static void startWadb(String port) {
        if (Sui.isSui()) {
            Runnable runnable = () -> {
                try {
                    ShizukuSystemProperties.set("service.adb.tcp.port", port);
                    if (!BuildConfig.DONOT_RESTART_ADBD || !BuildConfig.DEBUG) {
                        ShizukuSystemProperties.set("ctl.restart", "adbd");
                    }
                    Events.postWadbStateChangedEvent(event -> event.onWadbStarted(Integer.parseInt(port)));
                } catch (Throwable e) {
                    e.printStackTrace();
                    Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
                }
            };

            if (Shizuku.checkSelfPermission() == PERMISSION_GRANTED) {
                runnable.run();
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                Events.postWadbFailureEvent(WadbFailureEvent::onRootPermissionFailure);
            } else {
                Shizuku.addRequestPermissionResultListener(new Shizuku.OnRequestPermissionResultListener() {
                    @Override
                    public void onRequestPermissionResult(int requestCode, int grantResult) {
                        if (requestCode != 1) return;

                        Shizuku.removeRequestPermissionResultListener(this);
                        if (grantResult == PERMISSION_GRANTED) {
                            runnable.run();
                        }
                    }
                });
                Shizuku.requestPermission(1);
            }
        } else {
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
    }

    public static void stopWadb() {
        if (Sui.isSui()) {
            Runnable runnable = () -> {
                try {
                    ShizukuSystemProperties.set("service.adb.tcp.port", "-1");
                    if (!BuildConfig.DONOT_RESTART_ADBD || !BuildConfig.DEBUG) {
                        ShizukuSystemProperties.set("ctl.restart", "adbd");
                    }
                    Events.postWadbStateChangedEvent(WadbStateChangedEvent::onWadbStopped);
                } catch (Throwable e) {
                    e.printStackTrace();
                    Events.postWadbFailureEvent(WadbFailureEvent::onOperateFailure);
                }
            };

            if (Shizuku.checkSelfPermission() == PERMISSION_GRANTED) {
                runnable.run();
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                Events.postWadbFailureEvent(WadbFailureEvent::onRootPermissionFailure);
            } else {
                Shizuku.addRequestPermissionResultListener(new Shizuku.OnRequestPermissionResultListener() {
                    @Override
                    public void onRequestPermissionResult(int requestCode, int grantResult) {
                        if (requestCode != 2) return;

                        Shizuku.removeRequestPermissionResultListener(this);
                        if (grantResult == PERMISSION_GRANTED) {
                            runnable.run();
                        }
                    }
                });
                Shizuku.requestPermission(2);
            }
        } else {
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
}
