package com.example.safetyapp;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;

public class LiveTrackingActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker userMarker;
    private DatabaseReference locationRef;
    private ValueEventListener locationListener;

    private boolean firstUpdate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tracking);

        // 🔥 FIX: get correct userId from login
        String userId = getSharedPreferences("user_session", MODE_PRIVATE)
                .getString("username", "user1");

        locationRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapLiveTracking);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnSendSOS = findViewById(R.id.btnSendSOS);

        // 🔥 FIX: REMOVE SHARE FUNCTION
        btnSendSOS.setText("SOS Already Sent");

        btnSendSOS.setOnClickListener(v ->
                Toast.makeText(this,
                        "SOS already sent automatically",
                        Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        locationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Double lat = snapshot.child("lat").getValue(Double.class);
                Double lon = snapshot.child("lon").getValue(Double.class);

                if (lat == null || lon == null) return;

                LatLng pos = new LatLng(lat, lon);

                if (userMarker == null) {

                    userMarker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(pos)
                                    .title("Live Location")
                    );

                    mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(pos, 18f)
                    );

                } else {

                    animateMarkerTo(userMarker, pos);

                    mMap.animateCamera(
                            CameraUpdateFactory.newLatLng(pos)
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        locationRef.addValueEventListener(locationListener);
    }

    // ===== Smooth Marker Animation =====
    private void animateMarkerTo(Marker marker, LatLng toPosition) {

        LatLng start = marker.getPosition();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(animation -> {

            float fraction = animation.getAnimatedFraction();

            double lat =
                    (toPosition.latitude - start.latitude) * fraction + start.latitude;

            double lng =
                    (toPosition.longitude - start.longitude) * fraction + start.longitude;

            marker.setPosition(new LatLng(lat, lng));
        });

        animator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (locationListener != null) {
            locationRef.removeEventListener(locationListener);
        }
    }
}