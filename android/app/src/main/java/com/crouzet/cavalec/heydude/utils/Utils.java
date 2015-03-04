package com.crouzet.cavalec.heydude.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by Johan on 26/02/2015.
 */
public class Utils {

    public static String getIP(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    public static BitmapDrawable createBitmapFromURL(String url) {
        Bitmap image = null;

        try {
            InputStream in = new java.net.URL(url).openStream();
            image = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //image = Bitmap.createBitmap(image, 0, 0, 50, 50);

        return image == null ? null : new BitmapDrawable(image);
    }
}
