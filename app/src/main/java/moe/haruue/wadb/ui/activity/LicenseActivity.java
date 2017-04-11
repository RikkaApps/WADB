package moe.haruue.wadb.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v7.app.AlertDialog;
import android.view.Window;
import android.webkit.WebView;

import java.io.InputStream;
import java.io.InputStreamReader;

import moe.haruue.util.StandardUtils;
import moe.haruue.util.ThreadUtils;
import moe.haruue.util.abstracts.HaruueActivity;
import moe.haruue.wadb.R;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class LicenseActivity extends RikkaActivity {

    private WebView licenseView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog dialog = new AlertDialog.Builder(LicenseActivity.this)
                .setTitle(R.string.license)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        LicenseActivity.this.finish();
                    }
                })
                .create();
        dialog.show();
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setContentView(R.layout.dialog_license);
        licenseView = (WebView) dialogWindow.findViewById(R.id.webview_license);
        preLoadData();
    }

    private void preLoadData() {
        ThreadUtils.runOnNewThread(new Runnable() {
            @Override
            public void run() {
                loadData(readStringFromRawResource(R.raw.license));
            }
        });
    }

    private void loadData(final String html) {
        ThreadUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    licenseView.loadData(html, "text/html", "UTF-8");
                } catch (Throwable t) {
                    StandardUtils.printStack(t);
                }
            }
        });
    }

    private String readStringFromRawResource(@RawRes int resId) {
        InputStream in = getResources().openRawResource(resId);
        InputStreamReader reader = new InputStreamReader(in);
        char[] flush = new char[10];
        int length;
        StringBuilder sb = new StringBuilder();
        try {
            while (-1 != (length = reader.read(flush))) {
                sb.append(flush, 0, length);
            }
        } catch (Throwable t) {
            StandardUtils.printStack(t);
        }
        return sb.toString();
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, LicenseActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

}
