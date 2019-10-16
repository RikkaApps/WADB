package moe.haruue.wadb.app

import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toolbar
import moe.haruue.wadb.R
import rikka.core.res.resolveColor
import rikka.design.widget.AppBarLayout

abstract class AppBarActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.appbar_fragment_activity)

        val toolbarContainer = findViewById<AppBarLayout>(R.id.toolbar_container)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setAppBar(toolbarContainer, toolbar)
    }

    override fun onApplyTranslucentSystemBars() {
        val window = window
        val theme = theme

        window?.statusBarColor = Color.TRANSPARENT

        window?.decorView?.post {
            if (window.decorView.rootWindowInsets?.systemWindowInsetBottom ?: 0 >= Resources.getSystem().displayMetrics.density * 40) {
                val alpha = -0x20000000
                window.navigationBarColor = theme.resolveColor(android.R.attr.colorPrimary) and 0x00ffffff or alpha
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    window.navigationBarDividerColor = theme.resolveColor(android.R.attr.navigationBarDividerColor)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        window.isNavigationBarContrastEnforced = false
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    window.navigationBarColor = Color.TRANSPARENT
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = true
                }
            }
        }
    }
}