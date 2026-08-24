package com.example.safetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class FakepinActivity extends AppCompatActivity {

    TextView pinDots;
    String enteredPin = "";

    private static final int PIN_LENGTH = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fakepin);

        pinDots = findViewById(R.id.textView4);

        int[] ids = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int i = 0; i <= 9; i++) {
            final String digit = String.valueOf(i);
            findViewById(ids[i]).setOnClickListener(v -> addDigit(digit));
        }
    }

    private void addDigit(String digit) {
        if (enteredPin.length() < PIN_LENGTH) {
            enteredPin += digit;
            updateDots();
        }

        if (enteredPin.length() == PIN_LENGTH) {
            validatePin();
        }
    }

    private void updateDots() {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < enteredPin.length(); i++) {
            dots.append("• ");
        }
        pinDots.setText(dots.toString().trim());
    }

    private void validatePin() {

        SharedPreferences prefs =
                getSharedPreferences("safety", MODE_PRIVATE);

        String realPin = prefs.getString("user_pin", "1234");

        if (enteredPin.equals(realPin)) {

            // ✅ REAL STOP
            stopSOS();

        } else {

            // ❌ FAKE CANCEL
            fakeStop();
        }

        // 🔥 VERY IMPORTANT FIX (RESET PIN)
        enteredPin = "";
        pinDots.setText("");
    }

    // ================= REAL STOP =================
    private void stopSOS() {

        stopService(new Intent(this, TrackingService.class));

        Toast.makeText(this,
                "SOS Stopped",
                Toast.LENGTH_SHORT).show();

        finish();
    }

    // ================= FAKE STOP =================
    private void fakeStop() {

        Toast.makeText(this,
                "SOS Stopped",
                Toast.LENGTH_SHORT).show();

        sendSilentAlert();

        finish();
    }

    // ================= ALERT =================
    private void sendSilentAlert() {

        // 🔍 DEBUG TO CONFIRM FUNCTION IS CALLED
        Toast.makeText(this,
                "Sending alert...",
                Toast.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this,
                    "SMS Permission Denied",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs =
                getSharedPreferences("safety", MODE_PRIVATE);

        String number =
                prefs.getString("trusted_number", null);

        if (number == null) {
            Toast.makeText(this,
                    "No contact saved",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String message =
                "⚠️ ALERT: Wrong PIN entered. User may be in danger.";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, null, null);

            Toast.makeText(this,
                    "Alert SMS sent",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this,
                    "SMS Failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}