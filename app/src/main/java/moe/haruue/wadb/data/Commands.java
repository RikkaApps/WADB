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

    public static void checkSUAvailable(Activity activity, final CommandsListener listener) {
        ThreadUtils.runOnNewThread(activity, new Runnable() {
            @Override
            public void run() {
                ThreadUtils.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onGetSUAvailable(Shell.SU.available());
                    }
                });
            }
        });
    }

    protected static String checkStateCommand = "getprop service.adb.tcp.port";

    public static void getWadbState(Activity activity, final CommandsListener listener) {
        ThreadUtils.runOnNewThread(activity, new Runnable() {
            @Override
            public void run() {
                List<String> shellResult;
                shellResult = Shell.SU.run(checkStateCommand);
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
        });
    }

    protected static String[] wadbStartCommands = new String[] {
            "setprop service.adb.tcp.port 5555",
            "stop adbd",
            "start adbd"
    };

    public static void startWadb(Activity activity, final CommandsListener listener) {
        ThreadUtils.runOnNewThread(activity, new Runnable() {
            @Override
            public void run() {
                List<String> shellResult = Shell.SU.run(wadbStartCommands);
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
        });
    }

    protected static String[] wadbStopCommands = new String[] {
            "setprop service.adb.tcp.port -1",
            "stop adbd",
            "start adbd"
    };

    public static void stopWadb(Activity activity, final CommandsListener listener) {
        ThreadUtils.runOnNewThread(activity, new Runnable() {
            @Override
            public void run() {
                List<String> shellResult = Shell.SU.run(wadbStopCommands);
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
        });
    }

}
