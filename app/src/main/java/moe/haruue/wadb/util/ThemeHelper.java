package moe.haruue.wadb.util;

import android.content.Context;

import androidx.annotation.StyleRes;

import moe.haruue.wadb.R;
import moe.haruue.wadb.WadbApplication;

public class ThemeHelper {

    private static final String TAG = "ThemeHelper";

    private static final String THEME_CLASSIC = "classic";
    private static final String THEME_WHITE = "white";
    private static final String THEME_TEAL = "teal";
    private static final String THEME_PINK = "pink";
    private static final String THEME_BLUE = "blue";

    public static final String KEY_LIGHT_THEME = "pref_light_theme";

    public static void setLightTheme(String theme) {
        WadbApplication.getDefaultSharedPreferences().edit().putString(KEY_LIGHT_THEME, theme).apply();
    }

    public static String getTheme(Context context) {
        return WadbApplication.getDefaultSharedPreferences().getString(KEY_LIGHT_THEME, THEME_CLASSIC);
    }

    @StyleRes
    public static int getThemeStyleRes(Context context) {
        switch (getTheme(context)) {
            case THEME_CLASSIC:
                return R.style.ThemeOverlay_Classic;

            case THEME_WHITE:
                return R.style.ThemeOverlay_White;
            case THEME_TEAL:
                return R.style.ThemeOverlay_Teal;
            case THEME_PINK:
                return R.style.ThemeOverlay_Pink;
            case THEME_BLUE:
                return R.style.ThemeOverlay_Blue;

            default:
                return R.style.ThemeOverlay;
        }
    }
}
