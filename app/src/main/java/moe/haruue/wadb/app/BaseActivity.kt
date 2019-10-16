package moe.haruue.wadb.app

import android.app.ActivityManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import moe.haruue.wadb.R
import rikka.design.app.MaterialActivity

abstract class BaseActivity : MaterialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateTaskDescription()
    }

    private fun updateTaskDescription() {
        val color = getColor(R.color.primary_color_light)

        if (Build.VERSION.SDK_INT >= 28) {
            setTaskDescription(ActivityManager.TaskDescription(null, R.drawable.ic_task_icon, color))
        } else {
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_task_icon)

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
