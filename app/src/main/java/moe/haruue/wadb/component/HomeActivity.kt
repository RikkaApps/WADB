package moe.haruue.wadb.component

import android.app.Dialog
import android.content.pm.PackageManager
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
import moe.haruue.wadb.R
import moe.haruue.wadb.app.AppBarFragmentActivity
import rikka.html.text.HtmlCompat
import rikka.html.text.toHtml

class HomeActivity : AppBarFragmentActivity() {

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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_about) {
            val context = this
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
            (dialog.findViewById<View>(R.id.design_about_title) as TextView).text = getString(R.string.wireless_adb_short)
            (dialog.findViewById<View>(R.id.design_about_version) as TextView).apply {
                movementMethod = LinkMovementMethod.getInstance()
                this.text = text
            }
            (dialog.findViewById<View>(R.id.design_about_info) as TextView).isVisible = false
            true
        } else super.onOptionsItemSelected(item)
    }
}
