package com.kael21ce.sleepanalysisandroid;


import static androidx.activity.compose.ActivityResultRegistryKt.rememberLauncherForActivityResult;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.room.Room;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectAvailability;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManager;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManagerKt;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.kael21ce.sleepanalysisandroid.data.SleepDao;
import com.kael21ce.sleepanalysisandroid.data.SleepModel;
import com.kael21ce.sleepanalysisandroid.data.V0;
import com.kael21ce.sleepanalysisandroid.data.V0Dao;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();
    RecommendFragment recommendFragment = new RecommendFragment();
    SettingFragment settingFragment = new SettingFragment();
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy"+ "HH:mm", Locale.KOREA);
    //health connect
    private static Context context;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setContentView(R.layout.activity_main);

        context = getApplicationContext();
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
            startActivity(new Intent(MainActivity.this, PermissionsActivity.class));
        }

        if(hasPermissions){
            Log.v("PERMISSION", "PERMISSION GRANTED");
        }else{
            Log.v("PERMISSION", "PERMISSION HAS NOT BEEN GRANTED, ASKING FOR PERMISSION...");
            requestPermissionLauncher.launch(HealthConnectManagerKt.getJAVPERMISSIONS());
        }

        //get the shared preferences variable
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        long twoWeeks = (1000*60*60*24*14);
        long fiveMinutesToMil = (1000*60*50);

        //update variables
        long lastSleepUpdate = sharedPref.getLong("lastSleepUpdate", System.currentTimeMillis() - twoWeeks);
        long lastDataUpdate = sharedPref.getLong("lastUpdate", System.currentTimeMillis() - twoWeeks);

        //user sleep variables
        long sleepOnset = sharedPref.getLong("sleepOnset", System.currentTimeMillis());
        long workOnset = sharedPref.getLong("workOnset", System.currentTimeMillis());
        long workOffset = sharedPref.getLong("workOffset", System.currentTimeMillis());

        //sleep result variables
        long mainSleepStart = sharedPref.getLong("lastUpdate", System.currentTimeMillis() - twoWeeks);
        long mainSleepEnd = sharedPref.getLong("mainSleepEnd", System.currentTimeMillis() - twoWeeks);
        long napSleepStart = sharedPref.getLong("napSleepStart", System.currentTimeMillis() - twoWeeks);
        long napSleepEnd = sharedPref.getLong("napSleepEnd", System.currentTimeMillis() - twoWeeks);

        Instant now = Instant.now();
        Instant ILastSleepUpdate = Instant.ofEpochMilli(lastSleepUpdate);

        //sync health connect data
        Future<Boolean> healthSyncDone = healthConnectManager.javReadSleepInputs(ILastSleepUpdate, now);
        try {
            Boolean result = healthSyncDone.get();
            Log.v("DONE", result.toString());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //database configuration
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "sleep_wake").allowMainThreadQueries().build();

        SleepDao sleepDao = db.sleepDao();

        //get sleep data
        List<Sleep> sleeps = sleepDao.getAll();
        boolean check = false;
        for(Sleep sleep: sleeps){
            String sleepStart = sdfDateTime.format(new Date(sleep.sleepStart));
            String sleepEnd = sdfDateTime.format(new Date(sleep.sleepEnd));
            if(ILastSleepUpdate.isBefore(Instant.ofEpochMilli(sleep.sleepStart))){
                check = true;
            }
        }
        Log.v("SLEEP DATA", "GOT SLEEP DATA");
        if(check){
            //edit lastSleepUpdate to match current time
            editor.putLong("lastSleepUpdate", System.currentTimeMillis());
            editor.apply();
        }

        //get V0 data
        V0Dao v0Dao = db.v0Dao();
        List<V0> v0s = v0Dao.getAll();

        //do pcr simulation
        long yesterday = System.currentTimeMillis() - (1000*60*60*24);
        long startProcess = Long.min(yesterday, lastSleepUpdate);
        long endProcess = System.currentTimeMillis();
        long processDuration = (endProcess - startProcess) / fiveMinutesToMil;
        boolean gotInitV0 = false;
        double[] initV0 = {-0.8590, -0.6837, 0.1140, 14.2133};
        //get init V0
        for(V0 v0: v0s){
            if(v0.time >= startProcess){
                if(!gotInitV0 && v0.time <= startProcess + (1000*60*6)){
                    initV0 = new double[]{v0.y_val, v0.x_val, v0.n_val, v0.H_val};
                    gotInitV0 = true;
                }
            }
        }
        v0Dao.deleteRange(startProcess, endProcess);

        //get the sleep model & simulation result
        SleepModel sleepModel = new SleepModel();
        double[] sleepPattern = sleepToArray(startProcess, endProcess, sleeps);
        ArrayList<double[]> simulationResult = sleepModel.pcr_simulation(initV0, sleepPattern, 5/60.0);

        //update V0 from the simulation
        List<V0> newV0 = new ArrayList<>();
        for(int i = 0; i < simulationResult.size(); i ++){
            double[] res = simulationResult.get(i);
            V0 v0 = new V0();
            v0.y_val = res[0];
            v0.x_val = res[1];
            v0.n_val = res[2];
            v0.H_val = res[3];
            v0.time = startProcess + (i*fiveMinutesToMil);
            newV0.add(v0);

            if(v0.time >= (System.currentTimeMillis()-(1000*60*6)) && (v0.time <= System.currentTimeMillis())){
                initV0 = res;
            }
        }
        v0Dao.insertAll(newV0);

        //process sleep prediction
        int[] sleepSuggestion = sleepModel.Sleep_pattern_suggestion(initV0, (int)sleepOnset, (int)workOnset, (int)workOffset, 5/60.0);

        //update shared preferences
        editor.putLong("mainSleepStart", sleepSuggestion[0]);
        editor.putLong("mainSleepEnd", sleepSuggestion[1]);
        editor.putLong("napSleepStart", sleepSuggestion[2]);
        editor.putLong("napSleepEnd", sleepSuggestion[3]);
        editor.apply();

        //do another simulation for the updated data

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

    //convert sleep from long value to integers array value
    public static double[] sleepToArray(Long sleepStart, Long sleepEnd, List<Sleep> sleeps){
        //for every 1000*60*5 we add a value to the array list
        long fiveMinutesToMil = 1000*60*50;
        int duration = (int)((sleepEnd - sleepStart)/fiveMinutesToMil);
        double[] sleepPattern = new double[duration + 5];
        Arrays.fill(sleepPattern, 0);
        for(Sleep sleep: sleeps){
            long tempSleepStart = sleep.sleepStart / fiveMinutesToMil;
            long tempSleepEnd = sleep.sleepEnd / fiveMinutesToMil;
            int idx = (int)(tempSleepStart - sleepStart);
            int offset = (int)(tempSleepEnd - sleepStart);
            for(int i = idx; i <= offset; i ++){
                sleepPattern[i] = 1.0;
            }
        }
        return sleepPattern;
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

//        long sleepOnset = AppDatabase.sleepOnset;
//        String test = sdfDateTime.format(new Date(sleepOnset));
//        Log.v("tagMain", test);
//
//        AppDatabase.sleepOnset = System.currentTimeMillis()- 1000*60*60*24;

//        Sleep test = new Sleep();
//        test.sleep_id = 1;
//        test.sleepStart = System.currentTimeMillis()- 1000*60*60*24;
//        test.sleepEnd = System.currentTimeMillis();
//        String testInit = sdfDateTime.format(new Date(test.sleepStart));
//        userDao.insertAll(test);

//        String newTime = sdfDateTime.format(new Date(System.currentTimeMillis()));
//        Log.v("time", newTime);