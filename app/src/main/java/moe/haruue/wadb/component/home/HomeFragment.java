package moe.haruue.wadb.component.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import moe.haruue.wadb.R;
import moe.haruue.wadb.WadbApplication;
import moe.haruue.wadb.WadbPreferences;
import moe.haruue.wadb.events.Events;
import moe.haruue.wadb.events.GlobalRequestHandler;
import moe.haruue.wadb.events.WadbFailureEvent;
import moe.haruue.wadb.events.WadbStateChangedEvent;
import moe.haruue.wadb.util.NetworksUtils;
import moe.haruue.wadb.util.NotificationHelper;
import moe.haruue.wadb.util.ScreenKeeper;
import moe.shizuku.preference.EditTextPreference;
import moe.shizuku.preference.PreferenceFragment;
import moe.shizuku.preference.PreferenceManager;
import moe.shizuku.preference.TwoStatePreference;

public class HomeFragment extends PreferenceFragment implements WadbStateChangedEvent, WadbFailureEvent, SharedPreferences.OnSharedPreferenceChangeListener {

    private TwoStatePreference wadbSwitchPreference;
    private EditTextPreference portPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Events.registerAll(this);
        GlobalRequestHandler.checkWadbState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Events.unregisterAll(this);
    }

    @Nullable
    @Override
    public DividerDecoration onCreateItemDecoration() {
        return new CategoryDivideDividerDecoration();
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration() {

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                if (parent.getAdapter() == null) {
                    return;
                }

                if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                    outRect.bottom = Math.round(8 * parent.getContext().getResources().getDisplayMetrics().density);
                }
            }
        };
        recyclerView.addItemDecoration(itemDecoration);

        ViewGroup.LayoutParams _lp = recyclerView.getLayoutParams();
        if (_lp instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) _lp;
            lp.leftMargin = lp.rightMargin = (int) recyclerView.getContext().getResources().getDimension(R.dimen.design_activity_horizontal_margin);
        }

        return recyclerView;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceManager().setSharedPreferencesName(WadbApplication.getDefaultSharedPreferenceName());

        final Context context = requireContext();
        PreferenceManager.setDefaultValues(requireActivity(), R.xml.preferences, false);
        wadbSwitchPreference = (TwoStatePreference) findPreference("pref_key_wadb_switch");
        portPreference = (EditTextPreference) findPreference("pref_key_wadb_port");

        TwoStatePreference launcherIconPreference = (TwoStatePreference) findPreference("pref_key_hide_launcher_icon");
        launcherIconPreference.setChecked(WadbApplication.isLauncherActivity(context));

        portPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            String port;
            try {
                port = (String) newValue;
                int p = Integer.parseInt(port);
                if (p < 1025 || p > 65535) {
                    throw new NumberFormatException("Port must be 1025-65535");
                }
            } catch (Throwable e) {
                e.printStackTrace();

                Toast.makeText(context, R.string.bad_port_number, Toast.LENGTH_SHORT).show();
                return false;
            }

            if (getPreferenceManager().getSharedPreferences().getBoolean("pref_key_wadb_switch", false)) {
                GlobalRequestHandler.startWadb(port);
            }
            return true;
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onWadbStarted(int port) {
        final Context context = getContext();
        if (context == null) {
            return;
        }

        String ip = NetworksUtils.getLocalIPAddress(context);
        // refresh switch
        wadbSwitchPreference.setChecked(true);
        wadbSwitchPreference.setSummaryOn(ip + ":" + port);
        // refresh port
        portPreference.setText(Integer.toString(port));
        portPreference.setSummary(Integer.toString(port));
        wadbSwitchPreference.setEnabled(true);
        portPreference.setEnabled(true);
    }

    @Override
    public void onWadbStopped() {
        // refresh switch
        wadbSwitchPreference.setChecked(false);
        getPreferenceManager().getSharedPreferences().edit().putBoolean("pref_key_wadb_switch", false).apply();

        wadbSwitchPreference.setEnabled(true);
        portPreference.setEnabled(true);
    }

    @Override
    public void onRootPermissionFailure() {
        onWadbStopped();

        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.permission_error))
                .setMessage(activity.getString(R.string.supersu_tip))
                .setPositiveButton(activity.getString(R.string.exit), (dialogInterface, i) -> {
                    NotificationHelper.cancelNotification(activity);
                    activity.finishAffinity();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    public void onOperateFailure() {
        onWadbStopped();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        final Context context = requireContext();
        switch (key) {
            case "pref_key_wadb_switch":
                if (wadbSwitchPreference.isEnabled()) {
                    wadbSwitchPreference.setEnabled(false);
                    portPreference.setEnabled(false);
                    if (preferences.getBoolean(key, false)) {
                        GlobalRequestHandler.startWadb(preferences.getString("pref_key_wadb_port", "5555"));
                    } else {
                        GlobalRequestHandler.stopWadb();
                    }
                }
                break;
            // refresh notification when notification preferences are changed
            case "pref_key_notification":
            case "pref_key_notification_low_priority":
                if (preferences.getBoolean("pref_key_wadb_switch", false)) {
                    if (preferences.getBoolean("pref_key_notification", true)) {
                        GlobalRequestHandler.checkWadbState();
                    }
                } else {
                    NotificationHelper.cancelNotification(context);
                }
                break;
            case "pref_key_hide_launcher_icon":
                if (preferences.getBoolean(key, false)) {
                    WadbApplication.hideLauncherActivity(context);
                    Toast.makeText(context, R.string.tip_on_hide_launch_icon, Toast.LENGTH_SHORT).show();
                } else {
                    WadbApplication.showLauncherActivity(context);
                    Toast.makeText(context, R.string.tip_on_show_launch_icon, Toast.LENGTH_SHORT).show();
                }
                break;
            case WadbPreferences.KEY_WAKE_LOCK:
                if (preferences.getBoolean(key, false) && preferences.getBoolean("pref_key_wadb_switch", false)) {
                    ScreenKeeper.acquireWakeLock(context);
                } else {
                    ScreenKeeper.releaseWakeLock();
                }
                break;
        }
    }

}
