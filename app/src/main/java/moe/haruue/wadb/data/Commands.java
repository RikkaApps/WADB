package moe.haruue.wadb.data;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import moe.haruue.util.StandardUtils;
import moe.haruue.util.ThreadUtils;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class Commands {

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
        return new String[]{
                "setprop service.adb.tcp.port " + port,
                "stop adbd",
                "start adbd"
        };
    }

    private static Runnable startWadbRunnable(final CommandsListener listener, String port) {
        return new Runnable() {
            @Override
            public void run() {
                if (!isSUAvailable()) {
                    listener.onGetSUAvailable(false);
                    return;
                }
                List<String> shellResult = Shell.SU.run(commandFromPort(port));
                if (shellResult != null) {
                    listener.onWadbStartListener(true);
                } else {
                    listener.onWadbStartListener(false);
                }
            }
        };
    }

    public static void startWadb(final CommandsListener listener, String port) {
        ThreadUtils.runOnNewThread(startWadbRunnable(listener, port));
    }

    private static String[] stopWadbCommands = new String[]{
            "setprop service.adb.tcp.port -1",
            "stop adbd",
            "start adbd"
    };

    private static Runnable stopWadbRunnable(final CommandsListener listener) {
        return new Runnable() {
            @Override
            public void run() {
                if (!isSUAvailable()) {
                    listener.onGetSUAvailable(false);
                    return;
                }
                List<String> shellResult = Shell.SU.run(stopWadbCommands);
                if (shellResult != null) {
                    listener.onWadbStopListener(true);
                } else {
                    listener.onWadbStopListener(false);
                }
            }
        };
    }

    public static void stopWadb(final CommandsListener listener) {
        ThreadUtils.runOnNewThread(stopWadbRunnable(listener));
    }

}
