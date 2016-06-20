package moe.haruue.wadb.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import moe.haruue.util.ActivityCollector;
import moe.haruue.util.StandardUtils;
import moe.haruue.util.abstracts.HaruueActivity;
import moe.haruue.wadb.R;
import moe.haruue.wadb.data.Commands;
import moe.haruue.wadb.ui.service.NotificationService;
import moe.haruue.wadb.util.IPUtils;

public class MainActivity extends HaruueActivity {

    Toolbar toolbar;
    TextView infoTextView;
    String currentDisplay = "";
    FloatingActionButton fab;

    boolean isWadb = false;

    Listener listener = new Listener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        initView();
        Commands.checkSUAvailable(this, listener);
    }

    private void initToolbar() {
        toolbar = $(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
    }

    private void initView() {
        infoTextView = $(R.id.wadb_state_info);
        fab = $(R.id.fab_switch);
        fab.setOnClickListener(listener);
    }

    private void refreshInfoTextView() {
        infoTextView.setText(currentDisplay);
    }

    private void appendToState(String s) {
        currentDisplay += s + "\n";
        refreshInfoTextView();
    }

    private void clearState() {
        currentDisplay = "";
        refreshInfoTextView();
    }

    private void setFabState(boolean isWadb) {
        if (isWadb) {
            fab.setImageResource(R.drawable.ic_check_white_24dp);
        } else {
            fab.setImageResource(R.drawable.ic_close_white_24dp);
        }
    }

    class Listener implements View.OnClickListener, Commands.CommandsListener, AlertDialog.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_switch:
                    if (isWadb) {
                        Commands.stopWadb(MainActivity.this, this);
                    } else {
                        Commands.startWadb(MainActivity.this, this);
                    }
                    break;
            }
        }

        @Override
        public void onGetSUAvailable(boolean isAvailable) {
            if (!isAvailable) {
                new AlertDialog.Builder(MainActivity.this).setIcon(R.drawable.ic_error_red_24dp)
                        .setTitle(getResources().getString(R.string.permission_error))
                        .setMessage(getResources().getString(R.string.supersu_tip))
                        .setPositiveButton(getResources().getString(R.string.exit), this)
                        .setCancelable(false)
                        .create().show();
            } else {
                Commands.getWadbState(MainActivity.this, this);
            }
        }

        @Override
        public void onGetAdbState(boolean isWadb, int port) {
            MainActivity.this.isWadb = isWadb;
            setFabState(isWadb);
            if (isWadb) {
                appendToState("Wadb is started. \n\tadb connect " + IPUtils.getLocalIPAddress(getApplication()) + ":" + port);
                NotificationService.start(MainActivity.this);
            } else {
                appendToState("Wadb is stopped.");
                NotificationService.stop(MainActivity.this);
            }
        }

        @Override
        public void onGetAdbStateFailure() {
            StandardUtils.toast(R.string.refresh_state_failure);
            appendToState(getResources().getString(R.string.refresh_state_failure));
            this.onGetAdbState(false, -1);
        }

        @Override
        public void onWadbStartListener(boolean isSuccess) {
            if (!isSuccess) {
                StandardUtils.toast(R.string.failed);
            }
            Commands.getWadbState(MainActivity.this, this);
        }

        @Override
        public void onWadbStopListener(boolean isSuccess) {
            if (!isSuccess) {
                StandardUtils.toast(R.string.failed);
            }
            Commands.getWadbState(MainActivity.this, this);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            ActivityCollector.exitApplication();
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }
}
