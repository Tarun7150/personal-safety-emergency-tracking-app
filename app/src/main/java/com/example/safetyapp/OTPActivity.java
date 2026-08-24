package com.example.safetyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    EditText etOTP;
    Button btnVerify;

    String verificationId;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        etOTP = findViewById(R.id.etOTP);
        btnVerify = findViewById(R.id.btnVerify);

        mAuth = FirebaseAuth.getInstance();

        String phone = getIntent().getStringExtra("phone");

        sendOTP(phone);

        btnVerify.setOnClickListener(v -> {
            String code = etOTP.getText().toString().trim();

            if (code.isEmpty()) {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            verifyCode(code);
        });
    }

    private void sendOTP(String phone) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    signIn(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String id,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    verificationId = id;
                }
            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);
        signIn(credential);
    }

    private void signIn(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // 🔥 SAVE LOGIN STATE
                        SharedPreferences prefs =
                                getSharedPreferences("app", MODE_PRIVATE);

                        prefs.edit()
                                .putBoolean("logged_in", true)
                                .apply();

                        Toast.makeText(this,
                                "Login Successfully",
                                Toast.LENGTH_SHORT).show();

                        Intent intent =
                                new Intent(OTPActivity.this, MainActivity.class);

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);

                        finish();
                    } else {
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}