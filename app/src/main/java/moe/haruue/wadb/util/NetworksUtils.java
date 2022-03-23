package moe.haruue.wadb.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import moe.haruue.wadb.events.GlobalRequestHandler;

public class NetworksUtils {

    public static String getLocalIPAddress(Context context) {
        WifiManager wifiManger = context.getApplicationContext().getSystemService(WifiManager.class);
        if (wifiManger == null) return intToIp(0);
        WifiInfo wifiInfo = wifiManger.getConnectionInfo();

        // WLAN unavailable; if possible retrieve hotspot ip
        if (wifiInfo.getIpAddress() == 0) {
            String result = GlobalRequestHandler.getRetrieveIP("wlan0");
            if (!result.isEmpty()) return result;
            result = GlobalRequestHandler.getRetrieveIP("wlan1");
            if (!result.isEmpty()) return result;
        }
        return intToIp(wifiInfo.getIpAddress());
    }


    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

}
