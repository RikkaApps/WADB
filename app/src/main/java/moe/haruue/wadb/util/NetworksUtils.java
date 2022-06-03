package moe.haruue.wadb.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.system.OsConstants;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NetworksUtils {
    private static final String TAG = NetworksUtils.class.getSimpleName();

    public static String getLocalIPAddress(Context context) {
        List<LibWADB.InterfaceIPPair> ipInfoList = getLocalIPInfo(context);
        if (ipInfoList.isEmpty()) {
            return "";
        }
        return ipInfoList.get(0).getIp();
    }

    public static List<LibWADB.InterfaceIPPair> getLocalIPInfo(Context context) {
        List<LibWADB.InterfaceIPPair> result = null;
        try {
            result = LibWADB.getInterfaceIps(false);
        } catch (Exception e) {
            Log.e(TAG, "getLocalIPInfo: LibWADB.getInterfaceIps() failed", e);
        }

        if (result != null) {
            return result;
        }

        // fallback to the legacy way
        result = new ArrayList<>();
        WifiManager wifiManger = context.getApplicationContext().getSystemService(WifiManager.class);
        if (wifiManger == null) {
            return result;
        }
        WifiInfo wifiInfo = wifiManger.getConnectionInfo();
        if (wifiInfo.getIpAddress() != 0) {
            LibWADB.InterfaceIPPair info = new LibWADB.InterfaceIPPair(0, (byte) OsConstants.AF_INET, "wlan0", intToIp(wifiInfo.getIpAddress()));
            result.add(info);
        }
        return result;
    }


    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

}
