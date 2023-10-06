package com.kael21ce.sleepanalysisandroid;


import static androidx.activity.compose.ActivityResultRegistryKt.rememberLauncherForActivityResult;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.runtime.MutableState;
import androidx.health.connect.client.permission.HealthPermission;
import androidx.health.connect.client.records.SleepSessionRecord;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectAvailability;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManager;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManagerKt;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();
    RecommendFragment recommendFragment = new RecommendFragment();
    SettingFragment settingFragment = new SettingFragment();

    //health connect
    private static Context context;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        HealthConnectManager healthConnectManager = new HealthConnectManager(context);
        CompletableFuture<Boolean> javHasPermissions = healthConnectManager.javHasAllPermissions();
        Boolean hasPermissions = true;
        try {
            hasPermissions = javHasPermissions.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        MutableState
                <HealthConnectAvailability> availability = healthConnectManager.getAvailability();

        if(availability.getValue() == HealthConnectAvailability.INSTALLED){
            Log.v("test", "test1");
        }else{
            Log.v("test2", "test2");
        }

        if(hasPermissions){
            Log.v("yay", "you did it");
        }else{
            Log.v("no permission", "no permission");
            requestPermissionLauncher.launch(HealthConnectManagerKt.getJAVPERMISSIONS());
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, homeFragment).commit();
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>Sleepwake</font>"));

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.tabHome) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.mainFrame, homeFragment).commit();
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>Sleepwake</font>"));
                            return true;
                        } else if (item.getItemId() == R.id.tabSchedule) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.mainFrame, scheduleFragment).commit();
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>수면 기록</font>"));
                            return true;
                        } else if (item.getItemId() == R.id.tabRecommend) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.mainFrame, recommendFragment).commit();
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>추천 수면</font>"));
                            return true;
                        } else if (item.getItemId() == R.id.tabSetting) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.mainFrame, settingFragment).commit();
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>설정</font>"));
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
        );
    }
    public static Context getAppContext() {
        return MainActivity.context;
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
