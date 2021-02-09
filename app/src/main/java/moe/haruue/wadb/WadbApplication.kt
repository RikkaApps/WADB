package moe.haruue.wadb

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION
import moe.haruue.wadb.events.Events
import moe.haruue.wadb.events.WadbFailureEvent
import moe.haruue.wadb.events.WadbStateChangedEvent
import moe.haruue.wadb.receiver.BootCompletedReceiver
import moe.haruue.wadb.util.NetworksUtils
import moe.haruue.wadb.util.NotificationHelper.cancelNotification
import moe.haruue.wadb.util.NotificationHelper.showNotification
import moe.haruue.wadb.util.ScreenKeeper
import rikka.material.app.DayNightDelegate
import rikka.sui.Sui

lateinit var wadbApplication: WadbApplication

class WadbApplication : Application(), WadbStateChangedEvent, WadbFailureEvent {

    companion object {

        val defaultSharedPreferenceName: String
            get() = BuildConfig.APPLICATION_ID + "_preferences"

        @JvmStatic
        val defaultSharedPreferences: SharedPreferences
            get() {
                var context: Context? = wadbApplication
                if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context = wadbApplication.createDeviceProtectedStorageContext()
                    context.moveSharedPreferencesFrom(wadbApplication, defaultSharedPreferenceName)
                }
                return context!!.getSharedPreferences(defaultSharedPreferenceName, MODE_PRIVATE)
            }

        @JvmStatic
        val wadbPort: String
            get() {
                val port = defaultSharedPreferences.getString(WadbPreferences.KEY_WAKE_PORT, "5555")
                var p: Int
                try {
                    p = port!!.toInt()
                    if (p < 1025 || p > 65535) {
                        p = 5555
                        defaultSharedPreferences.edit().putString(WadbPreferences.KEY_WAKE_PORT, "5555").apply()
                    }
                } catch (e: Throwable) {
                    p = 5555
                    defaultSharedPreferences.edit().putString(WadbPreferences.KEY_WAKE_PORT, "5555").apply()
                }
                return p.toString()
            }

    }

    override fun onCreate() {
        super.onCreate()
        Events.registerAll(this)
        DayNightDelegate.setApplicationContext(this)
        DayNightDelegate.setDefaultNightMode(DayNightDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    override fun onWadbStarted(port: Int) {
        val preferences = defaultSharedPreferences
        preferences.edit().putString(WadbPreferences.KEY_WAKE_PORT, Integer.toString(port)).apply()
        val ip = NetworksUtils.getLocalIPAddress(this)
        if (preferences.getBoolean(WadbPreferences.KEY_NOTIFICATION, true)) {
            showNotification(this, ip, port)
        }
        if (preferences.getBoolean(WadbPreferences.KEY_WAKE_LOCK, false)) {
            ScreenKeeper.acquireWakeLock(this)
        }
    }

    override fun onWadbStopped() {
        cancelNotification(this)
        ScreenKeeper.releaseWakeLock()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        wadbApplication = this
        Sui.init(base.packageName)
    }

    private inline val launcherActivity get() = ComponentName.createRelative(packageName, ".ui.activity.LaunchActivity")

    fun isLauncherActivityEnabled(): Boolean {
        val state = wadbApplication.packageManager.getComponentEnabledSetting(launcherActivity)
        return state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT || state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    fun disableLauncherActivity() {
        wadbApplication.packageManager.setComponentEnabledSetting(
                launcherActivity,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        )
    }

    fun enableLauncherActivity() {
        wadbApplication.packageManager.setComponentEnabledSetting(
                launcherActivity,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP
        )
    }

    private inline val bootCompletedReceiver get() = ComponentName.createRelative(packageName, BootCompletedReceiver::class.java.name)

    fun isBootCompletedReceiverEnabled(): Boolean {
        val state = wadbApplication.packageManager.getComponentEnabledSetting(bootCompletedReceiver)
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    fun disableBootCompletedReceiver() {
        wadbApplication.packageManager.setComponentEnabledSetting(
                bootCompletedReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        )
    }

    fun enableBootCompletedReceiver() {
        wadbApplication.packageManager.setComponentEnabledSetting(
                bootCompletedReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        )
    }
}