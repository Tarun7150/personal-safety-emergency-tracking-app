package com.example.safetyapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

public class NetworkUtil {

    public static boolean isInternetAvailable(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            NetworkCapabilities capabilities =
                    cm.getNetworkCapabilities(cm.getActiveNetwork());

            if (capabilities == null) return false;

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        } else {

            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
    }
}