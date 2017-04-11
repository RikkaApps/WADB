package moe.haruue.wadb.ui.activity;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import moe.haruue.util.abstracts.HaruueActivity;
import moe.haruue.wadb.R;

/**
 * @author Rikka
 */

public class RikkaActivity extends HaruueActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_task);
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            setTaskDescription(new ActivityManager.TaskDescription(null, bitmap, ContextCompat.getColor(this, R.color.colorPrimary)));
        }
    }
}
