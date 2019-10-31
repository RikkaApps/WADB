package moe.haruue.wadb.component.license

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.webkit.WebView

import moe.haruue.wadb.R
import moe.haruue.wadb.app.AppBarActivity
import rikka.core.res.resolveColor

class LicenseActivity : AppBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_license)

        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        val licenseView = findViewById<WebView>(R.id.webview_license)
        licenseView.loadUrl("file:///android_asset/license.html")
    }

    override fun onApplyTranslucentSystemBars() {
        window.statusBarColor = theme.resolveColor(android.R.attr.colorPrimary)
    }
}
