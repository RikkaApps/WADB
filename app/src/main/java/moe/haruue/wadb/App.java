package moe.haruue.wadb;

import moe.haruue.util.*;
import moe.haruue.util.abstracts.HaruueApplication;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class App extends HaruueApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        StandardUtils.setDebug(BuildConfig.DEBUG);
    }
}
