package moe.haruue.wadb.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import moe.haruue.wadb.events.GlobalRequestHandler;

public class TurnOffReceiver extends BroadcastReceiver {

    public TurnOffReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalRequestHandler.stopWadb();
    }

}
