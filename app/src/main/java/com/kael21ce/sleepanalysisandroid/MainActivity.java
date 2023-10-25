package com.kael21ce.sleepanalysisandroid;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.runtime.MutableState;
import androidx.room.Room;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.Awareness;
import com.kael21ce.sleepanalysisandroid.data.AwarenessDao;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import kotlinx.coroutines.Dispatchers;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();
    RecommendFragment recommendFragment = new RecommendFragment();
    SettingFragment settingFragment = new SettingFragment();
    private RelativeLayout loadingScreenLayout;
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy"+ "HH:mm", Locale.KOREA);
    HealthConnectManager healthConnectManager;
    //health connect
    private static Context context;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    //for fragment too
    private long mainSleepStart, mainSleepEnd, napSleepStart, napSleepEnd;
    private long sleepOnset, workOnset, workOffset;
    private long lastSleepUpdate, lastDataUpdate;

    AppDatabase db;
    private List<Sleep> sleeps;
    private List<Awareness> awarenesses;
    private List<V0> v0s;
    SleepDao sleepDao;
    V0Dao v0Dao;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);

        healthConnectManager = new HealthConnectManager(getApplicationContext());

        //get the shared preferences variable
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        long twoWeeks = (1000*60*60*24*14);
        long fiveMinutesToMil = (1000*60*50);

        //update variables
        lastSleepUpdate = sharedPref.getLong("lastSleepUpdate", System.currentTimeMillis() - twoWeeks);
        lastDataUpdate = sharedPref.getLong("lastUpdate", System.currentTimeMillis() - twoWeeks);

        //user sleep variables
        sleepOnset = sharedPref.getLong("sleepOnset", System.currentTimeMillis());
        workOnset = sharedPref.getLong("workOnset", System.currentTimeMillis());
        workOffset = sharedPref.getLong("workOffset", System.currentTimeMillis());

        //sleep result variables
        mainSleepStart = sharedPref.getLong("mainSleepStart", System.currentTimeMillis() - twoWeeks);
        mainSleepEnd = sharedPref.getLong("mainSleepEnd", System.currentTimeMillis() - twoWeeks);
        napSleepStart = sharedPref.getLong("napSleepStart", System.currentTimeMillis() - twoWeeks);
        napSleepEnd = sharedPref.getLong("napSleepEnd", System.currentTimeMillis() - twoWeeks);

        Instant now = Instant.now();
        Instant ILastSleepUpdate = Instant.ofEpochMilli(lastSleepUpdate);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "sleep_wake").allowMainThreadQueries().build();

        sleepDao = db.sleepDao();

        //get sleep data
        sleeps = sleepDao.getAll();
        boolean check = false;
        for(Sleep sleep: sleeps){
            String sleepStart = sdfDateTime.format(new Date(sleep.sleepStart));
            String sleepEnd = sdfDateTime.format(new Date(sleep.sleepEnd));
            Log.v("SLEEP REAL", sleepStart);
            Log.v("SLEEP REAL", sleepEnd);
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
        v0Dao = db.v0Dao();
        v0s = v0Dao.getAll();

        //do pcr simulation
        long yesterday = System.currentTimeMillis() - (1000*60*60*24);
        long startProcess = Long.min(yesterday, lastSleepUpdate);
        long endProcess = System.currentTimeMillis();
        long processDuration = (endProcess - startProcess) / fiveMinutesToMil;
        boolean gotInitV0 = false;
        double[] initV0 = {-0.8590, -0.6837, 0.1140, 14.2133};
        //get init V0
        for(V0 v0: v0s){
            Log.v("V0", "H: "+ v0.H_val + ", n: " + v0.n_val + ", y: "+v0.y_val + ", x: " + v0.x_val);
            if(v0.time >= startProcess){
                if(!gotInitV0 && v0.time <= startProcess + (1000*60*6)){
                    initV0 = new double[]{v0.y_val, v0.x_val, v0.n_val, v0.H_val};
                    gotInitV0 = true;
                }
            }
        }
        v0Dao.deleteRange(startProcess, endProcess);

        //if we don't have the initV0, then something went wrong in the previous calculation
        //or it is the first time we get sleep data, recalculate everything from the first sleep
        if(!gotInitV0 && sleeps.size() > 0){
            Sleep firstSleep = sleeps.get(0);
            long firstSleepDayStart = firstSleep.sleepStart / (1000*60*60*24);
            long firstSleepNoon = firstSleepDayStart + (1000*60*60*12);
            if(firstSleep.sleepStart > firstSleepNoon){
                startProcess = firstSleepNoon;
                initV0=new double[]{0.8958, 0.5219, 0.5792, 12.4225};
            }else{
                startProcess = firstSleepDayStart;
            }
        }

        //get the sleep model & simulation result
        SleepModel sleepModel = new SleepModel();
        double[] sleepPattern = sleepToArray(startProcess, endProcess, sleeps);
        ArrayList<double[]> simulationResult = sleepModel.pcr_simulation(initV0, sleepPattern, 5/60.0);

        //update V0 from the simulation
        List<V0> newV0 = new ArrayList<>();
        Log.v("SIZE", String.valueOf(simulationResult.size()));
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
        int[] sleepSuggestion = sleepModel.Sleep_pattern_suggestion(initV0, (int)sleepOnset/1000, (int)workOnset/1000, (int)workOffset/1000, 5/60.0);

        //update shared preferences
        editor.putLong("mainSleepStart", sleepSuggestion[0]);
        editor.putLong("mainSleepEnd", sleepSuggestion[1]);
        editor.putLong("napSleepStart", sleepSuggestion[2]);
        editor.putLong("napSleepEnd", sleepSuggestion[3]);
        editor.apply();

        //calculate the awareness
        AwarenessDao awarenessDao = db.awarenessDao();
        long oneDayToMils = 1000*60*60*24;
        if(v0s.size() > 0){
            long startDay = v0s.get(0).time/oneDayToMils;
            long goodDuration = 0;
            long badDuration = 0;
            for(V0 v0: v0s){
                long v0StartDay = v0.time/oneDayToMils;
                double awareness = getAwarenessValue(v0.H_val, v0.n_val, v0.y_val, v0.x_val);
                if(startDay != v0StartDay){
                    awarenessDao.updateAwareness(startDay, goodDuration, badDuration);

                    goodDuration = 0;
                    badDuration = 0;
                    startDay = v0StartDay;
                }
                if(awareness > 0){
                    goodDuration += 5;
                }else{
                    badDuration += 5;
                }
            }
        }
        awarenesses = awarenessDao.getAll();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, homeFragment).commit();
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
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        db.close();
    }

    public double getAwarenessValue(double H, double n, double y, double x){
        double coef_y = 0.8, coef_x = -0.16, v_vh = 1.01;
        double C = 3.37*0.5*(1+coef_y*x + coef_x * y);
        double D_up = (2.46+10.2+C)/v_vh;
        double awareness = D_up - H;
        return awareness;
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
            if(sleepStart/fiveMinutesToMil < tempSleepStart) {
                int idx = (int) (tempSleepStart - sleepStart / fiveMinutesToMil);
                int offset = (int) (tempSleepEnd - sleepStart / fiveMinutesToMil);
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
            if(!(sleepX.sleepEnd < sleep.sleepStart || sleepX.sleepStart > sleep.sleepEnd)){
                return true;
            }
        }
        return false;
    }

    public List<Awareness> getAwarenesses(){
        return awarenesses;
    }

    public List<Sleep> getSleeps(){
        return sleeps;
    }

    public boolean addSleep(Sleep sleep){
        //check if the sleep is already there
        boolean isOverlap = isOverlap(sleeps, sleep, -1);
        if(!isOverlap) {
            this.sleeps.add(sleep);
            List<Sleep> listSleep = new ArrayList<>();
            listSleep.add(sleep);
            this.sleepDao.insertAll(listSleep);
            healthConnectManager.javWriteSleepInput(sleep.sleepStart, sleep.sleepEnd);
            return true;
        }else{
            return false;
        }

    }

    public boolean editSleep(Sleep prevSleep, Sleep updatedSleep){

        for(Sleep sleep: this.sleeps){
            if(sleep.sleepStart == prevSleep.sleepStart && sleep.sleepEnd == prevSleep.sleepEnd){
                int sleepId = sleep.sleep_id;
                if(!isOverlap(this.sleeps, updatedSleep, sleepId)) {
                    updatedSleep.sleep_id = sleepId;
                    this.sleeps.set(sleepId, updatedSleep);
                    sleepDao.updateSleep(sleepId, updatedSleep.sleepStart, updatedSleep.sleepEnd);
                    return true;
                }else{
                    return false;
                }
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

    public void setSleepOnset(long napSleepEnd) {
        this.sleepOnset = sleepOnset;
        editor.putLong("sleepOnset", sleepOnset);
        editor.apply();
    }

    public long getWorkOnset(){
        return workOnset;
    }

    public void setWorkOnset(long workOnset){
        this.workOnset = workOnset;
        editor.putLong("workOnset", workOnset);
        editor.apply();
    }

    public long getWorkOffset() {
        return workOffset;
    }

    public void setWorkOffset(long workOffset) {
        this.workOffset = workOffset;
        editor.putLong("workOffset", workOffset);
        editor.apply();
    }

    public long getLastSleepUpdate() {
        return lastSleepUpdate;
    }

    public void setLastSleepUpdate(long lastSleepUpdate) {
        this.lastSleepUpdate = lastSleepUpdate;
        editor.putLong("lastSleepUpdate", lastSleepUpdate);
        editor.apply();
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