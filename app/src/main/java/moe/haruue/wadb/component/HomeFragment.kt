package moe.haruue.wadb.component

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.haruue.wadb.R
import moe.haruue.wadb.WadbApplication
import moe.haruue.wadb.WadbPreferences.*
import moe.haruue.wadb.events.Events
import moe.haruue.wadb.events.GlobalRequestHandler
import moe.haruue.wadb.events.WadbFailureEvent
import moe.haruue.wadb.events.WadbStateChangedEvent
import moe.haruue.wadb.util.NetworksUtils
import moe.haruue.wadb.util.NotificationHelper
import moe.haruue.wadb.util.ScreenKeeper
import moe.haruue.wadb.wadbApplication
import rikka.preference.MainSwitchPreference
import rikka.recyclerview.fixEdgeEffect
import rikka.widget.borderview.BorderRecyclerView

class HomeFragment : PreferenceFragmentCompat(), WadbStateChangedEvent, WadbFailureEvent,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var togglePreference: MainSwitchPreference
    private lateinit var ipPreference: Preference
    private lateinit var portPreference: EditTextPreference

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

    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?
    ): RecyclerView {
        val recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState) as BorderRecyclerView

        recyclerView.fixEdgeEffect()

        val lp = recyclerView.layoutParams
        if (lp is FrameLayout.LayoutParams) {
            lp.rightMargin = recyclerView.context.resources.getDimension(R.dimen.rd_activity_horizontal_margin).toInt()
            lp.leftMargin = lp.rightMargin
        }

        return recyclerView
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.setStorageDeviceProtected()
        addPreferencesFromResource(R.xml.preferences)
        preferenceManager.sharedPreferencesName = WadbApplication.defaultSharedPreferenceName

        val context = requireContext()
        togglePreference = findPreference(KEY_WADB_SWITCH)!!
        ipPreference = findPreference(KEY_WAKE_IP)!!
        portPreference = findPreference(KEY_WAKE_PORT)!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationLowPriorityPreference: TwoStatePreference = findPreference(KEY_NOTIFICATION_LOW_PRIORITY)!!
            notificationLowPriorityPreference.isVisible = false
        }

        portPreference.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
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

        togglePreference.addOnSwitchChangeListener { switchView, isChecked ->
            togglePreference.isEnabled = false
            portPreference.isEnabled = false
            if (isChecked) {
                GlobalRequestHandler.startWadb(WadbApplication.wadbPort)
            } else {
                GlobalRequestHandler.stopWadb()
            }
        }

        val wadbActive = GlobalRequestHandler.getWadbPort() != -1
        togglePreference.isChecked = wadbActive
        ipPreference.isVisible = wadbActive

        findPreference<Preference>(KEY_NOTIFICATION_SETTINGS)!!.isVisible =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        findPreference<Preference>(KEY_SCREEN_LOCK_SWITCH)!!.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        val launcherIconPreference: TwoStatePreference = findPreference(KEY_LAUNCHER_ICONS)!!
        val launcherActivityEnabled = wadbApplication.isLauncherActivityEnabled()

        launcherIconPreference.isChecked = !launcherActivityEnabled
        launcherIconPreference.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(R.string.dialog_hide_icon_message_q)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            wadbApplication.disableLauncherActivity()
                            launcherIconPreference.isChecked = true
                        }
                        .show()
                    false
                } else {
                    wadbApplication.disableLauncherActivity()
                    true
                }
            } else {
                wadbApplication.enableLauncherActivity()
                true
            }
        }

        val bootCompletedReceiverPreference: TwoStatePreference = findPreference("start_on_boot")!!
        bootCompletedReceiverPreference.summary =
            getString(R.string.settings_start_on_boot_summary, getString(R.string.wireless_adb))
        bootCompletedReceiverPreference.isChecked = wadbApplication.isBootCompletedReceiverEnabled()
        bootCompletedReceiverPreference.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                wadbApplication.enableBootCompletedReceiver()
            } else {
                wadbApplication.disableBootCompletedReceiver()
            }
            true
        }

        findPreference<Preference>(KEY_NOTIFICATION)!!.summary =
            getString(R.string.settings_show_notification_summary, getString(R.string.wireless_adb))
        findPreference<Preference>(KEY_WAKE_LOCK)!!.summary =
            getString(R.string.settings_keep_screen_on_summary, getString(R.string.wireless_adb))
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onWadbStarted(port: Int) {
        val context = context ?: return

        val ipInfoList = NetworksUtils.getLocalIPInfo(context)

        // refresh switch
        togglePreference.isChecked = true
        when {
            ipInfoList.isEmpty() -> {
                ipPreference.summary = ""
            }
            ipInfoList.size == 1 -> {
                ipPreference.summary = "${ipInfoList[0].ip}:$port"
            }
            else -> {
                ipPreference.summary = ipInfoList.joinToString(separator = "\n") {
                    val uiInterfaceName = when (it.interfaceName) {
                        "wlan0" -> "WLAN"
                        "wlan1" -> "\t\tAP\t\t"
                        else -> it.interfaceName
                    }
                    "[${uiInterfaceName}]\t${it.ip}:$port"
                }
            }
        }
        // refresh port
        portPreference.text = port.toString()
        togglePreference.isEnabled = true
        portPreference.isEnabled = true
        ipPreference.isVisible = true
    }

    override fun onWadbStopped() {
        // refresh switch
        togglePreference.isChecked = false
        ipPreference.isVisible = false

        togglePreference.isEnabled = true
        portPreference.isEnabled = true
    }

    override fun onRootPermissionFailure() {
        val activity = activity
        if (activity == null || activity.isFinishing) {
            return
        }

        onWadbStopped()

        MaterialAlertDialogBuilder(activity)
            .setMessage(activity.getString(R.string.dialog_not_rooted_message))
            .setPositiveButton(android.R.string.ok, null)
            .create()
            .show()
    }

    override fun onStart() {
        super.onStart()
        GlobalRequestHandler.checkWadbState()
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
}
