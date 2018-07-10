package moe.haruue.wadb;

import com.topjohnwu.superuser.Shell;

import moe.haruue.util.*;
import moe.haruue.util.abstracts.HaruueApplication;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class App extends Shell.ContainerApp {

    @Override
    public void onCreate() {
        super.onCreate();
        StandardUtils.initialize(this);
        ActivityCollector.initialize();
        InstanceSaver.initialize();
        ThreadUtils.initialize(this);
        StandardUtils.setDebug(BuildConfig.DEBUG);
    }
}
