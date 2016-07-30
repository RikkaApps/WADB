package moe.haruue.wadb.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import moe.haruue.util.ActivityCollector;
import moe.haruue.util.StandardUtils;
import moe.haruue.wadb.R;
import moe.haruue.wadb.presenter.Commander;
import moe.haruue.wadb.ui.service.NotificationService;

public class MainActivity extends PreferenceActivity {

    SwitchPreference wadbSwitchPreference;

    Listener listener = new Listener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.push(this);
        StandardUtils.initializeInActivity(this);
        Commander.addChangeListener(listener);
        Commander.addFailureListener(listener);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        wadbSwitchPreference = (SwitchPreference) findPreference("pref_key_wadb_switch");
        Commander.checkWadbState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Commander.removeChangeListener(listener);
        Commander.removeFailureListener(listener);
        ActivityCollector.pop(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    class Listener implements Commander.WadbStateChangeListener, Commander.WadbFailureListener, SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onRootPermissionFailure() {

        }

        @Override
        public void onStateRefreshFailure() {
            onWadbStop();
        }

        @Override
        public void onOperateFailure() {

        }

        @Override
        public void onWadbStart(String ip, int port) {
            wadbSwitchPreference.setChecked(true);
            wadbSwitchPreference.setSummaryOn(ip + ":" + port);
            wadbSwitchPreference.setEnabled(true);
        }

        @Override
        public void onWadbStop() {
            wadbSwitchPreference.setChecked(false);
            wadbSwitchPreference.getEditor().putBoolean("pref_key_wadb_switch", false).commit();
            wadbSwitchPreference.setEnabled(true);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            switch (s) {
                case "pref_key_wadb_switch":
                    if (wadbSwitchPreference.isEnabled()) {
                        wadbSwitchPreference.setEnabled(false);
                        if (sharedPreferences.getBoolean("pref_key_wadb_switch", false)) {
                            Commander.startWadb();
                        } else {
                            Commander.stopWadb();
                        }
                    }
                    break;
                case "pref_key_notification":
                    if (sharedPreferences.getBoolean("pref_key_notification", true)) {
                        if (sharedPreferences.getBoolean("pref_key_wadb_switch", false)) {
                            NotificationService.start(StandardUtils.getApplication());
                        }
                    } else {
                        try {
                            NotificationService.stop(StandardUtils.getApplication());
                        } catch (Throwable t) {
                            StandardUtils.printStack(t);
                        }
                    }
                    break;
            }
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }
}
