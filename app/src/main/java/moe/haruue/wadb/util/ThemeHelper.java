package moe.haruue.wadb.util;

import androidx.annotation.StyleRes;

import moe.haruue.wadb.R;
import moe.haruue.wadb.WadbApplication;

public class ThemeHelper {

    private static final String TAG = "ThemeHelper";

    public static final String THEME_DEFAULT = "default";
    public static final String THEME_TEAL = "teal";
    public static final String THEME_PINK = "pink";

    public static final String KEY_LIGHT_THEME = "pref_light_theme";

    public static void setLightTheme(String theme) {
        WadbApplication.getDefaultSharedPreferences().edit().putString(KEY_LIGHT_THEME, theme).apply();
    }

    public static String getTheme() {
        return WadbApplication.getDefaultSharedPreferences().getString(KEY_LIGHT_THEME, THEME_DEFAULT);
    }

    @StyleRes
    public static int getThemeStyleRes() {
        switch (getTheme()) {
            case THEME_TEAL:
                return R.style.ThemeOverlay_Teal;
            case THEME_PINK:
                return R.style.ThemeOverlay_Pink;

            case THEME_DEFAULT:
            default:
                return R.style.ThemeOverlay;
        }
    }
}
