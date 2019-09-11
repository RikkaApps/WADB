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
import moe.haruue.wadb.events.Events;
import moe.haruue.wadb.events.GlobalRequestHandler;
import moe.haruue.wadb.events.WadbFailureEvent;
import moe.haruue.wadb.events.WadbStateChangedEvent;
import moe.haruue.wadb.util.NetworksUtils;
import moe.haruue.wadb.util.NotificationHelper;
import moe.haruue.wadb.util.ScreenKeeper;
import moe.shizuku.preference.EditTextPreference;
import moe.shizuku.preference.PreferenceFragment;
import moe.shizuku.preference.TwoStatePreference;

import static moe.haruue.wadb.WadbPreferences.KEY_ABOUT;
import static moe.haruue.wadb.WadbPreferences.KEY_LAUNCHER_ICONS;
import static moe.haruue.wadb.WadbPreferences.KEY_NOTIFICATION;
import static moe.haruue.wadb.WadbPreferences.KEY_NOTIFICATION_LOW_PRIORITY;
import static moe.haruue.wadb.WadbPreferences.KEY_WADB_SWITCH;
import static moe.haruue.wadb.WadbPreferences.KEY_WAKE_LOCK;
import static moe.haruue.wadb.WadbPreferences.KEY_WAKE_PORT;

public class HomeFragment extends PreferenceFragment implements WadbStateChangedEvent, WadbFailureEvent, SharedPreferences.OnSharedPreferenceChangeListener {

    private TwoStatePreference switchPreference;
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
        switchPreference = (TwoStatePreference) findPreference(KEY_WADB_SWITCH);
        portPreference = (EditTextPreference) findPreference(KEY_WAKE_PORT);

        TwoStatePreference launcherIconPreference = (TwoStatePreference) findPreference(KEY_LAUNCHER_ICONS);
        launcherIconPreference.setChecked(!WadbApplication.isLauncherActivityEnabled(context));

        findPreference(KEY_ABOUT).setSummary(getString(R.string.copyright) + "\n" + getString(R.string.about_text));

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

            if (switchPreference.isChecked()) {
                GlobalRequestHandler.startWadb(port);
            }
            return true;
        });

        launcherIconPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                WadbApplication.disableLauncherActivity(context);
                Toast.makeText(context, R.string.tip_on_hide_launch_icon, Toast.LENGTH_SHORT).show();
            } else {
                WadbApplication.enableLauncherActivity(context);
                Toast.makeText(context, R.string.tip_on_show_launch_icon, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            switchPreference.setEnabled(false);
            portPreference.setEnabled(false);
            if ((boolean) newValue) {
                GlobalRequestHandler.startWadb(WadbApplication.getWadbPort());
            } else {
                GlobalRequestHandler.stopWadb();
            }
            return false;
        });

        switchPreference.setChecked(GlobalRequestHandler.getWadbPort() != -1);
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
        switchPreference.setChecked(true);
        switchPreference.setSummaryOn(ip + ":" + port);
        // refresh port
        portPreference.setText(Integer.toString(port));
        portPreference.setSummary(Integer.toString(port));
        switchPreference.setEnabled(true);
        portPreference.setEnabled(true);
    }

    @Override
    public void onWadbStopped() {
        // refresh switch
        switchPreference.setChecked(false);

        switchPreference.setEnabled(true);
        portPreference.setEnabled(true);
    }

    @Override
    public void onRootPermissionFailure() {
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
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        final Context context = requireContext();
        switch (key) {
            // refresh notification when notification preferences are changed
            case KEY_NOTIFICATION:
            case KEY_NOTIFICATION_LOW_PRIORITY:
                if (switchPreference.isChecked()) {
                    if (preferences.getBoolean(KEY_NOTIFICATION, true)) {
                        GlobalRequestHandler.checkWadbState();
                    }
                } else {
                    NotificationHelper.cancelNotification(context);
                }
                break;
            case KEY_WAKE_LOCK:
                if (preferences.getBoolean(key, false) && switchPreference.isChecked()) {
                    ScreenKeeper.acquireWakeLock(context);
                } else {
                    ScreenKeeper.releaseWakeLock();
                }
                break;
        }
    }

}
