package com.example.safetyapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ContactsActivity extends AppCompatActivity {

    private EditText et1, et2, et3, et4, et5;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        et1 = findViewById(R.id.etContact1);
        et2 = findViewById(R.id.etContact2);
        et3 = findViewById(R.id.etContact3);
        et4 = findViewById(R.id.etContact4);
        et5 = findViewById(R.id.etContact5);

        btnSave = findViewById(R.id.btnSaveContacts);

        loadContacts();

        btnSave.setOnClickListener(v -> saveContacts());
    }

    private void loadContacts() {

        SharedPreferences prefs = getSharedPreferences("safety", MODE_PRIVATE);

        et1.setText(prefs.getString("contact1", ""));
        et2.setText(prefs.getString("contact2", ""));
        et3.setText(prefs.getString("contact3", ""));
        et4.setText(prefs.getString("contact4", ""));
        et5.setText(prefs.getString("contact5", ""));
    }

    private void saveContacts() {

        String c1 = formatNumber(et1.getText().toString());
        String c2 = formatNumber(et2.getText().toString());
        String c3 = formatNumber(et3.getText().toString());
        String c4 = formatNumber(et4.getText().toString());
        String c5 = formatNumber(et5.getText().toString());

        if (TextUtils.isEmpty(c1)) {
            Toast.makeText(this,
                    "At least Contact 1 is required",
                    Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("safety", MODE_PRIVATE);

        prefs.edit()
                .putString("contact1", c1)
                .putString("contact2", c2)
                .putString("contact3", c3)
                .putString("contact4", c4)
                .putString("contact5", c5)
                .apply();

        Toast.makeText(this,
                "Contacts saved successfully",
                Toast.LENGTH_SHORT).show();

        finish();
    }

    // 🔥 Auto add +91 if user enters 10 digit number
    private String formatNumber(String number) {

        number = number.trim();

        if (number.isEmpty()) return "";

        if (!number.startsWith("+")) {
            if (number.length() == 10) {
                number = "+91" + number;
            }
        }

        return number;
    }
}