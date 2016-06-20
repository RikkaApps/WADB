package moe.haruue.wadb;

import android.util.Log;

import moe.haruue.util.abstracts.HaruueApplication;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class App extends HaruueApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("App", "Application.onCreate() invoked");
    }
}
