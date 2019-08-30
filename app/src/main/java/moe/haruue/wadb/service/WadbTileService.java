package moe.haruue.wadb.service;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.annotation.RequiresApi;

import moe.haruue.wadb.R;
import moe.haruue.wadb.WadbApplication;
import moe.haruue.wadb.WadbPreferences;
import moe.haruue.wadb.events.Events;
import moe.haruue.wadb.events.GlobalRequestHandler;
import moe.haruue.wadb.events.WadbFailureEvent;
import moe.haruue.wadb.events.WadbStateChangedEvent;
import moe.haruue.wadb.util.NetworksUtils;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public abstract class WadbTileService extends TileService {

    private final Runnable mStartWadbRunnable = () -> {
        GlobalRequestHandler.startWadb(WadbApplication.getWadbPort(this));
    };

    private static final Runnable STOP_WADB = GlobalRequestHandler::stopWadb;

    public static void requestListening(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TileService.requestListeningState(context, ComponentName.createRelative(context, moe.haruue.wadb.ui.service.WadbTileService.class.getName()));
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        int port;
        if ((port = GlobalRequestHandler.getWadbPort()) != -1) {
            showStateOn(NetworksUtils.getLocalIPAddress(this), port);
        } else {
            showStateOff();
        }
    }

    @Override
    public void onClick() {
        super.onClick();
        boolean enableScreenLockSwitch = WadbApplication.getDefaultSharedPreferences(this).getBoolean("pref_key_screen_lock_switch", false);
        if (getQsTile().getState() == Tile.STATE_ACTIVE) {
            if (enableScreenLockSwitch) {
                STOP_WADB.run();
            } else {
                unlockAndRun(STOP_WADB);
            }
        } else {
            if (enableScreenLockSwitch) {
                mStartWadbRunnable.run();
            } else {
                unlockAndRun(mStartWadbRunnable);
            }
        }
    }

    private void showStateOn(String ip, int port) {
        final Context context = this;
        final Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.setIcon(Icon.createWithResource(context, R.drawable.ic_qs_network_adb_on));
        tile.setLabel(ip + ":" + port);
        tile.updateTile();
    }

    private void showStateOff() {
        final Context context = this;
        final Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        tile.setIcon(Icon.createWithResource(context, R.drawable.ic_qs_network_adb_off));
        tile.setLabel(context.getString(R.string.app_name));
        tile.updateTile();
    }

    private void showStateUnavailable() {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_UNAVAILABLE);
        tile.updateTile();
    }
}
