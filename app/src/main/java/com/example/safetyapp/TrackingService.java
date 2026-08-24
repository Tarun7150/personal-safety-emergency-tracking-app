package com.example.safetyapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class TrackingService extends Service {

    private static final String CHANNEL_ID = "tracking_channel";

    private FusedLocationProviderClient fusedClient;

    private LocationCallback locationCallback;

    private DatabaseReference dbRef;

    // ================= MOVEMENT =================

    private long lastMoveTime =
            System.currentTimeMillis();

    private double lastLat = 0;

    private double lastLon = 0;

    // ================= SMS COOLDOWN =================

    private long lastSMSTime = 0;

    private static final long SMS_COOLDOWN = 150000;

    @Override
    public void onCreate() {

        super.onCreate();

        fusedClient =
                LocationServices
                        .getFusedLocationProviderClient(this);

        dbRef =
                FirebaseDatabase
                        .getInstance()
                        .getReference("users");

        createNotificationChannel();

        Notification notification =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setContentTitle("Safety App")
                        .setContentText("Live tracking active")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .build();

        startForeground(1, notification);

        startLocationUpdates();
    }

    // ================= LOCATION =================

    private void startLocationUpdates() {

        LocationRequest request =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        10000
                )
                        .setMinUpdateIntervalMillis(5000)
                        .build();

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            stopSelf();

            return;
        }

        locationCallback =
                new LocationCallback() {

                    @Override
                    public void onLocationResult(
                            LocationResult result
                    ) {

                        if (result == null) return;

                        for (android.location.Location location :
                                result.getLocations()) {

                            double lat =
                                    location.getLatitude();

                            double lon =
                                    location.getLongitude();

                            float speed =
                                    location.getSpeed();

                            long currentTime =
                                    System.currentTimeMillis();

                            // ================= MOVEMENT =================

                            if (distance(
                                    lat,
                                    lon,
                                    lastLat,
                                    lastLon
                            ) > 5) {

                                lastMoveTime =
                                        currentTime;

                                lastLat = lat;

                                lastLon = lon;
                            }

                            long stationaryTime =
                                    currentTime
                                            - lastMoveTime;

                            // ================= RISK ENGINE =================

                            int risk =
                                    RiskEngine.calculateRisk(
                                            TrackingService.this,
                                            lat,
                                            lon,
                                            speed,
                                            stationaryTime
                                    );

                            Log.d(
                                    "RISK",
                                    "Risk Score: " + risk
                            );

                            // ================= HIGH RISK =================

                            if (risk >= 5) {

                                Log.d(
                                        "ALERT",
                                        "⚠ HIGH RISK DETECTED"
                                );

                                showHighRiskNotification();

                                sendSMSIfOffline(
                                        lat,
                                        lon
                                );
                            }

                            // ================= FIREBASE =================

                            uploadToFirebase(
                                    lat,
                                    lon
                            );
                        }
                    }
                };

        fusedClient.requestLocationUpdates(
                request,
                locationCallback,
                null
        );
    }

    // ================= FIREBASE =================

    private void uploadToFirebase(
            double lat,
            double lon
    ) {

        Map<String, Object> data =
                new HashMap<>();

        data.put("lat", lat);

        data.put("lon", lon);

        data.put(
                "time",
                System.currentTimeMillis()
        );

        SharedPreferences prefs =
                getSharedPreferences(
                        "user_session",
                        MODE_PRIVATE
                );

        String userId =
                prefs.getString(
                        "username",
                        "user1"
                );

        dbRef.child(userId)
                .setValue(data);
    }

    // ================= SMS =================

    private void sendSMSIfOffline(
            double lat,
            double lon
    ) {

        if (NetworkUtil.isInternetAvailable(this))
            return;

        long currentTime =
                System.currentTimeMillis();

        // ================= COOLDOWN =================

        if (currentTime - lastSMSTime
                < SMS_COOLDOWN)
            return;

        SharedPreferences prefs =
                getSharedPreferences(
                        "safety",
                        MODE_PRIVATE
                );

        String[] contacts = {

                prefs.getString("contact1", ""),

                prefs.getString("contact2", ""),

                prefs.getString("contact3", ""),

                prefs.getString("contact4", ""),

                prefs.getString("contact5", "")
        };

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED)
            return;

        String link =
                "https://maps.google.com/?q="
                        + lat
                        + ","
                        + lon;

        String message =
                "⚠️ HIGH RISK DETECTED!\n\n"
                        + "Location:\n"
                        + link;

        try {

            SmsManager smsManager;

            if (Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.S) {

                smsManager =
                        getSystemService(
                                SmsManager.class
                        );

            } else {

                smsManager =
                        SmsManager.getDefault();
            }

            int sentCount = 0;

            for (String number : contacts) {

                if (number != null
                        && !number.trim().isEmpty()) {

                    smsManager.sendTextMessage(
                            number.trim(),
                            null,
                            message,
                            null,
                            null
                    );

                    sentCount++;
                }
            }

            lastSMSTime = currentTime;

            Log.d(
                    "SMS",
                    "Emergency SMS sent to "
                            + sentCount
                            + " contacts"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ================= DISTANCE =================

    private float distance(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        float[] result = new float[1];

        android.location.Location.distanceBetween(
                lat1,
                lon1,
                lat2,
                lon2,
                result
        );

        return result[0];
    }

    // ================= NOTIFICATION =================

    private void showHighRiskNotification() {

        Notification notification =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setContentTitle("⚠ Danger Detected")
                        .setContentText("High risk situation detected")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .build();

        startForeground(2, notification);
    }

    // ================= CHANNEL =================

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Tracking Service",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {

                manager.createNotificationChannel(
                        channel
                );
            }
        }
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if (locationCallback != null) {

            fusedClient.removeLocationUpdates(
                    locationCallback
            );
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}