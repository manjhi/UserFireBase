package com.omninos.firstmobapp;

import android.content.Context;
import android.net.ConnectivityManager;


public class CommonUtils {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
