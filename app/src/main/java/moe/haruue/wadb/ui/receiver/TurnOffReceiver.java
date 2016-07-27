package moe.haruue.wadb.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import moe.haruue.wadb.presenter.Commander;

public class TurnOffReceiver extends BroadcastReceiver {

    public TurnOffReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Commander.stopWadb();
    }

}
