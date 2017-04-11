package moe.haruue.wadb.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import moe.haruue.util.StandardUtils;
import moe.haruue.util.abstracts.HaruueActivity;
import moe.haruue.wadb.BuildConfig;
import moe.haruue.wadb.R;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class LaunchActivity extends RikkaActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.start(this);
        finish();
    }

    private static ComponentName getLaunchActivityComponentName() {
        return new ComponentName(BuildConfig.APPLICATION_ID, LaunchActivity.class.getName());
    }

    public static void hideLaunchIcon(Context context) {
        context.getPackageManager().setComponentEnabledSetting(
                getLaunchActivityComponentName(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
        StandardUtils.toast(R.string.tip_on_hide_launch_icon);
    }

    public static void showLaunchIcon(Context context) {
        context.getPackageManager().setComponentEnabledSetting(
                getLaunchActivityComponentName(),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP
        );
        StandardUtils.toast(R.string.tip_on_show_launch_icon);
    }
}
