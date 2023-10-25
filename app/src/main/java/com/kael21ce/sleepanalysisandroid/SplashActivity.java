package com.kael21ce.sleepanalysisandroid;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.runtime.MutableState;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.kael21ce.sleepanalysisandroid.data.HealthConnectAvailability;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManager;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManagerKt;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        HealthConnectManager healthConnectManager = new HealthConnectManager(getApplicationContext());
        MutableState
                <HealthConnectAvailability> availability = healthConnectManager.getAvailability();
        boolean hasPermissions = true;

        //get the availability of health connect and make sure that the build version is 34 to check for permissions
        if((availability.getValue() == HealthConnectAvailability.INSTALLED) && (android.os.Build.VERSION.SDK_INT >= 34)){
            Log.v("ANDROID SDK VERSION", String.valueOf(android.os.Build.VERSION.SDK_INT));
            CompletableFuture<Boolean> javHasPermissions = healthConnectManager.javHasAllPermissions();
            try {
                hasPermissions = javHasPermissions.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //if we have lower android API level then just check if it is installed, user needs to make sure that they give permissions
        }else if(availability.getValue() != HealthConnectAvailability.INSTALLED){
            startActivity(new Intent(SplashActivity.this, PermissionsActivity.class));
        }

        if(hasPermissions){
            Log.v("PERMISSION", "PERMISSION GRANTED");
        }else{
            Log.v("PERMISSION", "PERMISSION HAS NOT BEEN GRANTED, ASKING FOR PERMISSION...");
            requestPermissionLauncher.launch(HealthConnectManagerKt.getJAVPERMISSIONS());
        }

        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        long twoWeeks = (1000*60*60*24*14);

        long lastSleepUpdate = sharedPref.getLong("lastSleepUpdate", System.currentTimeMillis() - twoWeeks);
        Log.v("LAST SLEEP UPDATE", String.valueOf(lastSleepUpdate));
        Instant now = Instant.now();
        Instant ILastSleepUpdate = Instant.ofEpochMilli(lastSleepUpdate);

        healthConnectManager.javReadSleepInputs(ILastSleepUpdate, now);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 4000);
    }

    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                if (isGranted.containsValue(false)) {
                    Log.v("permission not allowed", "permission not allowed");
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                } else {
                    Log.v("ok", "permission granted");
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                }
            });
}