package com.kael21ce.sleepanalysisandroid.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.room.Room;

import com.github.mikephil.charting.data.BarEntry;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * MainActivity / Onboarding 공용 파이프라인
 * -> Sleep data 호출 및 정제, Alertness 계산
 */
public class ProcessingAPI {
    static long now;
    static long nineHours;
    static long twoWeeks = (1000*60*60*24*14);
    static long oneDay = 1000*60*60*24;
    static long oneHour = 1000*60*60;
    static long fiveMinutesToMil = (1000*60*5);
    static SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy" + " HH:mm", Locale.getDefault());

    public static CombineResult run(Context context, SharedPreferences sharedPref) {
        //1) 기본 세팅
        SharedPreferences.Editor editor = sharedPref.edit();
        String email = sharedPref.getString("User_Email", "tester33");
        now = System.currentTimeMillis();
        long lastSleepUpdate = sharedPref.getLong("lastSleepUpdate", now - twoWeeks);
        long lastDataUpdate = sharedPref.getLong("lastDataUpdate", now - twoWeeks);
        long lastBackendUpdate = sharedPref.getLong("lastBackendUpdate", now - twoWeeks);
        List<V0> v0s = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        Instant ILastSleepUpdate = Instant.ofEpochMilli(lastSleepUpdate);
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime zoneNow = ZonedDateTime.now(zone);
        ZoneOffset offset = zoneNow.getOffset();
        nineHours = (1000L * offset.getTotalSeconds()); // nineHours는 현재 timeZone과 UTC와의 차이를 나타낸다.


        // 2) HealthConnect 읽기
        HealthConnectManager healthConnectManager = new HealthConnectManager(context);
        List<Awareness> awarenesses = Collections.synchronizedList(new ArrayList<>());
        List<Awareness> sleepAwarenesses = Collections.synchronizedList(new ArrayList<>());

        AppDatabase db = Room.databaseBuilder(context,
                AppDatabase.class, "sleep_wake").allowMainThreadQueries().build();
        SleepDao sleepDao = db.sleepDao();

        // 3) 수면 날짜 업데이트
        long sleepOnset, workOnset, workOffset, sleepOnsetShow;
        sleepOnset = sharedPref.getLong("sleepOnset", now);
        workOnset = sharedPref.getLong("workOnset", now);
        workOffset = sharedPref.getLong("workOffset", now);
        sleepOnsetShow = sharedPref.getLong("sleepOnsetShow", now);
        Long[] updatedDates = updateOnsetDate(now, sleepOnset, sleepOnsetShow, workOnset, workOffset);
        editor.putLong("sleepOnset", updatedDates[0]);
        editor.putLong("sleepOnsetShow", updatedDates[1]);
        editor.putLong("workOnset", updatedDates[2]);
        editor.putLong("workOffset", updatedDates[3]);
        editor.apply();
        ArrayList<Long> onsetOffsets = new ArrayList<>(Arrays.asList(updatedDates).subList(0, 4));

        List<Sleep> sleeps = getSleepData(sharedPref, sleepDao, lastDataUpdate, lastSleepUpdate, ILastSleepUpdate);

        if (!sleeps.isEmpty()) {
            CombineResult combineResult = do_simulation(db, sharedPref, sleeps, barEntries, onsetOffsets, lastDataUpdate);
            v0s = combineResult.getV0s();
            barEntries = combineResult.getBarEntries();
            awarenesses = calculateAwareness(db, sleeps, v0s);
            sleepAwarenesses = calculateSleepAwareness(db, sleeps, v0s);
            if(now-lastBackendUpdate >= (1000*60*60*12)) {
                sendV0(context, email, sleeps, v0s);
                editor.putLong("lastBackendUpdate", now);
                editor.apply();
            }
        }

        return new CombineResult(sleeps, v0s, barEntries, awarenesses, sleepAwarenesses);
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
        if (sleepOnset <= currentTime && currentTime <= workOnset) {
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

    public static List<Sleep> getSleepData(SharedPreferences sharedPref, SleepDao sleepDao, long lastDataUpdate,
                                    long lastSleepUpdate, Instant ILastSleepUpdate){
        SharedPreferences.Editor editor = sharedPref.edit();
        List<Sleep> sleeps = Collections.synchronizedList(sleepDao.getAll());
        boolean check = false;
        long lastSleep1 = 0;
        long befSleepStart = 0;
        long befSleepEnd = 0;
        ArrayList<Sleep> deleteTheSleeps = new ArrayList<>();
        for(Sleep sleep: new ArrayList<Sleep>(sleeps)){
            //synchronize the sleep
            if(befSleepStart == sleep.sleepStart && befSleepEnd == sleep.sleepEnd) {
                sleepDao.delete(sleep);
                sleeps.remove(sleep);
                Log.v("same data", "same data");
                befSleepStart = sleep.sleepStart;
                befSleepEnd = sleep.sleepEnd;
                continue;
            }
            befSleepStart = sleep.sleepStart;
            befSleepEnd = sleep.sleepEnd;
            String sleepStart = sdfDateTime.format(new Date(sleep.sleepStart));
            String sleepEnd = sdfDateTime.format(new Date(sleep.sleepEnd));
            Date sleepEndD = new Date(sleep.sleepEnd);
            lastSleep1 = sleepEndD.getTime();
            Log.v("SLEEP REAL", sleepStart);
            Log.v("SLEEP REAL", sleepEnd);
            if(ILastSleepUpdate.isBefore(Instant.ofEpochMilli(sleep.sleepStart))){
                if(!check){
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

        return sleeps;
    }

    public static CombineResult do_simulation(AppDatabase db, SharedPreferences sharedPref, List<Sleep> sleeps,
                                     ArrayList<BarEntry> barEntries, ArrayList<Long> onsetOffsets,
                                     long lastDataUpdate){
        SharedPreferences.Editor editor = sharedPref.edit();
        //get V0 data
        V0Dao v0Dao = db.v0Dao();
        List<V0> v0s = Collections.synchronizedList(v0Dao.getAll());

        Boolean deleteException = sharedPref.getBoolean("deleteException", false);
        if (!deleteException) {
            Log.v("MainActivity", "Conventional");
            if (lastDataUpdate < now - twoWeeks) {
                lastDataUpdate = now - twoWeeks;
            }
        } else {
            Log.v("MainActivity", "Exceptional");
            editor.putBoolean("deleteException", false).apply();
        }

        //do pcr simulation
        long yesterday = now - (1000*60*60*24);
        Log.v("LAST DATA UPDATE", lastDataUpdate + " " + sdfDateTime.format(new Date(lastDataUpdate)));
        long startProcess = Long.min(yesterday, lastDataUpdate);
        if(!sleeps.isEmpty()) {
            lastDataUpdate = now - (1000 * 60 * 5);
            editor.putLong("lastDataUpdate", lastDataUpdate);
            editor.apply();
        }
        long endProcess = now;
        long processDuration = (endProcess - startProcess) / fiveMinutesToMil;
        boolean gotInitV0 = false;
        double[] initV0 = {-0.8990, -0.6153, 0.0961, 14.2460};

        //Clean entries
        List<V0> deleteV0 = new ArrayList<>();

        //get init V0
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
        if(!gotInitV0 && !sleeps.isEmpty()){
            Sleep firstSleep = sleeps.get(0);
            long firstSleepDayStart = (firstSleep.sleepStart + nineHours)/ (1000*60*60*24);
            long firstSleepNoon = (firstSleepDayStart*(1000*60*60*24)) + (1000*60*60*12);
            Log.v("FIRST SLEEP DAY START", String.valueOf(firstSleepNoon));
            Log.v("FIRST SLEEP", String.valueOf(firstSleep.sleepStart));
            if(firstSleep.sleepStart+nineHours >= firstSleepNoon){
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
            Log.v("SLEEP PATTERN SUPER: ", i + " " + sleepPattern[i]);
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
                barEntries.add(new BarEntry(barIdx, value*100f/3f));
                barIdx += addBarIdx;
                Log.v("Each bar", "x: " + barIdx + " / y: " + value*100f/3.0f);
            }
        }
        v0Dao.insertAll(newV0);
        long sleepOnset, sleepOnsetShow, workOnset, workOffset;
        sleepOnset = onsetOffsets.get(0);
        sleepOnsetShow = onsetOffsets.get(1);
        workOnset = onsetOffsets.get(2);
        workOffset = onsetOffsets.get(3);
        Log.v("V0 DONE", "V0 DONE");
        Log.v("SLEEP ONSET", String.valueOf((int)(sleepOnset-now)/(1000*60*5)));
        Log.v("SLEEP OFFSET SHOW", String.valueOf((int)(sleepOnsetShow-now)/(1000*60*5)));
        Log.v("WORK ONSET", String.valueOf((int)(workOnset-now)/(1000*60*5)));
        Log.v("WORK OFFSET", String.valueOf((int)(workOffset-now)/(1000*60*5)));
        Log.v("INIT V0", initV0[0] + " " + initV0[1] + " " + initV0[2] + " " + initV0[3]);

        //process sleep prediction
        boolean isearlysleep, isenoughsleep;
        boolean isNight = sleepOnset == sleepOnsetShow;
        int[] sleepSuggestion = sleepModel.Sleep_pattern_suggestion(initV0, (int)(sleepOnset-now)/(1000*60*5),
                (int)(workOnset-now)/(1000*60*5), (int)(workOffset-now)/(1000*60*5), 5/60.0, isNight);
        Log.v("SLEEP SUGGESTION", "is night? : " + isNight);
        Log.v("SLEEP SUGGESTION", String.valueOf(sleepSuggestion[0]));
        Log.v("MAIN SLEEP START", sdfDateTime.format(new Date(sleepSuggestion[0]*(1000*60*5)+now)));
        Log.v("MAIN SLEEP END", sdfDateTime.format(new Date(sleepSuggestion[1]*(1000*60*5)+now)));
        Log.v("NAP SLEEP START", sdfDateTime.format(new Date(sleepSuggestion[2]*(1000*60*5)+now)));
        Log.v("NAP SLEEP END", sdfDateTime.format(new Date(sleepSuggestion[3]*(1000*60*5)+now)));

        //update shared preferences
        long mainSleepStart, mainSleepEnd, napSleepStart, napSleepEnd;
        mainSleepStart = sleepSuggestion[0]*(1000*60*5)+now;
        mainSleepEnd = sleepSuggestion[1]*(1000*60*5)+now;
        napSleepStart = sleepSuggestion[2]*(1000*60*5)+now;
        napSleepEnd = sleepSuggestion[3]*(1000*60*5)+now;
        int isenough = sleepSuggestion[4], isearly = sleepSuggestion[5];
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
            Log.v("SLEEP PATTERN: ", i + " " + sleepPattern[i]);
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
            Log.v("Each bar", "x: " + barIdx + " / y: " + fAwarenessVal*100f/3.0f);
        }

        Log.v("BAR ENTRIES SIZE", String.valueOf(barEntries.size()));

        if(barEntries.size() < 576){
            int need = 576 - barEntries.size();
            float thePlus = need * addBarIdx;
            for (int i = 0; i < barEntries.size(); i++) {
                barEntries.set(i, new BarEntry(barEntries.get(i).getX() + thePlus, barEntries.get(i).getY()));
            }
        }

        return new CombineResult(null, v0s, barEntries, null, null);
    }

    public static List<Awareness> calculateAwareness(AppDatabase db, List<Sleep> sleeps, List<V0> v0s){
        //calculate the awareness
        AwarenessDao awarenessDao = db.awarenessDao();
        List<Awareness> awarenesses = Collections.synchronizedList(new ArrayList<>());
        long oneDayToMils = 1000*60*60*24;
        if(!v0s.isEmpty()){
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
                    if(!isInAwareness){
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
                Log.v("AWARENESS CALCULATION", (sdfDateTime.format(new Date(v0.time)))+": " + awareness);
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
                if(!isInAwareness){
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

        return awarenesses;
    }

    public static List<Awareness> calculateSleepAwareness(AppDatabase db, List<Sleep> sleeps, List<V0> v0s){
        //calculate the awareness in context of sleep
        AwarenessDao awarenessDao = db.awarenessDao();
        List<Awareness> sleepAwarenesses = Collections.synchronizedList(new ArrayList<>());
        long oneDayToMils = 1000*60*60*24;
        if(!v0s.isEmpty()){
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
                if(!isSleep){
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
                    for(int i = 0; i < sleepAwarenesses.size(); i ++){
                        if(sleepAwarenesses.get(i).awarenessDay == addAwareness.awarenessDay){
                            sleepAwarenesses.set(i, addAwareness);
                            isInAwareness = true;
                            break;
                        }
                    }
                    if(!isInAwareness){
                        sleepAwarenesses.add(addAwareness);
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
                Log.v("AWARENESS CALCULATION", (sdfDateTime.format(new Date(v0.time)))+": " + awareness);
                if(awareness <= 0.0){
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
                for(int i = 0; i < sleepAwarenesses.size(); i ++){
                    if(sleepAwarenesses.get(i).awarenessDay == addAwareness.awarenessDay){
                        sleepAwarenesses.set(i, addAwareness);
                        isInAwareness = true;
                        break;
                    }
                }
                if(!isInAwareness){
                    sleepAwarenesses.add(addAwareness);
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

        return sleepAwarenesses;
    }

    protected static void sendV0(Context context, String userEmail, List<Sleep> sleeps, List<V0> v0s) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.sleep-math.com/sleepapp/")
                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                // at last we are building our retrofit builder.
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        List<Sleep> tempSleep = new ArrayList<>();
        List<V0> tempV0 = new ArrayList<>();
        for(Sleep sleep: sleeps){
            if(sleep.sleepStart >= (1000*60*60*24*14) && sleep.sleepStart <= now){
                tempSleep.add(sleep);
            }
        }
        if (v0s != null) {
            for(V0 v0: v0s){
                if(v0.time >= (1000*60*60*24*14) && v0.time <= now){
                    tempV0.add(v0);
                }
            }
        } else {
            Toast.makeText(context, "전송할 수면 데이터가 존재하지 않습니다",
                    Toast.LENGTH_SHORT).show();
        }

        //DataModal modal = new DataModal(username, tempSleep, tempV0);
        DataModal modal = new DataModal(userEmail, tempSleep);
        Call<DataModal> call = retrofitAPI.createPost(modal);
        call.enqueue(new Callback<DataModal>() {
            @Override
            public void onResponse(Call<DataModal> call, Response<DataModal> response) {
                // this method is called when we get response from our api.
                Locale currentLocale = Locale.getDefault();
                String language = currentLocale.getLanguage();
                Log.v("MainActivity", "Response code: " + response.code());
                if(response.code() <= 300) {
                    if (language.equals("ko")) {
                        Toast.makeText(context, "데이터가 전송되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Data added to API", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (language.equals("ko")) {
                        Toast.makeText(context, "데이터 전송에 실패했습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Data sending failed", Toast.LENGTH_SHORT).show();
                    }
                    // we are getting response from our body
                    // and passing it to our modal class.
                    DataModal responseFromAPI = response.body();

                    // on below line we are getting our data from modal class and adding it to our string.
                    String responseString = "Response Code : " + response.code() + "\nName : " + "\n";
                    Log.v("RESPONSE for sending data", responseString);
                }
            }

            @Override
            public void onFailure(Call<DataModal> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
                Log.v("ERROR", "Error found is : " + t.getMessage());
            }
        });

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

    public static double getAwarenessValue(double H, double n, double y, double x){
        double coef_y = 0.8, coef_x = -0.16, v_vh = 1.01;
        double C = 3.37*0.5*(1+coef_y*y + coef_x * x);
        double D_up = (2.46+10.2+C)/v_vh;
        double awareness = D_up - H;
        return awareness;
    }
}
