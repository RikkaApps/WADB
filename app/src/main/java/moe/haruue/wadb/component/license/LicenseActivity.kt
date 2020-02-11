package moe.haruue.wadb.component.license

import android.os.Bundle
import android.webkit.WebView
import moe.haruue.wadb.R
import moe.haruue.wadb.app.AppBarActivity
import rikka.core.res.resolveColor

class LicenseActivity : AppBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)

        appBar?.setDisplayHomeAsUpEnabled(true)

        val licenseView = findViewById<WebView>(R.id.webview_license)
        licenseView.loadUrl("file:///android_asset/license.html")
    }
}
