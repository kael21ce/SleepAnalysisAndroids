package com.kael21ce.sleepanalysisandroid;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.Awareness;
import com.kael21ce.sleepanalysisandroid.data.AwarenessDao;
import com.kael21ce.sleepanalysisandroid.data.DataModal;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManager;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.kael21ce.sleepanalysisandroid.data.SleepDao;
import com.kael21ce.sleepanalysisandroid.data.SleepModel;
import com.kael21ce.sleepanalysisandroid.data.V0;
import com.kael21ce.sleepanalysisandroid.data.V0Dao;

import java.security.Permission;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();
    RecommendFragment recommendFragment = new RecommendFragment();
    SettingFragment settingFragment = new SettingFragment();
    private RelativeLayout loadingScreenLayout;
    boolean creation = false;
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy"+ "HH:mm", Locale.KOREA);
    private static final String TAG = "MainActivity";
    private static final String CHECK_CHANNEL_ID = "check_recommend", SURVEY_CHANNEL_ID = "alertness_survey";
    HealthConnectManager healthConnectManager;
    //health connect
    private static Context context;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private static final String NotifyKey = "Notify_At";
    //Time after click back button
    private boolean doubleBackToExitPressedOnce = false;

    private static final int PERMISSION_REQUEST_READ_LOCATION = 0x00000001;

    //for fragment too
    private long mainSleepStart, mainSleepEnd, napSleepStart, napSleepEnd;
    private boolean isearlysleep, isenoughsleep;
    private long sleepOnset, workOnset, workOffset;
    private long lastSleepUpdate, lastDataUpdate, lastBackendUpdate;

    AppDatabase db;
    private List<Sleep> sleeps;
    private List<Awareness> awarenesses;
    private List<V0> v0s;
    String email, username;
    long now, nineHours;
    long twoWeeks = (1000*60*60*24*14), oneDay = 1000*60*60*24;
    long fiveMinutesToMil = (1000*60*5);
    Instant ILastSleepUpdate;
    SleepDao sleepDao;
    V0Dao v0Dao;

    ArrayList<BarEntry> barEntries;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);

        healthConnectManager = new HealthConnectManager(getApplicationContext());
        awarenesses = Collections.synchronizedList(new ArrayList<>());

        //get the shared preferences variable
        sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        //Set the initial notification time
        if (!sharedPref.contains(NotifyKey)) {
            editor.putString(NotifyKey, "21:00").apply();
        }

        //Create channel
        createNotificationChannel(this);
        createSurveyChannel(this);


        //Request the permission of notification in context if API >= 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                //Request again with rational
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    Toast.makeText(this, "추천 수면에 대한 알림을 받기 위해서 권한을 설정해야 합니다.",
                            Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_READ_LOCATION);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_READ_LOCATION);
                }
            }
        }

        //If there is no user email, start onBoarding pages
        if (!sharedPref.contains("User_Name") || !sharedPref.contains("User_Email")) {
            Intent signIntent = new Intent(MainActivity.this, BeginRegisterActivity.class);
            startActivity(signIntent);
        }

        //Show action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }

        //this is in GMT
        nineHours = (1000*60*60*9);
        now = System.currentTimeMillis();
        creation = true;

        //update variables
        lastSleepUpdate = sharedPref.getLong("lastSleepUpdate", now - twoWeeks);
        lastDataUpdate = sharedPref.getLong("lastDataUpdate", now - twoWeeks);
        lastBackendUpdate = sharedPref.getLong("lastBackendUpdate", now - twoWeeks);
        email = sharedPref.getString("User_Email", "tester33");
        username = sharedPref.getString("User_Name", "tester33");

        //user sleep variables
        sleepOnset = sharedPref.getLong("sleepOnset", now);
        workOnset = sharedPref.getLong("workOnset", now);
        workOffset = sharedPref.getLong("workOffset", now);

        barEntries = new ArrayList<BarEntry>();

        if(now > sleepOnset && now < workOnset){
            Log.v("SLEEP ONSET", "SLEEP ONSET IS NOW");
            sleepOnset = now + 1000*60*15;
        }else if(now > workOnset){
            Log.v("NOW", "NOW IS BIGGER THAN WORK ONSET");
            while(now > workOnset){
                sleepOnset += 1000*60*60*24;
                workOnset += 1000*60*60*24;
                workOffset += 1000*60*60*24;
            }
            editor.putLong("sleepOnset", sleepOnset);
            editor.putLong("workOnset", workOnset);
            editor.putLong("workOffset", workOffset);
            editor.apply();
        }

        //sleep result variables
        mainSleepStart = sharedPref.getLong("mainSleepStart", now - twoWeeks);
        mainSleepEnd = sharedPref.getLong("mainSleepEnd", now - twoWeeks);
        napSleepStart = sharedPref.getLong("napSleepStart", now - twoWeeks);
        napSleepEnd = sharedPref.getLong("napSleepEnd", now - twoWeeks);

        ILastSleepUpdate = Instant.ofEpochMilli(lastSleepUpdate);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "sleep_wake").allowMainThreadQueries().build();

        sleepDao = db.sleepDao();

        getSleepData();
        Log.v("SLEEP Sd", Integer.toString(sleeps.size()));
        if(sleeps.size() > 0) {
            do_simulation();
            calculateAwareness();
//            if(now-lastBackendUpdate >= (1000*60*60*24)) {
                sendV0(username);
                lastBackendUpdate = now;
                editor.putLong("lastBackendUpdate", now);
                editor.apply();
//            }
        }
        /*
        //Hide navigation bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
         */


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        boolean isSchedule = sharedPref.getBoolean("isSchedule", false);
        if(isSchedule == true){
            editor.putBoolean("isSchedule", false);
            editor.apply();
            getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, scheduleFragment).commit();
            setBottomNaviItem(R.id.tabSchedule);
        }else {
            getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, homeFragment).commit();
            setBottomNaviItem(R.id.tabHome);
        }
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>SleepWake</font>"));

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.tabHome) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.mainFrame, homeFragment).commit();
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>SleepWake</font>"));
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
        //Cancel the former work request
        if (settingFragment.getRequestedId() != null) {
            WorkManager.getInstance(this).cancelWorkById(settingFragment.getRequestedId());
            Log.v(TAG, "Former request is canceled");
        }

        //Start worker with delay
        String notifyAt = sharedPref.getString(NotifyKey, "21:00");
        long targetTime = timeToSeconds(notifyAt);
        long delay = targetTime - now;
        if (delay < 0) {
            delay += oneDay;
        }
        Log.v(TAG, "Delay of the notification: " + delay);

        OneTimeWorkRequest pushRequest = new OneTimeWorkRequest.Builder(PushWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this).enqueue(pushRequest);
        settingFragment.setRequested(pushRequest);

        long surveyTime, surveyDelay;
        if (!sharedPref.contains("workOnset")) {
            surveyTime = timeToSeconds("13:00");
            surveyDelay = surveyTime - now;
        } else {
            surveyTime = getWorkOnset();
            surveyDelay = surveyTime - now - oneDay;
        }

        if (surveyDelay < 0) {
            surveyDelay += oneDay;
        }
        Log.v(TAG, "Survey delay: " + surveyDelay);
        //Notification for survey
        OneTimeWorkRequest surveyRequest = new OneTimeWorkRequest.Builder(SurveyWorker.class)
                .setInitialDelay(surveyDelay, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this).enqueue(surveyRequest);
    }

    //Create channel for notification of recommendation
    public void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHECK_CHANNEL_ID,
                    "Check Recommendation", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //Create channel for notification for survey
    public void createSurveyChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(SURVEY_CHANNEL_ID,
                    "Alertness Survey", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //Change "HH:mm" to milliseconds
    public long timeToSeconds(String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime time = LocalTime.parse(value, formatter);
        LocalDate currentDate = LocalDate.now();
        ZonedDateTime dateTime = ZonedDateTime.of(currentDate, time, ZoneId.systemDefault());

        return dateTime.toInstant().toEpochMilli();
    }

    //If back button is clicked twice, move out from application
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "\'뒤로\' 버튼을 한 번 더 누르시면 종료됩니다",
                Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(creation == false) {
            Log.v("RESUMING", "RESUMING");
            barEntries = new ArrayList<BarEntry>();

            if (now > sleepOnset && now < workOnset) {
                Log.v("SLEEP ONSET", "SLEEP ONSET IS NOW");
                sleepOnset = now + 1000 * 60 * 15;
            } else if (now > workOnset) {
                Log.v("NOW", "NOW IS BIGGER THAN WORK ONSET");
                while (now > workOnset) {
                    sleepOnset += 1000 * 60 * 60 * 24;
                    workOnset += 1000 * 60 * 60 * 24;
                    workOffset += 1000 * 60 * 60 * 24;
                }
                editor.putLong("sleepOnset", sleepOnset);
                editor.putLong("workOnset", workOnset);
                editor.putLong("workOffset", workOffset);
                editor.apply();
            }

            //sleep result variables
            mainSleepStart = sharedPref.getLong("mainSleepStart", now - twoWeeks);
            mainSleepEnd = sharedPref.getLong("mainSleepEnd", now - twoWeeks);
            napSleepStart = sharedPref.getLong("napSleepStart", now - twoWeeks);
            napSleepEnd = sharedPref.getLong("napSleepEnd", now - twoWeeks);

            ILastSleepUpdate = Instant.ofEpochMilli(lastSleepUpdate);

            db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "sleep_wake").allowMainThreadQueries().build();

            sleepDao = db.sleepDao();

            getSleepData();
            if (sleeps.size() > 0) {
                do_simulation();
                calculateAwareness();
                if(now-lastBackendUpdate >= (1000*60*60*24)) {
                    sendV0(username);
                    lastBackendUpdate = now;
                    editor.putLong("lastBackendUpdate", now);
                    editor.apply();
                }
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        db.close();
        Log.v(TAG, "onDestroy() is called");
    }

    public double getAwarenessValue(double H, double n, double y, double x){
        double coef_y = 0.8, coef_x = -0.16, v_vh = 1.01;
        double C = 3.37*0.5*(1+coef_y*y + coef_x * x);
        double D_up = (2.46+10.2+C)/v_vh;
        double awareness = D_up - H;
        return awareness;
    }

    public void getSleepData(){
        sleeps = Collections.synchronizedList(sleepDao.getAll());
        boolean check = false;
        long lastSleep1 = 0;
        for(Sleep sleep: sleeps){
            String sleepStart = sdfDateTime.format(new Date(sleep.sleepStart));
            String sleepEnd = sdfDateTime.format(new Date(sleep.sleepEnd));
            Date sleepEndD = new Date(sleep.sleepEnd);
            lastSleep1 = sleepEndD.getTime();
            Log.v("SLEEP REAL", sleepStart);
            Log.v("SLEEP REAL", sleepEnd);
            if(ILastSleepUpdate.isBefore(Instant.ofEpochMilli(sleep.sleepStart))){
                if(check == false){
                    lastDataUpdate = Long.min(lastDataUpdate, sleep.sleepStart - (1000*60*60*24));
                    editor.putLong("lastDataUpdate", lastDataUpdate);
                    editor.apply();
                }
                check = true;
            }
        }
        Log.v("CHECK", String.valueOf(check));
        Log.v("LAST SLEEP UPDATE", sdfDateTime.format(new Date(lastSleepUpdate)));
        Log.v("LAST SLEEP DATA", sdfDateTime.format(new Date(lastSleep1)));
        Log.v("SLEEP DATA", "GOT SLEEP DATA");
        if(check){
            //edit lastSleepUpdate to match current time
            editor.putLong("lastSleepUpdate", lastSleep1);
            editor.apply();
        }
    }

    public void do_simulation(){
        //get V0 data
        v0Dao = db.v0Dao();
        v0s = Collections.synchronizedList(v0Dao.getAll());

        //do pcr simulation
        long yesterday = now - (1000*60*60*24);
        Log.v("LAST DATA UPDATE", lastDataUpdate + " " + sdfDateTime.format(new Date(lastDataUpdate)));
        long startProcess = Long.min(yesterday, lastDataUpdate);
        if(sleeps.size() > 0) {
            lastDataUpdate = now - (1000 * 60 * 5);
            editor.putLong("lastDataUpdate", lastDataUpdate);
            editor.apply();
        }
        long endProcess = now;
        long processDuration = (endProcess - startProcess) / fiveMinutesToMil;
        boolean gotInitV0 = false;
        double[] initV0 = {-0.8990, -0.6153, 0.0961, 14.2460};
        //get init V0
        List<V0> deleteV0 = new ArrayList<>();
        for(V0 v0: v0s){
//            Log.v("V0", "H: "+ v0.H_val + ", n: " + v0.n_val + ", y: "+v0.y_val + ", x: " + v0.x_val);
            if(v0.time >= startProcess){
                if(!gotInitV0 && v0.time <= startProcess + (1000*60*6)){
                    initV0 = new double[]{v0.x_val, v0.y_val, v0.n_val, v0.H_val};
                    gotInitV0 = true;
                }
                deleteV0.add(v0);
            }
        }
        for(V0 v0: deleteV0){
            v0s.remove(v0);
        }
        v0Dao.deleteRange(startProcess, endProcess);

        //if we don't have the initV0, then something went wrong in the previous calculation
        //or it is the first time we get sleep data, recalculate everything from the first sleep
        if(!gotInitV0 && sleeps.size() > 0){
            Sleep firstSleep = sleeps.get(0);
            long firstSleepDayStart = (firstSleep.sleepStart + nineHours)/ (1000*60*60*24);
            long firstSleepNoon = (firstSleepDayStart*(1000*60*60*24)) + (1000*60*60*12);
            if(firstSleep.sleepStart > firstSleepNoon){
                startProcess = firstSleepNoon-nineHours;
                initV0=new double[]{0.8958, 0.5219, 0.5792, 12.4225};
            }else{
                startProcess = (firstSleepDayStart * (1000*60*60*24))-nineHours;
            }
        }
        Log.v("START PROCESS", sdfDateTime.format(new Date(startProcess)));
        Log.v("END PROCESS", sdfDateTime.format(new Date(endProcess)));

        Log.v("INIT V0", initV0[0] + " " + initV0[1] + " " + initV0[2] + " " + initV0[3]);
        Log.v("AWARENESS OF V0", String.valueOf(getAwarenessValue(initV0[3], initV0[2], initV0[1], initV0[0])));

        //get the sleep model & simulation result
        SleepModel sleepModel = new SleepModel();
        double[] sleepPattern = sleepToArray(startProcess, endProcess, sleeps);
        Log.v("SLEEP SIZE", String.valueOf(sleepPattern.length));
        for(int i = 0; i < sleepPattern.length; i ++){
            Log.v("SLEEP PATTERN SUPER: ", String.valueOf(i) + " " + String.valueOf(sleepPattern[i]));
        }
        double step = 1/12.0;
        ArrayList<double[]> simulationResult = sleepModel.pcr_simulation(initV0, sleepPattern, step);

        //update V0 from the simulation
        List<V0> newV0 = new ArrayList<>();
        Log.v("SIZE", String.valueOf(simulationResult.size()));
        float barIdx = 0f;
        float addBarIdx = 0.0833333f;
        for(int i = 0; i < simulationResult.size(); i ++){
            double[] res = simulationResult.get(i);
            V0 v0 = new V0();
            v0.x_val = res[0];
            v0.y_val = res[1];
            v0.n_val = res[2];
            v0.H_val = res[3];
            v0.time = startProcess + (i*fiveMinutesToMil);
            Log.v("VO TIME", i*5 + " " + getAwarenessValue(res[3], res[2], res[1], res[0]));
            newV0.add(v0);
            v0s.add(v0);

            if(v0.time >= (now-(1000*60*6)) && (v0.time <= now)){
                Log.v("UPDATED INIT V0", "UPDATED INIT V0");
                initV0 = res;
            }

            if(simulationResult.size() - 288 <= i){
                Log.v("BAR ENTRY", sdfDateTime.format(new Date(startProcess + (i*fiveMinutesToMil))));
                Log.v("WTF", "WTF");
                float value = (float) getAwarenessValue(res[3], res[2], res[1], res[0]);
                //Normalization
                barEntries.add(new BarEntry((float) barIdx, value*100f/3f));
                barIdx += addBarIdx;
            }
        }
        v0Dao.insertAll(newV0);
        Log.v("V0 DONE", "V0 DONE");
        Log.v("SLEEP ONSET", String.valueOf((int)(sleepOnset-now)/(1000*60*5)));
        Log.v("WORK ONSET", String.valueOf((int)(workOnset-now)/(1000*60*5)));
        Log.v("WORK OFFSET", String.valueOf((int)(workOffset-now)/(1000*60*5)));
        Log.v("INIT V0", initV0[0] + " " + initV0[1] + " " + initV0[2] + " " + initV0[3]);

        //process sleep prediction
        int[] sleepSuggestion = sleepModel.Sleep_pattern_suggestion(initV0, (int)(sleepOnset-now)/(1000*60*5), (int)(workOnset-now)/(1000*60*5), (int)(workOffset-now)/(1000*60*5), 5/60.0);
        Log.v("SLEEP SUGGESTION", String.valueOf(sleepSuggestion[0]));
        Log.v("MAIN SLEEP START", sdfDateTime.format(new Date(sleepSuggestion[0]*(1000*60*5)+now)));
        Log.v("MAIN SLEEP END", sdfDateTime.format(new Date(sleepSuggestion[1]*(1000*60*5)+now)));
        Log.v("NAP SLEEP START", sdfDateTime.format(new Date(sleepSuggestion[2]*(1000*60*5)+now)));
        Log.v("NAP SLEEP END", sdfDateTime.format(new Date(sleepSuggestion[3]*(1000*60*5)+now)));
        //update shared preferences
        mainSleepStart = sleepSuggestion[0]*(1000*60*5)+now;
        mainSleepEnd = sleepSuggestion[1]*(1000*60*5)+now;
        napSleepStart = sleepSuggestion[2]*(1000*60*5)+now;
        napSleepEnd = sleepSuggestion[3]*(1000*60*5)+now;
        int isearly = sleepSuggestion[4], isenough = sleepSuggestion[5];
        //Get isearlysleep and isenoughsleep
        isearlysleep = isearly != 0;
        isenoughsleep = isenough != 0;
        editor.putLong("mainSleepStart", mainSleepStart);
        editor.putLong("mainSleepEnd", mainSleepEnd);
        editor.putLong("napSleepStart", napSleepStart);
        editor.putLong("napSleepEnd", napSleepEnd);
        editor.putBoolean("isearlysleep", isearlysleep);
        editor.putBoolean("isenoughsleep", isenoughsleep);
        editor.apply();

        //get the new graph V0
        List<Sleep> newSleep = new ArrayList<>();
        Sleep newMainSleep = new Sleep();
        newMainSleep.sleepStart = mainSleepStart;
        newMainSleep.sleepEnd = mainSleepEnd;
        Sleep newNapSleep = new Sleep();
        newNapSleep.sleepStart = napSleepStart;
        newNapSleep.sleepEnd = napSleepEnd;
        newSleep.add(newMainSleep);
        newSleep.add(newNapSleep);

        sleepPattern = sleepToArray(now, now+1000*60*60*24, newSleep);
        for(int i = 0; i < sleepPattern.length; i ++){
            Log.v("SLEEP PATTERN: ", String.valueOf(i) + " " + String.valueOf(sleepPattern[i]));
        }
        Log.v("SLEEP SIZE", String.valueOf(sleepPattern.length));
        simulationResult = sleepModel.pcr_simulation(initV0, sleepPattern, 5/60.0);
        for(int i = Integer.max(0, sleepPattern.length-288); i < sleepPattern.length; i ++){
            double[] res = simulationResult.get(i);
            double awarenessVal = getAwarenessValue(res[3], res[2], res[1], res[0]);
            awarenessVal = Double.min(3.0, Double.max(-3.0, awarenessVal));
            float fAwarenessVal = (float) awarenessVal;
            barEntries.add(new BarEntry(barIdx, fAwarenessVal*100f/3.0f));
            barIdx += addBarIdx;
        }
    }

    public ArrayList<BarEntry> getBarEntries(){
        return barEntries;
    }

    public void calculateAwareness(){
        //calculate the awareness
        AwarenessDao awarenessDao = db.awarenessDao();
        long oneDayToMils = 1000*60*60*24;
        if(v0s.size() > 0){
            long startDay = (v0s.get(0).time+nineHours)/oneDayToMils;
            long goodDuration = 0;
            long badDuration = 0;
            for(V0 v0: v0s){
                boolean isSleep = false;
                for(Sleep sleep: sleeps){
                    if(sleep.sleepStart <= v0.time && v0.time <= sleep.sleepEnd){
                        isSleep = true;
                        break;
                    }
                }
                if(isSleep){
                    continue;
                }

                long v0StartDay = (v0.time+nineHours)/oneDayToMils;
                //check through the sleep in O(N) time. Fix it using hash map, but for now the complexity should be fine
                double awareness = getAwarenessValue(v0.H_val, v0.n_val, v0.y_val, v0.x_val);
                if(startDay != v0StartDay){
                    //if it is not in database, add, if yes, update
                    Awareness awarenessDb = awarenessDao.findByDay(startDay);
                    Awareness addAwareness = new Awareness();
                    addAwareness.awarenessDay = startDay;
                    addAwareness.goodDuration = goodDuration;
                    addAwareness.badDuration = badDuration;
                    boolean isInAwareness = false;
                    for(int i = 0; i < awarenesses.size(); i ++){
                        if(awarenesses.get(i).awarenessDay == addAwareness.awarenessDay){
                            awarenesses.set(i, addAwareness);
                            isInAwareness = true;
                            break;
                        }
                    }
                    if(isInAwareness == false){
                        awarenesses.add(addAwareness);
                    }
                    if(awarenessDb == null){
                        //insert
                        List<Awareness> awarenessList = new ArrayList<>();
                        awarenessList.add(addAwareness);
                        awarenessDao.insertAll(awarenessList);
                    }else {
                        //we can make it faster by using lazy loading, but this is okay for now
                        awarenessDao.updateAwareness(startDay, goodDuration, badDuration);
                    }
                    Log.v("AWARENESS", String.valueOf(startDay)+' '+ goodDuration + ' ' + badDuration);

                    goodDuration = 0;
                    badDuration = 0;
                    startDay = v0StartDay;
                }
                Log.v("AWARENESS CALCULATION", (sdfDateTime.format(new Date(v0.time)))+": " + String.valueOf(awareness));
                if(awareness >= 0.0){
                    goodDuration += 5;
                }else{
                    badDuration += 5;
                }
            }
            if(goodDuration > 0 || badDuration > 0){
                Awareness awarenessDb = awarenessDao.findByDay(startDay);
                Awareness addAwareness = new Awareness();
                addAwareness.awarenessDay = startDay;
                addAwareness.goodDuration = goodDuration;
                addAwareness.badDuration = badDuration;
                boolean isInAwareness = false;
                for(int i = 0; i < awarenesses.size(); i ++){
                    if(awarenesses.get(i).awarenessDay == addAwareness.awarenessDay){
                        awarenesses.set(i, addAwareness);
                        isInAwareness = true;
                        break;
                    }
                }
                if(isInAwareness == false){
                    awarenesses.add(addAwareness);
                }
                if(awarenessDb == null){
                    //insert
                    List<Awareness> awarenessList = new ArrayList<>();
                    awarenessList.add(addAwareness);
                    awarenessDao.insertAll(awarenessList);
                }else {
                    //we can make it faster by using lazy loading, but this is okay for now
                    awarenessDao.updateAwareness(startDay, goodDuration, badDuration);
                }
            }
        }
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    //convert sleep from long value to integers array value
    public static double[] sleepToArray(Long sleepStart, Long sleepEnd, List<Sleep> sleeps){
        //for every 1000*60*5 we add a value to the array list
        long fiveMinutesToMil = 1000*60*5;
        int duration = (int)((sleepEnd - sleepStart)/fiveMinutesToMil);
        double[] sleepPattern = new double[duration + 5];
        Arrays.fill(sleepPattern, 0);
        for(Sleep sleep: sleeps){
            long tempSleepStart = Long.max( sleepStart/fiveMinutesToMil, sleep.sleepStart / fiveMinutesToMil);
            long tempSleepEnd = Long.min(sleepEnd/fiveMinutesToMil, sleep.sleepEnd / fiveMinutesToMil);
            if(sleepStart/fiveMinutesToMil <= tempSleepStart && tempSleepEnd <= sleepEnd/fiveMinutesToMil && tempSleepStart <= tempSleepEnd) {
                Log.v("temp sleep start", String.valueOf(tempSleepStart));
                Log.v("temp sleep end", String.valueOf(tempSleepEnd));
                Log.v("sleep start", String.valueOf(sleepStart));
                Log.v("sleep end", String.valueOf(sleepEnd));
                int idx = (int) (tempSleepStart - (sleepStart / fiveMinutesToMil));
                int offset = (int) (tempSleepEnd - (sleepStart / fiveMinutesToMil));
                Log.v("index", String.valueOf(idx));
                Log.v("offset", String.valueOf(offset));
                for (int i = idx; i <= offset; i++) {
                    sleepPattern[i] = 1.0;
                }
            }
        }
        return sleepPattern;
    }

    public boolean isOverlap(List<Sleep> sleeps, Sleep sleepX, int sleepEx){
        for(Sleep sleep: sleeps){
            if(sleepEx == sleep.sleep_id){
                continue;
            }
            if(!(sleepX.sleepEnd <= sleep.sleepStart || sleepX.sleepStart >= sleep.sleepEnd)){
                return true;
            }
        }
        return false;
    }

    protected void sendV0(String username) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sleep-math.com/sleepapp/")
                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())
                // at last we are building our retrofit builder.
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        List<Sleep> tempSleep = new ArrayList<>();
        List<V0> tempV0 = new ArrayList<>();
        for(Sleep sleep: sleeps){
            if(sleep.sleepStart <= now){
                tempSleep.add(sleep);
            }
        }
        for(V0 v0: v0s){
            if(v0.time <= now){
                tempV0.add(v0);
            }
        }

        DataModal modal = new DataModal(username, tempSleep, tempV0);
        Call<DataModal> call = retrofitAPI.createPost(modal);
        call.enqueue(new Callback<DataModal>() {
            @Override
            public void onResponse(Call<DataModal> call, Response<DataModal> response) {
                // this method is called when we get response from our api.
                Toast.makeText(MainActivity.this, "Data added to API", Toast.LENGTH_SHORT).show();

                // we are getting response from our body
                // and passing it to our modal class.
                DataModal responseFromAPI = response.body();

                // on below line we are getting our data from modal class and adding it to our string.
                String responseString = "Response Code : " + response.code() + "\nName : "  + "\n" ;
                Log.v("RESPONSE2", responseString);
            }

            @Override
            public void onFailure(Call<DataModal> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
               Log.v("ERROR", "Error found is : " + t.getMessage());
            }
        });

    }

