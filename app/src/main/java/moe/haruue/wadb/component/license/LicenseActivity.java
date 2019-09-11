package moe.haruue.wadb.component.license;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import moe.haruue.wadb.R;
import moe.haruue.wadb.app.BaseActivity;

public class LicenseActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_license);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        WebView licenseView = findViewById(R.id.webview_license);
        licenseView.loadUrl("file:///android_asset/license.html");
    }

    @Override
    public boolean shouldApplyTranslucentSystemBars() {
        return false;
    }
}
