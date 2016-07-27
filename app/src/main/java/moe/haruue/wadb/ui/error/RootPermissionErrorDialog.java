package moe.haruue.wadb.ui.error;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import moe.haruue.util.ActivityCollector;
import moe.haruue.util.StandardUtils;
import moe.haruue.wadb.R;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class RootPermissionErrorDialog {

    public static void show() {
        new AlertDialog.Builder(StandardUtils.getApplication()).setIcon(R.drawable.ic_error_red_24dp)
                .setTitle(StandardUtils.getApplication().getResources().getString(R.string.permission_error))
                .setMessage(StandardUtils.getApplication().getResources().getString(R.string.supersu_tip))
                .setPositiveButton(StandardUtils.getApplication().getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        ActivityCollector.exitApplication();
                    }
                })
                .setCancelable(false)
                .create().show();
    }

}
