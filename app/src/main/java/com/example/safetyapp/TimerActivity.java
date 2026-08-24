package com.example.safetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TimerActivity extends AppCompatActivity {

    private TextView tvCountdown;
    private Button btnStart5Min, btnCancel;

    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;

    private static final long FIVE_MINUTES = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        // Match XML IDs exactly
        tvCountdown = findViewById(R.id.tvCountdown);
        btnStart5Min = findViewById(R.id.btnStart5Min);
        btnCancel = findViewById(R.id.btnCancel);

        btnStart5Min.setOnClickListener(v -> startTimer());
        btnCancel.setOnClickListener(v -> cancelTimer());
    }

    private void startTimer() {

        if (timerRunning) return;

        countDownTimer = new CountDownTimer(FIVE_MINUTES, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                tvCountdown.setText(
                        String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {

                timerRunning = false;
                tvCountdown.setText("00:00");

                Toast.makeText(TimerActivity.this,
                        "Timer expired! SOS triggered.",
                        Toast.LENGTH_LONG).show();

                triggerSOS();
            }
        };

        countDownTimer.start();
        timerRunning = true;

        Toast.makeText(this,
                "Safety timer started",
                Toast.LENGTH_SHORT).show();
    }

    private void cancelTimer() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        timerRunning = false;
        tvCountdown.setText("Cancelled");

        Toast.makeText(this,
                "Timer cancelled",
                Toast.LENGTH_SHORT).show();
    }

    private void triggerSOS() {

        Intent intent = new Intent(this, TrackingService.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        startActivity(new Intent(
                this,
                LiveTrackingActivity.class));
    }
}