package moe.haruue.wadb.ui.service;

import android.graphics.drawable.Icon;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

import moe.haruue.util.StandardUtils;
import moe.haruue.wadb.R;
import moe.haruue.wadb.presenter.Commander;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class WadbTileService extends TileService {

    Listener listener = new Listener();

    @Override
    public void onCreate() {
        super.onCreate();
        Commander.addChangeListener(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Commander.removeChangeListener(listener);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Commander.checkWadbState();
        NotificationHelper.start(getApplicationContext());
    }

    Runnable startWadbRunnable = new Runnable() {
        @Override
        public void run() {
            Commander.startWadb();
        }
    };
    Runnable stopWadbRunnable = new Runnable() {
        @Override
        public void run() {
            Commander.stopWadb();
        }
    };

    @Override
    public void onClick() {
        super.onClick();
        boolean enableScreenLockSwitch = PreferenceManager.getDefaultSharedPreferences(StandardUtils.getApplication()).getBoolean("pref_key_screen_lock_switch", false);
        if (getQsTile().getState() == Tile.STATE_ACTIVE) {
            if (enableScreenLockSwitch) {
                stopWadbRunnable.run();
            } else {
                unlockAndRun(stopWadbRunnable);
            }
        } else {
            if (enableScreenLockSwitch) {
                startWadbRunnable.run();
            } else {
                unlockAndRun(startWadbRunnable);
            }
        }
    }

    private void showStateOn(String ip, int port) {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.setIcon(Icon.createWithResource(getApplication(), R.drawable.ic_qs_network_adb_on));
        tile.setLabel(ip + ":" + port);
        tile.updateTile();
    }

    private void showStateOff() {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        tile.setIcon(Icon.createWithResource(getApplication(), R.drawable.ic_qs_network_adb_off));
        tile.setLabel(getApplication().getResources().getString(R.string.app_name));
        tile.updateTile();
    }

    private void showStateUnavailable() {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_UNAVAILABLE);
        tile.updateTile();
    }

    class Listener implements Commander.WadbStateChangeListener {

        @Override
        public void onWadbStart(String ip, int port) {
            showStateOn(ip, port);
        }

        @Override
        public void onWadbStop() {
            showStateOff();
        }
    }

}
