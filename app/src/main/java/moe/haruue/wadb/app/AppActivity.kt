package moe.haruue.wadb.app

import android.app.ActivityManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import moe.haruue.wadb.R
import moe.haruue.wadb.util.ThemeHelper.*
import rikka.core.res.resolveColor
import rikka.material.app.MaterialActivity

abstract class AppActivity : MaterialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateTaskDescription()
    }

    override fun computeUserThemeKey(): String? {
        return getTheme()
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        theme.applyStyle(getThemeStyleRes(), true)
    }

    private fun updateTaskDescription() {
        val color: Int = theme.resolveColor(R.attr.appBarColor)
        val icon: Int = when (getTheme()) {
            THEME_WHITE -> {
                R.drawable.ic_task_icon_black
            }
            THEME_PINK -> {
                R.drawable.ic_task_icon_black
            }
            THEME_CLASSIC -> {
                R.drawable.ic_task_icon_white
            }
            else -> {
                R.drawable.ic_task_icon_white
            }
        }

        if (Build.VERSION.SDK_INT >= 28) {
            setTaskDescription(ActivityManager.TaskDescription(null, icon, color))
        } else {
            val drawable = ContextCompat.getDrawable(this, icon)

            val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            @Suppress("DEPRECATION")
            setTaskDescription(ActivityManager.TaskDescription(null, bitmap, color))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun shouldApplyTranslucentSystemBars(): Boolean {
        return true
    }
}
