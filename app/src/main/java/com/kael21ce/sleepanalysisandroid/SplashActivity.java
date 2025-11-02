package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.kael21ce.sleepanalysisandroid.data.BlockStatusResponse;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManager;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;
import com.kael21ce.sleepanalysisandroid.data.RetrofitClient;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private final Handler dotHandler = new Handler();
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
        loadingImage.setImageDrawable(getDrawable(R.drawable.sleepwake_logo2));
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
        healthConnectManager.setIsSleepDone(false);
        healthConnectManager.setAddSleepDone(false);


        long currentTime = System.currentTimeMillis();
        long sleepOnset = sharedPref.getLong("sleepOnset", currentTime);
        long workOnset = sharedPref.getLong("workOnset", currentTime);
        long workOffset = sharedPref.getLong("workOffset", currentTime);
        long sleepOnsetShow = sharedPref.getLong("sleepOnsetShow", currentTime);

        Long[] updatedDates = updateOnsetDate(currentTime, sleepOnset, sleepOnsetShow, workOnset, workOffset);
        sleepOnsetShow = updatedDates[1];
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong("sleepOnset", updatedDates[0]);
        editor.putLong("sleepOnsetShow", updatedDates[1]);
        editor.putLong("workOnset", updatedDates[2]);
        editor.putLong("workOffset", updatedDates[3]);
        editor.apply();
        Log.v("SplashActivity", "Onset: " + updatedDates[0] + " / Onset Show: " + updatedDates[1] +
                " / Work onset: " + updatedDates[2] + " / Work offset: " + updatedDates[3]);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (getIntent() != null) {
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();

                Intent scheduleIntent = getIntent();
                int year = scheduleIntent.getIntExtra("Year", calendar.get(Calendar.YEAR));
                int month = scheduleIntent.getIntExtra("Month", calendar.get(Calendar.MONTH));
                int day = scheduleIntent.getIntExtra("Day", calendar.get(Calendar.DAY_OF_MONTH));

                Log.v(TAG, "Selected: " + year + "-" + (month + 1) + "-" + day);

                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                mainIntent.putExtra("Year", year);
                mainIntent.putExtra("Month", month);
                mainIntent.putExtra("Day", day);

                // is_blocked 불러온 후 MainActivity로 이동
                RetrofitAPI apiService = RetrofitClient.getClient(SplashActivity.this).create(RetrofitAPI.class);
                BlockStatusResponse.checkBlockStatus(apiService, SplashActivity.this, sharedPref, new BlockStatusResponse.BlockReadCallback() {
                    @Override
                    public void onBlockRead() {
                        Log.d(TAG, "is_blocked 읽기 완료");
                        startActivity(mainIntent);
                        finish();
                    }

                    @Override
                    public void onFailRead() {
                        Log.d(TAG, "is_blocked 읽기 실패");
                    }
                });
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1000);
    }

    private void updateDots() {
        dotHandler.postDelayed(() -> {
            dotCount = (dotCount + 1) % (MAX_DOTS + 1);
            loadingText.setText("Processing"
                    + new String(new char[dotCount]).replace("\0", "."));
            updateDots();
        },500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dotHandler.removeCallbacksAndMessages(null);
    }

    public static Long[] updateOnsetDate(long currentTime, long sleepOnset, long sleepOnsetShow, long workOnset, long workOffset) {
        long oneDayToMils = 1000*60*60*24;
        long tenMinToMils = 1000*60*10;
        long oneHourToMils = 1000*60*60;

        // Keep sleepOnsetShow before workOnset minus 1 day
        while (sleepOnsetShow < workOnset - oneDayToMils) {
            sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            sleepOnset = sleepOnsetShow;
        }

        // Ensure workOnset is after sleepOnset
        while (workOnset < sleepOnset) {
            workOnset = workOnset + oneDayToMils;
        }

        // Ensure workOffset is after workOnset
        while (workOffset < workOnset) {
            workOffset = workOffset + oneDayToMils;
        }

        // Adjust sleepOnset if currentTime is within sleepOnset and workOnset
        if (sleepOnset < currentTime && currentTime < workOnset) {
            while (sleepOnset < currentTime) {
                sleepOnset = currentTime + tenMinToMils;
            }
        }

        // Ensure sleepOnsetShow is not before currentTime
        if (workOnset - oneHourToMils <= sleepOnset) {
            while (sleepOnsetShow < currentTime) {
                sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            }
            sleepOnset = sleepOnsetShow;
        }

        // Repeat the adjustments for sleepOnsetShow, workOnset, and workOffset
        while (sleepOnsetShow < workOnset - oneDayToMils) {
            sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            sleepOnset = sleepOnsetShow;
        }
        while (workOnset < sleepOnset) {
            workOnset = workOnset + oneDayToMils;
        }
        while (workOffset < workOnset) {
            workOffset = workOffset + oneDayToMils;
        }

        // Update work if it is ended
        while (currentTime > workOffset) {
            workOnset = workOnset + oneDayToMils;
            workOffset = workOffset + oneDayToMils;
        }

        return new Long[]{sleepOnset, sleepOnsetShow, workOnset, workOffset};
    }
}