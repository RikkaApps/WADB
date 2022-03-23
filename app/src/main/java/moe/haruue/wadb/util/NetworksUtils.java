package moe.haruue.wadb.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.util.ArrayList;

import moe.haruue.wadb.events.GlobalRequestHandler;

public class NetworksUtils {

    public static String getLocalIPAddress(Context context) {
        return getLocalIPAddresses(context).get(0);
    }

    public static ArrayList<String> getLocalIPAddresses(Context context) {
        ArrayList<String> result = new ArrayList<String>();

        WifiManager wifiManger = context.getApplicationContext().getSystemService(WifiManager.class);
        if (wifiManger == null) {
            result.add(intToIp(0));
            return result;
        }
        WifiInfo wifiInfo = wifiManger.getConnectionInfo();
        // WLAN
        if (wifiInfo.getIpAddress() != 0) {
            result.add(intToIp(wifiInfo.getIpAddress()));
        }
        // AP
        String apIp = GlobalRequestHandler.getRetrieveIP("wlan0");
        if (!apIp.isEmpty() && !result.isEmpty() && !result.get(0).equals(apIp))
            result.add(apIp);
        else {
            apIp = GlobalRequestHandler.getRetrieveIP("wlan1");
            if (!apIp.isEmpty())
                result.add(apIp);
            else if (result.isEmpty())
                result.add(intToIp(0));
        }
        return result;
    }


    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

}