//    public void sendV0(V0 v0){
//        JSONObject postData = new JSONObject();
//        try {
//            postData.put("name", name.getText().toString());
//            postData.put("address", address.getText().toString());
//            postData.put("manufacturer", manufacturer.getText().toString());
//            postData.put("location", location.getText().toString());
//            postData.put("type", type.getText().toString());
//            postData.put("deviceID", deviceID.getText().toString());
//
//            new SendDeviceDetails().execute("http://52.88.194.67:8080/IOTProjectServer/registerDevice", postData.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    public List<Awareness> getAwarenesses(){
        return awarenesses;
    }

    public List<Sleep> getSleeps(){
        return sleeps;
    }

    public void addSleep(Sleep sleep){
        //check if the sleep is already there
        if(sleep.sleepStart < this.lastSleepUpdate) {
            List<Sleep> listSleep = new ArrayList<>();
            long sleepDayStart = (sleep.sleepStart + nineHours)/(1000*60*60*24);
            long sleepDayEnd = (sleep.sleepEnd + nineHours)/(1000*60*60*24);
            if(sleepDayStart != sleepDayEnd){
                long midnight = sleepDayEnd * 1000*60*60*24;
                midnight = midnight - nineHours;
                Sleep sleep2 = new Sleep();
                sleep2.sleepStart = sleep.sleepStart;
                sleep2.sleepEnd = midnight;
                sleep.sleepStart = midnight;
                listSleep.add(sleep2);
                listSleep.add(sleep);
            }else {
                listSleep.add(sleep);
            }
            Log.v("SLEEP DATA ADDED", String.valueOf(sleep.sleepStart));
            this.sleepDao.insertAll(listSleep);
        }
        lastDataUpdate = sleep.sleepStart - (1000*60*60*24);
        editor.putLong("lastDataUpdate", lastDataUpdate);
        editor.apply();
        healthConnectManager.javWriteSleepInput(sleep.sleepStart, sleep.sleepEnd);

    }

    public boolean editSleep(Sleep prevSleep, Sleep updatedSleep){
        int count = 0;
        for(Sleep sleep: this.sleeps){
            if(sleep.sleepStart/60000 == prevSleep.sleepStart/60000 && sleep.sleepEnd/60000 == prevSleep.sleepEnd/60000){
                int sleepId = sleep.sleep_id;
                if(!isOverlap(this.sleeps, updatedSleep, sleepId)) {
                    lastDataUpdate = updatedSleep.sleepStart - (1000*60*60*24);
                    editor.putLong("lastDataUpdate", lastDataUpdate);
                    editor.apply();

                    updatedSleep.sleep_id = sleepId;
                    sleepDao.updateSleep(sleepId, updatedSleep.sleepStart, updatedSleep.sleepEnd);
                    this.sleeps.set(count, updatedSleep);
                    return true;
                }else{
                    return false;
                }
            }
            count += 1;
        }
        return false;
    }

    public boolean deleteSleep(Sleep sleepDel){
        long sleepDelStart = sleepDel.sleepStart/60000;
        long sleepDelEnd = sleepDel.sleepEnd/60000;
        Log.v("SLEEP DELETE START", String.valueOf(sleepDel.sleepStart));
        Log.v("SLEEP DELETE END", String.valueOf(sleepDel.sleepEnd));

        for(Sleep sleep: this.sleeps){
            long sSleepStart = sleep.sleepStart/60000;
            long sSleepEnd = sleep.sleepEnd/60000;
            Log.v("SLEEP DELETE START", String.valueOf(sleep.sleepStart));
            Log.v("SLEEP DELETE END", String.valueOf(sleep.sleepEnd));
            if(sSleepStart == sleepDelStart && sSleepEnd == sleepDelEnd){
                Log.v("deleted broooo", "broooo");
                lastDataUpdate = sleep.sleepStart - (1000*60*60*24);
                editor.putLong("lastDataUpdate", lastDataUpdate);
                editor.apply();
                sleepDao.delete(sleep);
                this.sleeps.remove(sleep);
                return true;
            }
        }
        return false;
    }

    public long getMainSleepStart() {
        return mainSleepStart;
    }

    public void setMainSleepStart(long mainSleepStart) {
        this.mainSleepStart = mainSleepStart;
        editor.putLong("mainSleepStart", mainSleepStart);
        editor.apply();
    }

    public long getMainSleepEnd() {
        return mainSleepEnd;
    }

    public void setMainSleepEnd(long mainSleepEnd) {
        this.mainSleepEnd = mainSleepEnd;
        editor.putLong("mainSleepEnd", mainSleepEnd);
        editor.apply();
    }

    public long getNapSleepStart() {
        Log.v("function nap sleep start", String.valueOf(napSleepStart));
        return napSleepStart;
    }

    public void setNapSleepStart(long napSleepStart) {
        this.napSleepStart = napSleepStart;
        editor.putLong("napSleepStart", napSleepStart);
        editor.apply();
    }

    public long getNapSleepEnd() {
        return napSleepEnd;
    }

    public void setNapSleepEnd(long napSleepEnd) {
        this.napSleepEnd = napSleepEnd;
        editor.putLong("napSleepEnd", napSleepEnd);
        editor.apply();
    }

    public long getSleepOnset() {
        return sleepOnset;
    }

    public void setSleepOnset(long sleepOnset) {
        this.sleepOnset = sleepOnset;
        editor.putLong("sleepOnset", sleepOnset);
        editor.apply();
        Log.v("sleep onset", sdfDateTime.format(new Date(sharedPref.getLong("sleepOnset", now))));
    }

    public long getWorkOnset(){
        return workOnset;
    }

    public void setWorkOnset(long workOnset){
        this.workOnset = workOnset;
        editor.putLong("workOnset", workOnset);
        editor.apply();
        Log.v("work onset", sdfDateTime.format(new Date(sharedPref.getLong("workOnset", now))));
    }

    public long getWorkOffset() {
        return workOffset;
    }

    public void setWorkOffset(long workOffset) {
        this.workOffset = workOffset;
        editor.putLong("workOffset", workOffset);
        editor.apply();
        Log.v("work offset", sdfDateTime.format(new Date(sharedPref.getLong("workOffset", now))));
    }

    public boolean getIsEarlySleep() {
        return this.isearlysleep;
    }

    public boolean getIsEnoughSleep() {
        return this.isenoughsleep;
    }

    public long getLastSleepUpdate() {
        return lastSleepUpdate;
    }

    public void setLastSleepUpdate(long lastSleepUpdate) {
        this.lastSleepUpdate = lastSleepUpdate;
        editor.putLong("lastSleepUpdate", lastSleepUpdate);
        editor.apply();
    }

    public void setGoneBottomNavi() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }

    public void setVisibleBottomNavi() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    public void setBottomNaviItem(int selected_id) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(selected_id);
        }
    }

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