package moe.haruue.wadb.ui.service;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import moe.haruue.util.StandardUtils;
import moe.haruue.wadb.R;
import moe.haruue.wadb.data.Commands;
import moe.haruue.wadb.util.IPUtils;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class WadbTileService extends TileService {

    Listener listener = new Listener();

    @Override
    public void onClick() {
        super.onClick();
        Commands.getWadbState(new Commands.CommandsListener() {
            @Override
            public void onGetSUAvailable(boolean isAvailable) {

            }

            @Override
            public void onGetAdbState(boolean isWadb, int port) {
                if (isWadb) {
                    Commands.stopWadb(listener);
                } else {
                    Commands.startWadb(listener);
                }
            }

            @Override
            public void onGetAdbStateFailure() {
                this.onGetAdbState(false, -1);
            }

            @Override
            public void onWadbStartListener(boolean isSuccess) {

            }

            @Override
            public void onWadbStopListener(boolean isSuccess) {

            }
        });

    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        StandardUtils.initialize(getApplication());
        Commands.checkSUAvailable(listener);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        StandardUtils.initialize(getApplication());
        Commands.getWadbState(listener);
    }

    private void showStateOn(int port) {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_ACTIVE);
        tile.setLabel(IPUtils.getLocalIPAddress(getApplication()) + ":" + port);
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

    class Listener implements Commands.CommandsListener {

        @Override
        public void onGetSUAvailable(boolean isAvailable) {
            if (!isAvailable) {
                showDialog(
                        new AlertDialog.Builder(getApplication()).setIcon(R.drawable.ic_error_red_24dp)
                                .setTitle(getResources().getString(R.string.permission_error))
                                .setMessage(getResources().getString(R.string.supersu_tip))
                                .setPositiveButton(getResources().getString(R.string.exit), null)
                                .setCancelable(true)
                                .create()
                );
                showStateUnavailable();
            } else {
                Commands.getWadbState(this);
            }
        }

        @Override
        public void onGetAdbState(boolean isWadb, int port) {
            if (isWadb) {
                showStateOn(port);
            } else {
                showStateOff();
            }
        }

        @Override
        public void onGetAdbStateFailure() {
            this.onGetAdbState(false, -1);
        }

        @Override
        public void onWadbStartListener(boolean isSuccess) {
            if (!isSuccess) {
                Toast.makeText(getApplication(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
            Commands.getWadbState(this);
        }

        @Override
        public void onWadbStopListener(boolean isSuccess) {
            if (!isSuccess) {
                Toast.makeText(getApplication(), getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }
            Commands.getWadbState(this);
        }
    }

}
