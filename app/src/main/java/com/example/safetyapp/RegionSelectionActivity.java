package com.example.safetyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RegionSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 LOGIN CHECK
        SharedPreferences loginPrefs =
                getSharedPreferences("app", MODE_PRIVATE);

        if (loginPrefs.getBoolean("logged_in", false)) {

            startActivity(new Intent(this, MainActivity.class));

            finish();
            return;
        }

        // 🔥 REGION CHECK
        SharedPreferences prefs =
                getSharedPreferences("app_settings", MODE_PRIVATE);

        if (prefs.contains("region")) {

            Intent intent =
                    new Intent(this, CountryPickerActivity.class);

            intent.putExtra(
                    "region",
                    prefs.getString("region", "INDIA"));

            startActivity(intent);

            finish();
            return;
        }

        setContentView(R.layout.activity_region);

        Button btnIndia = findViewById(R.id.btnIndia);
        Button btnOther = findViewById(R.id.btnOther);

        btnIndia.setOnClickListener(v -> saveRegion("INDIA"));

        btnOther.setOnClickListener(v -> saveRegion("GLOBAL"));
    }

    private void saveRegion(String region) {

        SharedPreferences prefs =
                getSharedPreferences("app_settings", MODE_PRIVATE);

        prefs.edit()
                .putString("region", region)
                .apply();

        Intent intent =
                new Intent(this, CountryPickerActivity.class);

        intent.putExtra("region", region);

        startActivity(intent);

        finish();
    }
}