package moe.haruue.wadb.component

import android.app.Dialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import moe.haruue.wadb.R
import moe.haruue.wadb.WadbApplication
import moe.haruue.wadb.WadbPreferences.*
import moe.haruue.wadb.app.AppBarActivity
import moe.haruue.wadb.events.Events
import moe.haruue.wadb.events.GlobalRequestHandler
import moe.haruue.wadb.events.WadbFailureEvent
import moe.haruue.wadb.events.WadbStateChangedEvent
import moe.haruue.wadb.util.NetworksUtils
import moe.haruue.wadb.util.NotificationHelper
import moe.haruue.wadb.util.ScreenKeeper
import moe.haruue.wadb.util.ThemeHelper
import moe.shizuku.preference.*
import rikka.html.text.HtmlCompat
import rikka.html.text.toHtml
import rikka.material.widget.BorderRecyclerView
import rikka.material.widget.BorderView
import rikka.recyclerview.addVerticalPadding
import rikka.recyclerview.fixEdgeEffect

class HomeFragment : PreferenceFragment(), WadbStateChangedEvent, WadbFailureEvent, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var togglePreference: TwoStatePreference
    private lateinit var portPreference: EditTextPreference

    init {
        setHasOptionsMenu(true)
    }

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

        recyclerView.addVerticalPadding()
        recyclerView.fixEdgeEffect()

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
        togglePreference = findPreference(KEY_WADB_SWITCH) as TwoStatePreference
        portPreference = findPreference(KEY_WAKE_PORT) as EditTextPreference

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val checkBoxPreference = findPreference(KEY_NOTIFICATION_LOW_PRIORITY) as CheckBoxPreference
            checkBoxPreference.isVisible = false
        }

        val launcherIconPreference = findPreference(KEY_LAUNCHER_ICONS) as TwoStatePreference
        launcherIconPreference.isChecked = !WadbApplication.isLauncherActivityEnabled(context)

        portPreference.setOnPreferenceChangeListener { _, newValue ->
            val port: String = newValue as String
            val p = try {
                Integer.parseInt(port)
            } catch (e: Throwable) {
                -1
            }
            if (p < 1025 || p > 65535) {
                Toast.makeText(context, R.string.toast_bad_port_number, Toast.LENGTH_SHORT).show()
                return@setOnPreferenceChangeListener false
            }

            if (togglePreference.isChecked) {
                GlobalRequestHandler.startWadb(port)
            }
            true
        }

        launcherIconPreference.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                WadbApplication.disableLauncherActivity(context)
                Toast.makeText(context, R.string.toast_hide_icon, Toast.LENGTH_SHORT).show()
            } else {
                WadbApplication.enableLauncherActivity(context)
                Toast.makeText(context, R.string.toast_show_icon, Toast.LENGTH_SHORT).show()
            }
            true
        }

        togglePreference.setOnPreferenceChangeListener { _, newValue ->
            togglePreference.isEnabled = false
            portPreference.isEnabled = false
            if (newValue as Boolean) {
                GlobalRequestHandler.startWadb(WadbApplication.getWadbPort())
            } else {
                GlobalRequestHandler.stopWadb()
            }
            false
        }

        togglePreference.isChecked = GlobalRequestHandler.getWadbPort() != -1

        findPreference(KEY_NOTIFICATION_SETTINGS).isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        findPreference(KEY_SCREEN_LOCK_SWITCH).isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        val lightThemePreference = findPreference(KEY_LIGHT_THEME) as SimpleMenuPreference
        lightThemePreference.setOnPreferenceChangeListener { _, o ->
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
        togglePreference.isChecked = true
        togglePreference.summaryOn = "$ip:$port"
        // refresh port
        portPreference.text = port.toString()
        portPreference.summary = port.toString()
        togglePreference.isEnabled = true
        portPreference.isEnabled = true
    }

    override fun onWadbStopped() {
        // refresh switch
        togglePreference.isChecked = false

        togglePreference.isEnabled = true
        portPreference.isEnabled = true
    }

    override fun onRootPermissionFailure() {
        val activity = activity
        if (activity == null || activity.isFinishing) {
            return
        }

        onWadbStopped()

        AlertDialog.Builder(activity)
                .setMessage(activity.getString(R.string.dialog_not_rooted_message))
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        val context = requireContext()
        when (key) {
            // refresh notification when notification preferences are changed
            KEY_NOTIFICATION, KEY_NOTIFICATION_LOW_PRIORITY -> if (togglePreference.isChecked) {
                if (preferences.getBoolean(KEY_NOTIFICATION, true)) {
                    GlobalRequestHandler.checkWadbState()
                }
            } else {
                NotificationHelper.cancelNotification(context)
            }
            KEY_WAKE_LOCK -> if (preferences.getBoolean(key, false) && togglePreference.isChecked) {
                ScreenKeeper.acquireWakeLock(context)
            } else {
                ScreenKeeper.releaseWakeLock()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_about) {
            val context = requireContext()
            val versionName: String
            try {
                versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (ignored: PackageManager.NameNotFoundException) {
                return true
            }
            val text = "$versionName<p>${getString(R.string.open_source_info)}<p>${getString(R.string.copyright)}".toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)
            val dialog: Dialog = AlertDialog.Builder(context)
                    .setView(R.layout.dialog_about)
                    .show()
            (dialog.findViewById<View>(R.id.design_about_icon) as ImageView).setImageDrawable(context.getDrawable(R.drawable.ic_launcher))
            (dialog.findViewById<View>(R.id.design_about_title) as TextView).text = getString(R.string.app_name)
            (dialog.findViewById<View>(R.id.design_about_version) as TextView).apply {
                movementMethod = LinkMovementMethod.getInstance()
                this.text = text
            }
            (dialog.findViewById<View>(R.id.design_about_info) as TextView).isVisible = false
            true
        } else super.onOptionsItemSelected(item)
    }
}
