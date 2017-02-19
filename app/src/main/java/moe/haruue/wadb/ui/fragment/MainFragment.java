package moe.haruue.wadb.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.Toast;

import moe.haruue.util.StandardUtils;
import moe.haruue.wadb.R;
import moe.haruue.wadb.presenter.Commander;
import moe.haruue.wadb.ui.activity.LaunchActivity;
import moe.haruue.wadb.ui.service.NotificationService;
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
        init();
        Commander.checkWadbState();
    }

    private void init() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(StandardUtils.getApplication());
        String port = sharedPreferences.getString("pref_key_wadb_port", "5555");
        portPreference.setSummary(port);
        portPreference.setText(port);
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
                case "pref_key_hide_launcher_icon":
                    if (sharedPreferences.getBoolean("pref_key_hide_launcher_icon", false)) {
                        LaunchActivity.hideLaunchIcon(getContext());
                    } else {
                        LaunchActivity.showLaunchIcon(getContext());
                    }
                    break;
                case "pref_key_wake_lock":
                    if (sharedPreferences.getBoolean("pref_key_wake_lock", false)) {
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
                    break;
            }
        }
    }

}
