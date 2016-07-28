package moe.haruue.wadb.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import moe.haruue.util.ActivityCollector;
import moe.haruue.util.StandardUtils;
import moe.haruue.util.abstracts.HaruueActivity;
import moe.haruue.wadb.R;
import moe.haruue.wadb.ui.service.NotificationService;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class RootPermissionErrorDialogShadowActivity extends HaruueActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AlertDialog.Builder(this).setIcon(R.drawable.ic_error_red_24dp)
                .setTitle(StandardUtils.getApplication().getResources().getString(R.string.permission_error))
                .setMessage(StandardUtils.getApplication().getResources().getString(R.string.supersu_tip))
                .setPositiveButton(StandardUtils.getApplication().getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        NotificationService.stop(getApplication());
                        ActivityCollector.finishAllActivity();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .setCancelable(false)
                .create().show();
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, RootPermissionErrorDialogShadowActivity.class);
        context.startActivity(starter);
    }
}
