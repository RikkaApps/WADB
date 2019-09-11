package moe.haruue.wadb.app;

import android.app.ActivityManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import moe.haruue.wadb.R;
import moe.shizuku.support.utils.ResourceUtils;

public abstract class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateTaskDescription();
        if (shouldApplyTranslucentSystemBars()) {
            applyTranslucentSystemBars();
        }
    }

    private void updateTaskDescription() {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_task_icon);
        //noinspection ConstantConditions
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        int color = getColor(R.color.primary_color_light);
        setTaskDescription(new ActivityManager.TaskDescription(null, bitmap, color));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean shouldApplyTranslucentSystemBars() {
        return Build.VERSION.SDK_INT >= 27;
    }

    public void applyTranslucentSystemBars() {
        View root = findViewById(android.R.id.content);
        if (root == null) {
            return;
        }

        int paddingTop = root.getPaddingTop();
        int paddingLeft = root.getPaddingLeft();
        int paddingRight = root.getPaddingLeft();
        int paddingBottom = root.getPaddingTop();

        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            View list = findViewById(android.R.id.list);
            if (list != null) {
                root.setPadding(paddingLeft + insets.getSystemWindowInsetLeft(),
                        paddingTop + insets.getSystemWindowInsetTop(), paddingRight + insets.getSystemWindowInsetRight(), paddingBottom);

                list.setPadding(list.getPaddingLeft(), list.getPaddingTop(), list.getPaddingRight(), insets.getSystemWindowInsetBottom());
            } else {
                root.setPadding(paddingLeft + insets.getSystemWindowInsetLeft(),
                        paddingTop + insets.getSystemWindowInsetTop(),
                        paddingRight + insets.getSystemWindowInsetRight(),
                        paddingBottom + insets.getStableInsetBottom());
            }

            if (insets.getSystemWindowInsetBottom() >= Resources.getSystem().getDisplayMetrics().density * 40) {
                int alpha = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 0xe0000000 : 0x60000000;
                getWindow().setNavigationBarColor(ResourceUtils.resolveColor(getTheme(), android.R.attr.navigationBarColor) & 0x00ffffff | alpha);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    getWindow().setNavigationBarDividerColor(ResourceUtils.resolveColor(getTheme(), android.R.attr.navigationBarDividerColor));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        getWindow().setNavigationBarContrastEnforced(true);
                    }
                }
            }

            return insets.consumeSystemWindowInsets();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setNavigationBarDividerColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getWindow().setNavigationBarContrastEnforced(true);
            }
        }
    }
}
