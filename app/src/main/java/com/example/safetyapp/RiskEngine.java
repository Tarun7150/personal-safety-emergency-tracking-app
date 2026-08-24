package com.example.safetyapp;

import android.content.Context;

import java.util.Calendar;

public class RiskEngine {

    public static int calculateRisk(Context context,
                                    double lat,
                                    double lon,
                                    float speed,
                                    long stationaryTime) {

        int riskScore = 0;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        // 🔴 RULE 1: Night time risk
        if (hour >= 22 || hour <= 5) {
            riskScore += 2;
        }

        // 🔴 RULE 2: Low movement (user not moving)
        if (speed < 1 && stationaryTime > 300000) { // 5 min
            riskScore += 2;
        }

        // 🔴 RULE 3: Network weak
        if (!NetworkUtil.isInternetAvailable(context)) {
            riskScore += 2;
        }

        // 🔴 RULE 4: Unknown area (basic version)
        if (!isKnownLocation(lat, lon)) {
            riskScore += 1;
        }

        return riskScore;
    }

    private static boolean isKnownLocation(double lat, double lon) {
        // 🔧 For now always false (upgrade later)
        return false;
    }
}