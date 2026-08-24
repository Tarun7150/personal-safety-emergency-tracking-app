package com.example.safetyapp;

import android.content.Context;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

public class NetworkMonitor {

    private Context context;

    private TelephonyManager telephonyManager;

    public NetworkMonitor(Context context) {

        this.context = context;

        telephonyManager =
                (TelephonyManager)
                        context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void startMonitoring() {

        PhoneStateListener listener =
                new PhoneStateListener() {

                    @Override
                    public void onSignalStrengthsChanged(
                            SignalStrength signalStrength) {

                        super.onSignalStrengthsChanged(signalStrength);

                        Log.d("NETWORK", "Signal Changed");
                    }

                    @Override
                    public void onDisplayInfoChanged(
                            @NonNull TelephonyDisplayInfo info) {

                        super.onDisplayInfoChanged(info);

                        Log.d("NETWORK", "Network Changed");
                    }
                };

        telephonyManager.listen(
                listener,
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
        );

        checkCellTower();
    }

    private void checkCellTower() {

        try {

            List<CellInfo> cells =
                    telephonyManager.getAllCellInfo();

            if (cells == null) return;

            for (CellInfo cell : cells) {

                if (cell instanceof CellInfoLte) {

                    CellIdentityLte identity =
                            ((CellInfoLte) cell)
                                    .getCellIdentity();

                    int cid = identity.getCi();

                    Log.d("NETWORK", "Cell ID: " + cid);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}