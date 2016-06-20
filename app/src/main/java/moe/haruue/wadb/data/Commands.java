package moe.haruue.wadb.data;

import android.app.Activity;

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

    private static Runnable checkSUAvailableRunnable(final CommandsListener listener) {
        return new Runnable() {
            @Override
            public void run() {
                final boolean isAvailable = Shell.SU.available();
                ThreadUtils.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onGetSUAvailable(isAvailable);
                    }
                });
            }
        };
    }

    public static void checkSUAvailable(Activity activity, final CommandsListener listener) {
        ThreadUtils.runOnNewThread(activity, checkSUAvailableRunnable(listener));
    }

    public static void checkSUAvailable(final CommandsListener listener) {
        ThreadUtils.runOnNewThread(checkSUAvailableRunnable(listener));
    }

    private static String checkWadbStateCommand = "getprop service.adb.tcp.port";

    private static Runnable checkWadbStateRunnable(final CommandsListener listener) {
        return new Runnable() {
            @Override
            public void run() {
                List<String> shellResult;
                shellResult = Shell.SU.run(checkWadbStateCommand);
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
                        ThreadUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onGetAdbStateFailure();
                            }
                        });
                    }
                    if (port <= 0) {
                        final int finalPort = port;
                        ThreadUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onGetAdbState(false, finalPort);
                            }
                        });
                    } else {
                        final int finalPort1 = port;
                        ThreadUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onGetAdbState(true, finalPort1);
                            }
                        });
                    }
                }
            }
        };
    }

    public static void getWadbState(Activity activity, final CommandsListener listener) {
        ThreadUtils.runOnNewThread(activity, checkWadbStateRunnable(listener));
    }

    public static void getWadbState(final CommandsListener listener) {
        ThreadUtils.runOnNewThread(checkWadbStateRunnable(listener));
    }

    private static String[] startWadbCommands = new String[]{
            "setprop service.adb.tcp.port 5555",
            "stop adbd",
            "start adbd"
    };

    private static Runnable startWadbRunnable(final CommandsListener listener) {
        return new Runnable() {
            @Override
            public void run() {
                List<String> shellResult = Shell.SU.run(startWadbCommands);
                if (shellResult != null) {
                    ThreadUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onWadbStartListener(true);
                        }
                    });
                } else {
                    ThreadUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onWadbStartListener(false);
                        }
                    });
                }
            }
        };
    }

    public static void startWadb(Activity activity, final CommandsListener listener) {
        ThreadUtils.runOnNewThread(activity, startWadbRunnable(listener));
    }

    public static void startWadb(final CommandsListener listener) {
        ThreadUtils.runOnNewThread(startWadbRunnable(listener));
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
                List<String> shellResult = Shell.SU.run(stopWadbCommands);
                if (shellResult != null) {
                    ThreadUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onWadbStopListener(true);
                        }
                    });
                } else {
                    ThreadUtils.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onWadbStopListener(false);
                        }
                    });
                }
            }
        };
    }

    public static void stopWadb(Activity activity, final CommandsListener listener) {
        ThreadUtils.runOnNewThread(activity, stopWadbRunnable(listener));
    }

    public static void stopWadb(final CommandsListener listener) {
        ThreadUtils.runOnNewThread(stopWadbRunnable(listener));
    }

}
