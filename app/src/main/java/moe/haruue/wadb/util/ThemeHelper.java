package moe.haruue.wadb.util;

import androidx.annotation.StyleRes;

import moe.haruue.wadb.R;
import moe.haruue.wadb.WadbApplication;

public class ThemeHelper {

    private static final String TAG = "ThemeHelper";

    public static final String THEME_CLASSIC = "classic";
    public static final String THEME_WHITE = "white";
    public static final String THEME_PINK = "pink";

    public static final String KEY_LIGHT_THEME = "pref_light_theme";

    public static void setLightTheme(String theme) {
        WadbApplication.getDefaultSharedPreferences().edit().putString(KEY_LIGHT_THEME, theme).apply();
    }

    public static String getTheme() {
        return WadbApplication.getDefaultSharedPreferences().getString(KEY_LIGHT_THEME, THEME_CLASSIC);
    }

    @StyleRes
    public static int getThemeStyleRes() {
        switch (getTheme()) {
            case THEME_CLASSIC:
                return R.style.ThemeOverlay_Classic;

            case THEME_WHITE:
                return R.style.ThemeOverlay_White;
            case THEME_PINK:
                return R.style.ThemeOverlay_Pink;

            default:
                return R.style.ThemeOverlay;
        }
    }
}
