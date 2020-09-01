package moe.haruue.wadb.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import moe.haruue.wadb.WadbApplication
import moe.haruue.wadb.events.GlobalRequestHandler

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED
                && intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }

        Log.d("WABD", "onReceive: ${intent.action}")

        GlobalRequestHandler.startWadb(WadbApplication.wadbPort)
    }
}