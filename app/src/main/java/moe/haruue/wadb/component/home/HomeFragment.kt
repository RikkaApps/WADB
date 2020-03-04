package moe.haruue.wadb.component.home

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import moe.haruue.wadb.BuildConfig
import moe.haruue.wadb.R
import moe.haruue.wadb.WadbApplication
import moe.haruue.wadb.WadbPreferences.*
import moe.haruue.wadb.app.AppBarActivity
import moe.haruue.wadb.events.Events
import moe.haruue.wadb.events.GlobalRequestHandler
import moe.haruue.wadb.events.WadbFailureEvent
import moe.haruue.wadb.events.WadbStateChangedEvent
import moe.haruue.wadb.util.*
import moe.shizuku.preference.*
import rikka.material.widget.BorderRecyclerView
import rikka.material.widget.BorderView

class HomeFragment : PreferenceFragment(), WadbStateChangedEvent, WadbFailureEvent, SharedPreferences.OnSharedPreferenceChangeListener {

    private var switchPreference: TwoStatePreference? = null
    private var portPreference: EditTextPreference? = null
    private var lightThemePreference: SimpleMenuPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Events.registerAll(this)
        GlobalRequestHandler.checkWadbState()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHelper.createNotificationChannel(requireContext())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Events.unregisterAll(this)
    }

    override fun onCreateItemDecoration(): DividerDecoration {
        return CategoryDivideDividerDecoration()
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater, parent: ViewGroup, savedInstanceState: Bundle?): RecyclerView {
        val recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState) as BorderRecyclerView

        recyclerView.addItemDecoration(VerticalPaddingDecoration(recyclerView.context))

        val lp = recyclerView.layoutParams
        if (lp is FrameLayout.LayoutParams) {
            lp.rightMargin = recyclerView.context.resources.getDimension(R.dimen.rd_activity_horizontal_margin).toInt()
            lp.leftMargin = lp.rightMargin
        }
        recyclerView.borderVisibilityChangedListener = BorderView.OnBorderVisibilityChangedListener { top, _, _, _ ->
            if (activity != null) {
                (activity as AppBarActivity).appBar?.setRaised(!top)
            }
        }

        return recyclerView
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.setStorageDeviceProtected()
        addPreferencesFromResource(R.xml.preferences)
        preferenceManager.sharedPreferencesName = WadbApplication.getDefaultSharedPreferenceName()

        val context = requireContext()
        switchPreference = findPreference(KEY_WADB_SWITCH) as TwoStatePreference
        portPreference = findPreference(KEY_WAKE_PORT) as EditTextPreference

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val checkBoxPreference = findPreference(KEY_NOTIFICATION_LOW_PRIORITY) as CheckBoxPreference
            checkBoxPreference.isVisible = false
        }

        val launcherIconPreference = findPreference(KEY_LAUNCHER_ICONS) as TwoStatePreference
        launcherIconPreference.isChecked = !WadbApplication.isLauncherActivityEnabled(context)

        findPreference(KEY_ABOUT).summary = BuildConfig.VERSION_NAME + '\n' + getString(R.string.copyright)

        portPreference!!.setOnPreferenceChangeListener { _, newValue ->
            val port: String = newValue as String
            val p = Integer.parseInt(port)
            if (p < 1025 || p > 65535) {
                Toast.makeText(context, R.string.bad_port_number, Toast.LENGTH_SHORT).show()
                return@setOnPreferenceChangeListener false
            }

            if (switchPreference!!.isChecked) {
                GlobalRequestHandler.startWadb(port)
            }
            true
        }

        launcherIconPreference.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                WadbApplication.disableLauncherActivity(context)
                Toast.makeText(context, R.string.tip_on_hide_launch_icon, Toast.LENGTH_SHORT).show()
            } else {
                WadbApplication.enableLauncherActivity(context)
                Toast.makeText(context, R.string.tip_on_show_launch_icon, Toast.LENGTH_SHORT).show()
            }
            true
        }

        switchPreference!!.setOnPreferenceChangeListener { _, newValue ->
            switchPreference!!.isEnabled = false
            portPreference!!.isEnabled = false
            if (newValue as Boolean) {
                GlobalRequestHandler.startWadb(WadbApplication.getWadbPort())
            } else {
                GlobalRequestHandler.stopWadb()
            }
            false
        }

        switchPreference!!.isChecked = GlobalRequestHandler.getWadbPort() != -1

        findPreference(KEY_NOTIFICATION_SETTINGS).isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        findPreference(KEY_SCREEN_LOCK_SWITCH).isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        lightThemePreference = findPreference(KEY_LIGHT_THEME) as SimpleMenuPreference

        lightThemePreference!!.setOnPreferenceChangeListener { _, o ->
            if (o is String) {
                val theme = o.toString()
                if (ThemeHelper.getTheme(requireContext()) != theme) {
                    ThemeHelper.setLightTheme(theme)
                    activity?.recreate()
                }
            }
            true
        }

    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onWadbStarted(port: Int) {
        val context = context ?: return

        val ip = NetworksUtils.getLocalIPAddress(context)
        // refresh switch
        switchPreference!!.isChecked = true
        switchPreference!!.summaryOn = "$ip:$port"
        // refresh port
        portPreference!!.text = port.toString()
        portPreference!!.summary = port.toString()
        switchPreference!!.isEnabled = true
        portPreference!!.isEnabled = true
    }

    override fun onWadbStopped() {
        // refresh switch
        switchPreference!!.isChecked = false

        switchPreference!!.isEnabled = true
        portPreference!!.isEnabled = true
    }

    override fun onRootPermissionFailure() {
        val activity = activity
        if (activity == null || activity.isFinishing) {
            return
        }

        onWadbStopped()

        AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.permission_error))
                .setMessage(activity.getString(R.string.supersu_tip))
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(activity.getString(R.string.exit)) { _, _ ->
                    NotificationHelper.cancelNotification(activity)
                    activity.finishAffinity()
                }
                .create()
                .show()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        val context = requireContext()
        when (key) {
            // refresh notification when notification preferences are changed
            KEY_NOTIFICATION, KEY_NOTIFICATION_LOW_PRIORITY -> if (switchPreference!!.isChecked) {
                if (preferences.getBoolean(KEY_NOTIFICATION, true)) {
                    GlobalRequestHandler.checkWadbState()
                }
            } else {
                NotificationHelper.cancelNotification(context)
            }
            KEY_WAKE_LOCK -> if (preferences.getBoolean(key, false) && switchPreference!!.isChecked) {
                ScreenKeeper.acquireWakeLock(context)
            } else {
                ScreenKeeper.releaseWakeLock()
            }
        }
    }

}
