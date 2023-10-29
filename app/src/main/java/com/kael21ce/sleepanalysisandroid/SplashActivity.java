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

        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        long twoWeeks = (1000*60*60*24*14);
        long lastSleepUpdate = sharedPref.getLong("lastSleepUpdate", System.currentTimeMillis() - twoWeeks);
        Log.v("LAST SLEEP UPDATE", String.valueOf(lastSleepUpdate));

        Instant now = Instant.now();
        Instant ILastSleepUpdate = Instant.ofEpochMilli(lastSleepUpdate);

        healthConnectManager.javReadSleepInputs(ILastSleepUpdate, now);
        Instant curTime = Instant.now();

        while(!healthConnectManager.getIsSleepDone()){
            Instant curTimeUpdated = Instant.now();
            if(curTime.plusMillis(1000*2).isAfter(curTimeUpdated)){
                break;
            }
//            Log.v("loading", "loading");

        }
        healthConnectManager.setIsSleepDone(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1000);
    }
}