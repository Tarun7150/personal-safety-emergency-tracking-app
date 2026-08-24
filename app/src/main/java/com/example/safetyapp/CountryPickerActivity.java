package com.example.safetyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.hbb20.CountryCodePicker;

public class CountryPickerActivity extends AppCompatActivity {

    CountryCodePicker ccp;
    EditText phoneInput;
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 LOGIN CHECK
        SharedPreferences prefs =
                getSharedPreferences("app", MODE_PRIVATE);

        if (prefs.getBoolean("logged_in", false)) {

            startActivity(new Intent(this, MainActivity.class));

            finish();
            return;
        }

        setContentView(R.layout.activity_country_picker);

        ccp = findViewById(R.id.countryPicker);
        phoneInput = findViewById(R.id.phoneInput);
        btnContinue = findViewById(R.id.btnContinue);

        ccp.registerCarrierNumberEditText(phoneInput);

        btnContinue.setOnClickListener(v -> {

            String fullNumber =
                    ccp.getFullNumberWithPlus();

            Intent intent =
                    new Intent(this, OTPActivity.class);

            intent.putExtra("phone", fullNumber);

            startActivity(intent);
        });
    }
}