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
import androidx.room.Room;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectAvailability;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManager;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManagerKt;
import com.kael21ce.sleepanalysisandroid.data.User;
import com.kael21ce.sleepanalysisandroid.data.UserDao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

        MutableState
                <HealthConnectAvailability> availability = healthConnectManager.getAvailability();
        Boolean hasPermissions = true;

        if((availability.getValue() == HealthConnectAvailability.INSTALLED) && (android.os.Build.VERSION.SDK_INT >= 34)){
            Log.v("test", "test1");
            Log.v("test", String.valueOf(android.os.Build.VERSION.SDK_INT));
            CompletableFuture<Boolean> javHasPermissions = healthConnectManager.javHasAllPermissions();
            try {
                hasPermissions = javHasPermissions.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }else if(availability.getValue() != HealthConnectAvailability.INSTALLED){
            Log.v("test2", "test2");
            startActivity(new Intent(MainActivity.this, PermissionsActivity.class));
        }

        if(hasPermissions){
            Log.v("yay", "you did it");
        }else{
            Log.v("no permission", "no permission");
            requestPermissionLauncher.launch(HealthConnectManagerKt.getJAVPERMISSIONS());
        }
        Instant now = Instant.now();
        Instant yesterday = now.minusSeconds(60*60*24*7);
        healthConnectManager.javReadSleepInputs(yesterday, now);

        //database configuration
//        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
//                AppDatabase.class, "database-name").allowMainThreadQueries().build();
//        UserDao userDao = db.userDao();
//        User test = new User();
//        test.firstName = "test";
//        test.lastName = "test";
//        userDao.insertAll(test);
//        List<User> users = userDao.getAll();
//        Log.v("tag", users.get(0).firstName);

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
