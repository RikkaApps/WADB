package moe.haruue.wadb.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import moe.haruue.wadb.ui.activity.MainActivity;

public class SecretCodeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        context.startActivity(new Intent(context, MainActivity.class));
    }
}
