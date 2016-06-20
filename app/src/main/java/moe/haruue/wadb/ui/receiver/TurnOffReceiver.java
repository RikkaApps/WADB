package moe.haruue.wadb.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import moe.haruue.util.StandardUtils;
import moe.haruue.wadb.R;
import moe.haruue.wadb.data.Commands;
import moe.haruue.wadb.ui.service.NotificationService;

public class TurnOffReceiver extends BroadcastReceiver {

    Listener listener = new Listener();

    public TurnOffReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Commands.stopWadb(listener);
    }

    class Listener implements Commands.CommandsListener {

        @Override
        public void onGetSUAvailable(boolean isAvailable) {

        }

        @Override
        public void onGetAdbState(boolean isWadb, int port) {

        }

        @Override
        public void onGetAdbStateFailure() {

        }

        @Override
        public void onWadbStartListener(boolean isSuccess) {

        }

        @Override
        public void onWadbStopListener(boolean isSuccess) {
            if (!isSuccess) {
                StandardUtils.toast(R.string.failed);
            } else {
                NotificationService.stop(StandardUtils.getApplication());
            }
        }
    }
}
