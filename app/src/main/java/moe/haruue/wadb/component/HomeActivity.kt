package moe.haruue.wadb.component

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import moe.haruue.wadb.BuildConfig
import moe.haruue.wadb.R
import moe.haruue.wadb.app.AppBarFragmentActivity
import moe.haruue.wadb.util.ThemeHelper
import rikka.core.ktx.unsafeLazy
import rikka.html.text.HtmlCompat
import rikka.html.text.toHtml

class HomeActivity : AppBarFragmentActivity() {

    private val themes by unsafeLazy {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            arrayOf(
                getString(R.string.theme_default),
                getString(R.string.theme_pink),
            ) else
            arrayOf(
                getString(R.string.theme_default),
                getString(R.string.theme_teal),
                getString(R.string.theme_pink),
            )
    }

    private val themesValue = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
        arrayOf(
            ThemeHelper.THEME_DEFAULT,
            ThemeHelper.THEME_PINK
        ) else
        arrayOf(
            ThemeHelper.THEME_DEFAULT,
            ThemeHelper.THEME_TEAL,
            ThemeHelper.THEME_PINK
        )

    private val themesId = themesValue.map { it.hashCode() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val fragment = HomeFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        menu.findItem(R.id.menu_theme).subMenu.apply {
            val currentTheme = ThemeHelper.getTheme()
            for ((index, theme) in themes.withIndex()) {
                add(R.id.menu_theme_group, themesId[index].hashCode(), index, theme).apply {
                    isCheckable = true
                    isChecked = currentTheme == themesValue[index]
                }
            }
            setGroupCheckable(R.id.menu_theme_group, true, true)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                val context = this
                val versionName: String
                try {
                    versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (ignored: PackageManager.NameNotFoundException) {
                    return true
                }
                val text = StringBuilder()
                text.append(versionName)
                    .append("<p>")
                    .append(
                        getString(
                            R.string.open_source_info,
                            "<b><a href=\"${BuildConfig.GITHUB_URL}\">GitHub</a></b>",
                            BuildConfig.LICENSE
                        )
                    )
                val translators = getString(R.string.translators)
                if (translators.isNotBlank()) {
                    text.append("<p>").append(getString(R.string.translation_contributors, translators))
                }
                text.append("<p>").append(BuildConfig.COPYRIGHT)

                val dialog: Dialog = AlertDialog.Builder(context)
                    .setView(R.layout.dialog_about)
                    .show()
                (dialog.findViewById<View>(R.id.design_about_icon) as ImageView).setImageDrawable(context.getDrawable(R.drawable.ic_launcher))
                (dialog.findViewById<View>(R.id.design_about_title) as TextView).text =
                    getString(R.string.wireless_adb_short)
                (dialog.findViewById<View>(R.id.design_about_version) as TextView).apply {
                    movementMethod = LinkMovementMethod.getInstance()
                    this.text = text.toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)
                }
                (dialog.findViewById<View>(R.id.design_about_info) as TextView).isVisible = false
                true
            }
            R.id.menu_translate -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.TRANSLATION_URL)))
                true
            }
            else -> {
                val index = themesId.indexOf(item.itemId)
                if (index == -1) return super.onOptionsItemSelected(item)

                if (ThemeHelper.getTheme() != themesValue[index]) {
                    ThemeHelper.setLightTheme(themesValue[index])
                    recreate()
                }
                return true
            }
        }
    }
}
