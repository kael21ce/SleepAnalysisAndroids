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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectAvailability;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManager;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManagerKt;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private Handler dotHandler = new Handler();
    private int dotCount = 0;
    private final int MAX_DOTS = 3;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Set the gif image
        ImageView loadingImage = findViewById(R.id.LoadingImage);
        Glide.with(this).load(R.raw.loading).into(loadingImage);
        loadingText = findViewById(R.id.LoadingText);
        updateDots();

        HealthConnectManager healthConnectManager = new HealthConnectManager(getApplicationContext());

        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        long twoWeeks = (1000*60*60*24*14);
        long lastSleepUpdate = sharedPref.getLong("lastSleepUpdate", System.currentTimeMillis() - twoWeeks);
        Log.v("LAST SLEEP UPDATE", String.valueOf(lastSleepUpdate));

        Instant now = Instant.now();
        Instant ILastSleepUpdate = Instant.ofEpochMilli(lastSleepUpdate);

        healthConnectManager.javReadSleepInputs(ILastSleepUpdate, now);
        Instant curTime = Instant.now();

        while(!healthConnectManager.getIsSleepDone() || !healthConnectManager.getIsAddSleepDone()){
            Instant curTimeUpdated = Instant.now();
            if(curTime.plusMillis(1000*2).isAfter(curTimeUpdated)){
                break;
            }
//            Log.v("loading", "loading");

        }
        healthConnectManager.setIsSleepDone(false);
        healthConnectManager.setAddSleepDone(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getIntent() != null) {
                    Date date = new Date();
                    Calendar calendar = Calendar.getInstance();

                    Intent scheduleIntent = getIntent();
                    int year = scheduleIntent.getIntExtra("Year", calendar.get(Calendar.YEAR));
                    int month = scheduleIntent.getIntExtra("Month", calendar.get(Calendar.MONTH));
                    int day = scheduleIntent.getIntExtra("Day", calendar.get(Calendar.DAY_OF_MONTH));

                    Log.v(TAG, "Selected: " + year + "-" + month + 1 + "-" + day);

                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    mainIntent.putExtra("Year", year);
                    mainIntent.putExtra("Month", month);
                    mainIntent.putExtra("Day", day);
                    startActivity(mainIntent);
                    finish();
                } else {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }
        }, 1000);
    }

    private void updateDots() {
        dotHandler.postDelayed(() -> {
            dotCount++;
            if (dotCount > MAX_DOTS) {
                loadingText.setText("Processing");
            } else {
                loadingText.setText("Processing"
                        + new String(new char[dotCount]).replace("\0", "."));
            }
            updateDots();
        },500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dotHandler.removeCallbacksAndMessages(null);
    }
}