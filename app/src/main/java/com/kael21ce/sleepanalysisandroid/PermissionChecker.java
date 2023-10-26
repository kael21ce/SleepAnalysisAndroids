package com.kael21ce.sleepanalysisandroid;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.runtime.MutableState;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.kael21ce.sleepanalysisandroid.data.HealthConnectAvailability;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManager;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManagerKt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PermissionChecker extends AppCompatActivity {

    HealthConnectManager healthConnectManager;
    MutableState<HealthConnectAvailability> availability;
    @Override
    public void onResume(){
        super.onResume();
        Log.v("CREATED", "CREATED");

        healthConnectManager = new HealthConnectManager(getApplicationContext());
        availability = healthConnectManager.getAvailability();
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
            startActivity(new Intent(PermissionChecker.this, PermissionsActivity.class));
        }

        if(hasPermissions){
            Log.v("PERMISSION", "PERMISSION GRANTED");
            startActivity(new Intent(PermissionChecker.this, SplashActivity.class));
        }else{
            Log.v("PERMISSION", "PERMISSION HAS NOT BEEN GRANTED, ASKING FOR PERMISSION...");
            requestPermissionLauncher.launch(HealthConnectManagerKt.getJAVPERMISSIONS());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_checker);

        Log.v("CREATED", "CREATED");

        healthConnectManager = new HealthConnectManager(getApplicationContext());
        availability = healthConnectManager.getAvailability();
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
            startActivity(new Intent(PermissionChecker.this, PermissionsActivity.class));
        }

        if(hasPermissions){
            Log.v("PERMISSION", "PERMISSION GRANTED");
            startActivity(new Intent(PermissionChecker.this, SplashActivity.class));
        }else{
            Log.v("PERMISSION", "PERMISSION HAS NOT BEEN GRANTED, ASKING FOR PERMISSION...");
            requestPermissionLauncher.launch(HealthConnectManagerKt.getJAVPERMISSIONS());
        }

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