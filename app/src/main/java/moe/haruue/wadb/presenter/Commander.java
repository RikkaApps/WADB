package moe.haruue.wadb.presenter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.HashSet;

import moe.haruue.util.StandardUtils;
import moe.haruue.wadb.R;
import moe.haruue.wadb.data.Commands;
import moe.haruue.wadb.ui.activity.RootPermissionErrorDialogShadowActivity;
import moe.haruue.wadb.ui.service.NotificationService;
import moe.haruue.wadb.util.IPUtils;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class Commander {

    public final static int ACTION_START_WADB = 1;
    public final static int ACTION_STOP_WADB = 2;
    public final static int STATE_START_WADB = 3;
    public final static int STATE_STOP_WADB = 4;
    public final static int STATE_GET_STATE_FAILURE = 5;
    public final static int STATE_ROOT_PERMISSION_FAILURE = 6;
    public final static int STATE_OPERATE_FAILED = 7;
    public final static int ACTION_CHECK_STATE = 8;

    private static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ACTION_START_WADB:
                    Commands.startWadb(commandsListener);
                    break;
                case ACTION_STOP_WADB:
                    Commands.stopWadb(commandsListener);
                    break;
                case STATE_START_WADB:
                    NotificationService.start(StandardUtils.getApplication());
                    notifyWadbStateChange(new WadbStateChange() {
                        @Override
                        public void change(WadbStateChangeListener listener) {
                            listener.onWadbStart(IPUtils.getLocalIPAddress(), msg.arg1);
                        }
                    });
                    break;
                case STATE_STOP_WADB:
                    NotificationService.stop(StandardUtils.getApplication());
                    notifyWadbStateChange(new WadbStateChange() {
                        @Override
                        public void change(WadbStateChangeListener listener) {
                            listener.onWadbStop();
                        }
                    });
                    break;
                case STATE_GET_STATE_FAILURE:
                    StandardUtils.toast(R.string.refresh_state_failure);
                    notifyWadbFailure(new WadbFailure() {
                        @Override
                        public void failure(WadbFailureListener listener) {
                            listener.onStateRefreshFailure();
                        }
                    });
                    notifyWadbStateChange(new WadbStateChange() {
                        @Override
                        public void change(WadbStateChangeListener listener) {
                            listener.onWadbStop();
                        }
                    });
                    break;
                case STATE_ROOT_PERMISSION_FAILURE:
                    RootPermissionErrorDialogShadowActivity.start(StandardUtils.getApplication());
                    notifyWadbFailure(new WadbFailure() {
                        @Override
                        public void failure(WadbFailureListener listener) {
                            listener.onRootPermissionFailure();
                        }
                    });
                    break;
                case STATE_OPERATE_FAILED:
                    StandardUtils.toast(R.string.failed);
                    notifyWadbFailure(new WadbFailure() {
                        @Override
                        public void failure(WadbFailureListener listener) {
                            listener.onOperateFailure();
                        }
                    });
                    break;
                case ACTION_CHECK_STATE:
                    Commands.getWadbState(commandsListener);
                    break;
            }
        }
    };

    public interface WadbStateChangeListener {
        void onWadbStart(String ip, int port);
        void onWadbStop();
    }

    private static HashSet<WadbStateChangeListener> wadbStateChangeListeners = new HashSet<>(0);

    public interface WadbFailureListener {
        void onRootPermissionFailure();
        void onStateRefreshFailure();
        void onOperateFailure();
    }

    private static HashSet<WadbFailureListener> wadbFailureListeners = new HashSet<>(0);

    public static void startWadb() {
        Message message = Message.obtain();
        message.what = ACTION_START_WADB;
        handler.sendMessage(message);
    }

    public static void stopWadb() {
        Message message = Message.obtain();
        message.what = ACTION_STOP_WADB;
        handler.sendMessage(message);
    }

    public static void checkWadbState() {
        Message message = Message.obtain();
        message.what = ACTION_CHECK_STATE;
        handler.sendMessage(message);
    }

    public static void addChangeListener(WadbStateChangeListener listener) {
        wadbStateChangeListeners.add(listener);
    }

    public static void removeChangeListener(WadbStateChangeListener listener) {
        wadbStateChangeListeners.remove(listener);
    }

    public interface WadbStateChange {
        void change(WadbStateChangeListener listener);
    }

    private static synchronized void notifyWadbStateChange(WadbStateChange change) {
        ArrayList<WadbStateChangeListener> uselessListener = new ArrayList<>(0);
        for (WadbStateChangeListener l: wadbStateChangeListeners) {
            try {
                change.change(l);
            } catch (Throwable t) {
                StandardUtils.printStack(t);
                uselessListener.add(l);
            }
        }
        wadbStateChangeListeners.removeAll(uselessListener);
    }

    public interface WadbFailure {
        void failure(WadbFailureListener listener);
    }

    public static void addFailureListener(WadbFailureListener listener) {
        wadbFailureListeners.add(listener);
    }

    public static void removeFailureListener(WadbFailureListener listener) {
        wadbFailureListeners.remove(listener);
    }

    private static synchronized void notifyWadbFailure(WadbFailure failure) {
        ArrayList<WadbFailureListener> uselessListener = new ArrayList<>(0);
        for (WadbFailureListener l: wadbFailureListeners) {
            try {
                failure.failure(l);
            } catch (Throwable t) {
                StandardUtils.printStack(t);
                uselessListener.add(l);
            }
        }
        wadbFailureListeners.removeAll(uselessListener);
    }

    private static CommandsListener commandsListener = new CommandsListener();

    private static class CommandsListener implements Commands.CommandsListener {

        @Override
        public void onGetSUAvailable(boolean isAvailable) {
            if (!isAvailable) {
                Message message = Message.obtain();
                message.what = STATE_ROOT_PERMISSION_FAILURE;
                handler.sendMessage(message);
            }
        }

        @Override
        public void onGetAdbState(boolean isWadb, int port) {
            if (isWadb) {
                Message message = Message.obtain();
                message.what = STATE_START_WADB;
                message.arg1 = port;
                handler.sendMessage(message);
            } else {
                Message message = Message.obtain();
                message.what = STATE_STOP_WADB;
                handler.sendMessage(message);
            }
        }

        @Override
        public void onGetAdbStateFailure() {
            Message message = Message.obtain();
            message.what = STATE_GET_STATE_FAILURE;
            handler.sendMessage(message);
        }

        @Override
        public void onWadbStartListener(boolean isSuccess) {
            if (isSuccess) {
                Commands.getWadbState(this);
            } else {
                onOperateFailed();
            }
        }

        @Override
        public void onWadbStopListener(boolean isSuccess) {
            if (isSuccess) {
                Commands.getWadbState(this);
            } else {
                onOperateFailed();
            }
        }

        private void onOperateFailed() {
            Message message = Message.obtain();
            message.what = STATE_OPERATE_FAILED;
            handler.sendMessage(message);
        }
    }

}
