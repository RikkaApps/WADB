package moe.haruue.wadb.ui.fragment;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

import moe.haruue.util.StandardUtils;
import moe.haruue.wadb.R;
import moe.haruue.wadb.data.Commands;
import moe.haruue.wadb.presenter.Commander;
import moe.haruue.wadb.ui.activity.LaunchActivity;
import moe.haruue.wadb.ui.service.NotificationHelper;
import moe.haruue.wadb.util.ScreenKeeper;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class MainFragment extends PreferenceFragment {

    SwitchPreference wadbSwitchPreference;
    EditTextPreference portPreference;
    Listener listener = new Listener();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Commander.addChangeListener(listener);
        Commander.addFailureListener(listener);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        wadbSwitchPreference = (SwitchPreference) findPreference("pref_key_wadb_switch");
        portPreference = (EditTextPreference) findPreference("pref_key_wadb_port");
        Commander.checkWadbState();
    }

    private void init() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Commander.removeChangeListener(listener);
        Commander.removeFailureListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onResume() {
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
            // refresh switch
            wadbSwitchPreference.setChecked(true);
            wadbSwitchPreference.setSummaryOn(ip + ":" + port);
            // refresh port
            portPreference.setText(port + "");
            portPreference.setSummary(port + "");
            wadbSwitchPreference.setEnabled(true);
        }

        @Override
        public void onWadbStop() {
            // refresh switch
            wadbSwitchPreference.setChecked(false);
            wadbSwitchPreference.getEditor().putBoolean("pref_key_wadb_switch", false).commit();
            // refresh port
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(StandardUtils.getApplication());
            String port = sharedPreferences.getString("pref_key_wadb_port", "5555");
            portPreference.setSummary(port);
            portPreference.setText(port);
            wadbSwitchPreference.setEnabled(true);
        }

        //TODO replace with compat fragment
        @TargetApi(Build.VERSION_CODES.M)
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
                    if (sharedPreferences.getBoolean("pref_key_wadb_switch", false)) {
                        NotificationHelper.start(StandardUtils.getApplication());
                    } else {
                        NotificationHelper.stop(StandardUtils.getApplication());
                    }
                    break;
                case "pref_key_hide_launcher_icon":
                    if (sharedPreferences.getBoolean("pref_key_hide_launcher_icon", false)) {
                        LaunchActivity.hideLaunchIcon(getContext());
                    } else {
                        LaunchActivity.showLaunchIcon(getContext());
                    }
                    break;
                case "pref_key_wake_lock":
                    if (sharedPreferences.getBoolean("pref_key_wake_lock", false) && sharedPreferences.getBoolean("pref_key_wadb_switch", false)) {
                        ScreenKeeper.acquireWakeLock();
                    } else {
                        ScreenKeeper.releaseWakeLock();
                    }
                    break;
                case "pref_key_wadb_port":
                    String port = sharedPreferences.getString("pref_key_wadb_port", "5555");
                    try {
                        int p = Integer.parseInt(port);
                        if (p < 1025 || p > 65535) {
                            throw new NumberFormatException(getText(R.string.bad_port_number).toString());
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(StandardUtils.getApplication(), R.string.bad_port_number, Toast.LENGTH_SHORT).show();
                        port = "5555";
                        e.printStackTrace();
                    }
                    portPreference.setText(port);
                    portPreference.setSummary(port);
                    Commands.getWadbState(new Commands.AbstractCommandsListener() {
                        @Override
                        public void onGetAdbState(boolean isWadb, int port) {
                            if (isWadb) {
                                Commander.startWadb();
                            }
                        }
                    });
                    break;
            }
        }
    }

}
