package moe.haruue.wadb.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import moe.haruue.util.abstracts.HaruueActivity;
import moe.haruue.wadb.R;
import moe.haruue.wadb.presenter.Commander;

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
        Commander.addChangeListener(listener);
        Commander.addFailureListener(listener);
        Commander.checkWadbState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Commander.removeChangeListener(listener);
        Commander.removeFailureListener(listener);
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

    class Listener implements View.OnClickListener, Commander.WadbStateChangeListener, Commander.WadbFailureListener {

        boolean init = true;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab_switch:
                    if (isWadb) {
                        Commander.stopWadb();
                    } else {
                        Commander.startWadb();
                    }
                    break;
            }
        }

        @Override
        public void onWadbStart(String ip, int port) {
            if (!isWadb || init) {
                isWadb = true;
                setFabState(true);
                appendToState("Wadb is started. \n\tadb connect " + ip + ":" + port);
            }
            init = false;
        }

        @Override
        public void onWadbStop() {
            if (isWadb || init) {
                isWadb = false;
                setFabState(false);
                appendToState("Wadb is stopped.");
            }
            init = false;
        }

        @Override
        public void onRootPermissionFailure() {
            appendToState(getResources().getString(R.string.permission_error));
        }

        @Override
        public void onStateRefreshFailure() {
            appendToState(getResources().getString(R.string.refresh_state_failure));
        }

        @Override
        public void onOperateFailure() {
            appendToState(getResources().getString(R.string.failed));
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }
}
