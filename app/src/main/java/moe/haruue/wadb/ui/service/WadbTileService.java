package moe.haruue.wadb.ui.service;

import android.os.Build;
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
    }

    @Override
    public void onClick() {
        super.onClick();
        StandardUtils.initialize(getApplication());
        if (getQsTile().getState() == Tile.STATE_ACTIVE) {
            Commander.stopWadb();
        } else {
            Commander.startWadb();
        }
    }

    private void showStateOn(String ip, int port) {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.setLabel(ip + ":" + port);
        tile.updateTile();
    }

    private void showStateOff() {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
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
