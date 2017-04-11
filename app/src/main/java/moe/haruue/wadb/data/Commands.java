package moe.haruue.wadb.data;

import android.util.Log;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import moe.haruue.util.StandardUtils;
import moe.haruue.util.ThreadUtils;
import moe.haruue.wadb.BuildConfig;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class Commands {

    public static final String TAG = Commands.class.getSimpleName();

    public interface CommandsListener {
        void onGetSUAvailable(boolean isAvailable);

        void onGetAdbState(boolean isWadb, int port);

        void onGetAdbStateFailure();

        void onWadbStartListener(boolean isSuccess);

        void onWadbStopListener(boolean isSuccess);
    }

    public static abstract class AbstractCommandsListener implements CommandsListener {

        @Override
        public void onGetSUAvailable(boolean isAvailable) {}

        @Override
        public void onGetAdbState(boolean isWadb, int port) {}

        @Override
        public void onGetAdbStateFailure() {}

        @Override
        public void onWadbStartListener(boolean isSuccess) {}

        @Override
        public void onWadbStopListener(boolean isSuccess) {}
    }

    private static boolean isSUAvailable() {
        return Shell.SU.available();
    }

    private static String checkWadbStateCommand = "getprop service.adb.tcp.port";

    private static Runnable checkWadbStateRunnable(final CommandsListener listener) {
        return new Runnable() {
            @Override
            public void run() {
                List<String> shellResult;
                shellResult = Shell.SH.run(checkWadbStateCommand);
                log("Check Wadb state", shellResult != null);
                if (shellResult == null) {
                    listener.onGetAdbStateFailure();
                } else {
                    String shellStringResult = "";
                    for (String s : shellResult) {
                        shellStringResult += s;
                    }
                    int port = -1;
                    try {
                        port = Integer.parseInt(shellStringResult);
                    } catch (Exception e) {
                        StandardUtils.printStack(e);
                        listener.onGetAdbStateFailure();
                    }
                    if (port <= 0) {
                        listener.onGetAdbState(false, port);
                    } else {
                        listener.onGetAdbState(true, port);
                    }
                }
            }
        };
    }

    public static void getWadbState(final CommandsListener listener) {
        ThreadUtils.runOnNewThread(checkWadbStateRunnable(listener));
    }

    private static String[] commandFromPort(String port) {
        if (BuildConfig.FAKE_OPERATE_MODE && BuildConfig.DEBUG) {
            return new String[]{
                    // don't restart adbd for debug
                    "setprop service.adb.tcp.port " + port
            };
        }
        return new String[]{
                "setprop service.adb.tcp.port " + port,
                "stop adbd",
                "start adbd"
        };
    }

    private static Runnable startWadbRunnable(final CommandsListener listener, final String port) {
        return new Runnable() {
            @Override
            public void run() {
                if (!isSUAvailable()) {
                    log("Check SU", false);
                    listener.onGetSUAvailable(false);
                    return;
                }
                List<String> shellResult = Shell.SU.run(commandFromPort(port));
                log("Wadb start", shellResult != null);
                listener.onWadbStartListener(shellResult != null);
            }
        };
    }

    public static void startWadb(final CommandsListener listener, String port) {
        ThreadUtils.runOnNewThread(startWadbRunnable(listener, port));
    }

    private static String[] stopWadbCommands;

    static {
        if (BuildConfig.FAKE_OPERATE_MODE && BuildConfig.DEBUG) {
            stopWadbCommands = new String[]{
                    // don't restart adbd for debug
                    "setprop service.adb.tcp.port -1"
            };
        } else {
            stopWadbCommands = new String[]{
                    "setprop service.adb.tcp.port -1",
                    "stop adbd",
                    "start adbd"
            };
        }
    }

    private static Runnable stopWadbRunnable(final CommandsListener listener) {
        return new Runnable() {
            @Override
            public void run() {
                if (!isSUAvailable()) {
                    log("Check SU", false);
                    listener.onGetSUAvailable(false);
                    return;
                }
                List<String> shellResult = Shell.SU.run(stopWadbCommands);
                log("Wadb stop", shellResult != null);
                listener.onWadbStopListener(shellResult != null);
            }
        };
    }

    public static void stopWadb(final CommandsListener listener) {
        ThreadUtils.runOnNewThread(stopWadbRunnable(listener));
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
