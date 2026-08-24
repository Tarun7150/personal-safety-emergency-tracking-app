package com.example.safetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class MainActivity extends AppCompatActivity {

    private Button btnSOS, btnContacts, btnTimer, btnFakePin;

    private TextView tvLocation;

    private FusedLocationProviderClient fusedClient;

    private LocationCallback locationCallback;

    private double currentLat = 0;

    private double currentLon = 0;

    private static final int REQ_PERMISSIONS = 101;

    private NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // ================= INIT =================

        tvLocation = findViewById(R.id.tvLocation);

        btnSOS = findViewById(R.id.btnSOS);

        btnContacts = findViewById(R.id.btnContacts);

        btnTimer = findViewById(R.id.btnTimer);

        btnFakePin = findViewById(R.id.btnFakePin);

        fusedClient =
                LocationServices
                        .getFusedLocationProviderClient(this);

        // ================= NETWORK MONITOR =================

        networkMonitor = new NetworkMonitor(this);

        networkMonitor.startMonitoring();

        // ================= PERMISSIONS =================

        requestPermissionsIfNeeded();

        // ================= BUTTONS =================

        btnContacts.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            MainActivity.this,
                            ContactsActivity.class
                    )
            );
        });

        btnTimer.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            MainActivity.this,
                            TimerActivity.class
                    )
            );
        });

        btnFakePin.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            MainActivity.this,
                            FakepinActivity.class
                    )
            );
        });

        btnSOS.setOnClickListener(v -> {

            handleSOS();
        });
    }

    // ================= PERMISSIONS =================

    private void requestPermissionsIfNeeded() {

        String[] permissions = {

                Manifest.permission.ACCESS_FINE_LOCATION,

                Manifest.permission.SEND_SMS,

                Manifest.permission.READ_PHONE_STATE
        };

        boolean granted = true;

        for (String permission : permissions) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
            ) != PackageManager.PERMISSION_GRANTED) {

                granted = false;

                break;
            }
        }

        if (!granted) {

            ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    REQ_PERMISSIONS
            );

        } else {

            startLiveLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQ_PERMISSIONS) {

            boolean granted = true;

            for (int result : grantResults) {

                if (result != PackageManager.PERMISSION_GRANTED) {

                    granted = false;

                    break;
                }
            }

            if (granted) {

                startLiveLocation();

            } else {

                Toast.makeText(
                        this,
                        "Permissions required",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    // ================= LIVE LOCATION =================

    private void startLiveLocation() {

        tvLocation.setText("Getting GPS location...");

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

            return;
        }

        locationCallback =
                new LocationCallback() {

                    @Override
                    public void onLocationResult(
                            LocationResult result
                    ) {

                        if (result == null) return;

                        for (Location location :
                                result.getLocations()) {

                            currentLat =
                                    location.getLatitude();

                            currentLon =
                                    location.getLongitude();

                            tvLocation.setText(
                                    "Lat: "
                                            + currentLat
                                            + "\nLon: "
                                            + currentLon
                            );
                        }
                    }
                };

        fusedClient.requestLocationUpdates(
                request,
                locationCallback,
                getMainLooper()
        );
    }

    // ================= SOS =================

    private void handleSOS() {

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

        String locationLink =
                "https://maps.google.com/?q="
                        + currentLat
                        + ","
                        + currentLon;

        String message =
                "🚨 EMERGENCY SOS!\n\n"
                        + "I need help.\n\n"
                        + "Live Location:\n"
                        + locationLink;

        int successCount = 0;

        StringBuilder fallbackNumbers =
                new StringBuilder();

        for (String number : contacts) {

            if (number != null
                    && !number.trim().isEmpty()) {

                boolean sent =
                        sendDirectSMS(
                                number.trim(),
                                message
                        );

                if (sent) {

                    successCount++;

                } else {

                    if (fallbackNumbers.length() > 0) {

                        fallbackNumbers.append(";");
                    }

                    fallbackNumbers.append(number.trim());
                }
            }
        }

        // ================= FALLBACK =================

        if (fallbackNumbers.length() > 0) {

            openSMSApp(
                    fallbackNumbers.toString(),
                    message
            );
        }

        // ================= TRACKING =================

        Intent serviceIntent =
                new Intent(
                        MainActivity.this,
                        TrackingService.class
                );

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {

            startForegroundService(serviceIntent);

        } else {

            startService(serviceIntent);
        }

        // ================= RESULT =================

        if (successCount > 0) {

            Toast.makeText(
                    this,
                    "SOS sent to "
                            + successCount
                            + " contacts",
                    Toast.LENGTH_LONG
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Could not send SMS directly",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ================= DIRECT SMS =================

    private boolean sendDirectSMS(
            String number,
            String message
    ) {

        try {

            SmsManager smsManager;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                smsManager =
                        this.getSystemService(SmsManager.class);

            } else {

                smsManager =
                        SmsManager.getDefault();
            }

            smsManager.sendTextMessage(
                    number,
                    null,
                    message,
                    null,
                    null
            );

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    // ================= FALLBACK SMS APP =================

    private void openSMSApp(
            String numbers,
            String message
    ) {

        try {

            Intent intent =
                    new Intent(Intent.ACTION_SENDTO);

            intent.setData(
                    Uri.parse("smsto:" + numbers)
            );

            intent.putExtra(
                    "sms_body",
                    message
            );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "SMS app not found",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ================= DESTROY =================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (locationCallback != null) {

            fusedClient.removeLocationUpdates(
                    locationCallback
            );
        }
    }
}