package com.crouzet.cavalec.heydude.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

/**
 * Created by Johan on 26/02/2015.
 */
public class Utils {

    public static String getIP(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }
}
