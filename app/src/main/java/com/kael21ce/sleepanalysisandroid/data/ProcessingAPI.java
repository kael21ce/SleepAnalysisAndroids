package com.kael21ce.sleepanalysisandroid.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.data.BarEntry;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * MainActivity / Onboarding 공용 파이프라인
 * -> Sleep data 호출 및 정제, Alertness 계산
 */
public class ProcessingAPI {
    static AppDatabase db;
    static final String TAG = "ProcessingAPI";
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

        db = AppDatabaseSingleton.getInstance(context);
        SleepDao sleepDao = db.sleepDao();
        V0Dao v0Dao = db.v0Dao();

        // 3) 14일 이전 데이터 정리
        // 14일 전의 자정 시간을 필터링 기준으로 설정
        Calendar cutOffCal = Calendar.getInstance();
        cutOffCal.setTimeInMillis(now - twoWeeks);
        cutOffCal.set(Calendar.HOUR_OF_DAY, 0);
        cutOffCal.set(Calendar.MINUTE, 0);
        cutOffCal.set(Calendar.SECOND, 0);
        cutOffCal.set(Calendar.MILLISECOND, 0);
        long cutoff = cutOffCal.getTimeInMillis();
        Log.v(TAG, "Filtering criterion: " + now);
        long firstSleepStart = now;
        long earliestV0 = now;

        // 오래된 수면 데이터 삭제 (sleepEnd < cutoff)
        for (Sleep sleep : sleepDao.getAll()) {
            if (sleep.sleepEnd < cutoff) {
                sleepDao.delete(sleep);
                Log.v(TAG, "This sleep data was recorded before two weeks");
                continue;
            }
            if (sleep.sleepStart < firstSleepStart) {
                firstSleepStart = sleep.sleepStart;
            }
        }
        // 오래된 V0 데이터 삭제 (time < cutoff)
        for (V0 v0 : v0Dao.getAll()) {
            if (v0.time < cutoff) {
                v0Dao.delete(v0);
                Log.v(TAG, "This V0 data was recorded before two weeks");
                continue;
            }
            if (v0.time < earliestV0) {
                earliestV0 = v0.time;
            }
        }

        // V0의 시작점이 첫 수면보다 48시간 이전일 경우 저장된 V0를 모두 제거
        if (earliestV0 - firstSleepStart >= 2 * oneDay) {
            v0Dao.deleteRange(earliestV0, now);
        }


        // 4) 수면 날짜 업데이트
        long sleepOnset, workOnset, workOffset, sleepOnsetShow;
        int workType;
        sleepOnset = sharedPref.getLong("sleepOnset", now);
        workOnset = sharedPref.getLong("workOnset", now);
        workOffset = sharedPref.getLong("workOffset", now);
        sleepOnsetShow = sharedPref.getLong("sleepOnsetShow", now);
        workType = sharedPref.getInt("workType", 0);
        Long[] updatedDates = updateOnsetDate(now, sleepOnset, sleepOnsetShow, workOnset, workOffset);
        editor.putLong("sleepOnset", updatedDates[0]);
        editor.putLong("sleepOnsetShow", updatedDates[1]);
        editor.putLong("workOnset", updatedDates[2]);
        editor.putLong("workOffset", updatedDates[3]);
        editor.apply();
        ArrayList<Long> onsetOffsets = new ArrayList<>(Arrays.asList(updatedDates).subList(0, 4));

        List<Sleep> sleeps = getSleepData(sharedPref, sleepDao, lastDataUpdate, lastSleepUpdate, ILastSleepUpdate);

        if (!sleeps.isEmpty()) {
            CombineResult combineResult = do_simulation(db, sharedPref, sleeps, barEntries, onsetOffsets, lastDataUpdate, workType);
            v0s = combineResult.getV0s();
            barEntries = combineResult.getBarEntries();
            awarenesses = calculateAwareness(db, sleeps, v0s);
            sleepAwarenesses = calculateSleepAwareness(db, sleeps, v0s);
            if(now-lastBackendUpdate >= (1000*60*60*12)) {
                sendData(context, sleeps, new UploadCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "데이터가 전송되었습니다", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
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

        // Access token으로 sleep data 가져오기
        List<Sleep> sleeps = Collections.synchronizedList(sleepDao.getAll());


        boolean check = false;
        long lastSleep1 = 0;
        long befSleepStart = 0;
        long befSleepEnd = 0;
        ArrayList<Sleep> deleteTheSleeps = new ArrayList<>();

        for(Sleep sleep: new ArrayList<>(sleeps)){
            // 동일한 수면 데이터는 삭제
            if(befSleepStart == sleep.sleepStart && befSleepEnd == sleep.sleepEnd) {
                sleepDao.delete(sleep);
                sleeps.remove(sleep);
                Log.v(TAG, "same data");
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

            // 필터링된 데이터를 로깅
            Log.v("SLEEP REAL", sleepStart);
            Log.v("SLEEP REAL", sleepEnd);
            if(ILastSleepUpdate.isBefore(Instant.ofEpochMilli(sleep.sleepStart))){
                if(!check){
                    lastDataUpdate = Long.min(lastDataUpdate, sleep.sleepStart - oneDay);
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
                                     long lastDataUpdate, int workType){
        SharedPreferences.Editor editor = sharedPref.edit();
        //get V0 data
        V0Dao v0Dao = db.v0Dao();
        List<V0> v0s = Collections.synchronizedList(v0Dao.getAll());

        // 14일 전의 자정 시간을 필터링 기준으로 설정
        Calendar cutOffCal = Calendar.getInstance();
        cutOffCal.setTimeInMillis(now - twoWeeks);
        cutOffCal.set(Calendar.HOUR_OF_DAY, 0);
        cutOffCal.set(Calendar.MINUTE, 0);
        cutOffCal.set(Calendar.SECOND, 0);
        cutOffCal.set(Calendar.MILLISECOND, 0);
        long cutoff = cutOffCal.getTimeInMillis();
        Log.v(TAG, "Filtering criterion: " + now);

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
        long yesterday = now - oneDay;
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
            long firstSleepDayStart = (firstSleep.sleepStart + nineHours)/ oneDay;
            long firstSleepNoon = (firstSleepDayStart*oneDay) + (1000*60*60*12);
            Log.v("FIRST SLEEP DAY START", String.valueOf(firstSleepNoon));
            Log.v("FIRST SLEEP", String.valueOf(firstSleep.sleepStart));
            if(firstSleep.sleepStart+nineHours >= firstSleepNoon){
                startProcess = firstSleepNoon-nineHours;
                initV0=new double[]{0.8958, 0.5219, 0.5792, 12.4225};
            }else{
                startProcess = (firstSleepDayStart * oneDay)-nineHours;
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
        int[] sleepSuggestion;
        if (workType == 0) {
            sleepSuggestion = sleepModel.Sleep_pattern_suggestion_off(initV0, (int)(sleepOnset-now)/(1000*60*5), 5/60.0);
        } else {
             sleepSuggestion = sleepModel.Sleep_pattern_suggestion(initV0, (int)(sleepOnset-now)/(1000*60*5),
                    (int)(workOnset-now)/(1000*60*5), (int)(workOffset-now)/(1000*60*5), 5/60.0, isNight);
        }
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

        sleepPattern = sleepToArray(now, now+oneDay, newSleep);
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

    public static void sendData(Context context, List<Sleep> sleeps, UploadCallback callback) {
        // 1. API service 불러오기
        SleepDataAPI apiService = RetrofitClient.getClient(context).create(SleepDataAPI.class);

        // 2. 데이터 준비
        List<Sleep_struct> sleepPayloadList = new ArrayList<>();

        // KST 기준 ISO 8601 형식 formatter
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.KOREA);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -14);
        Date twoWeeksAgo = calendar.getTime();

        for(Sleep sleep: sleeps){
            // 필터링: 시작 시간이 미래가 아니고, 종료 시간이 최근 2주 이내인 데이터만 전송
            Date sleepStartDate = new Date(sleep.sleepStart);
            Date sleepEndDate = new Date(sleep.sleepEnd);
            if (sleepStartDate.after(now) || sleepEndDate.before(twoWeeksAgo)) continue;

            String sleepStart = formatter.format(sleepStartDate);
            String sleepEnd = formatter.format(sleepEndDate);
            sleepPayloadList.add(new Sleep_struct(sleepStart, sleepEnd));
        }
        Log.d(TAG, "업로드할 수면 데이터 # - " + sleepPayloadList.size());

        if (sleepPayloadList.isEmpty()) {
            Toast.makeText(context, "전송할 수면 데이터가 존재하지 않습니다",
                    Toast.LENGTH_SHORT).show();
        }

        // 3. 요청 구성 및 네트워크 호출
        SleepUploadPayload payload = new SleepUploadPayload(sleepPayloadList);

        apiService.uploadSleepData(payload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "데이터 업로드 성공. Status: " + response.code());
                    callback.onSuccess();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "알 수 없는 에러";
                        String errorMessage = "서버 에러: " + response.code() + " - " + errorBody;
                        Log.e(TAG, errorMessage);
                        callback.onFailure("데이터 업로드 실패: 서버 에러");
                    } catch (IOException e) {
                        callback.onFailure("데이터 업로드 실패: 에러 메시지 파싱 실패");
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "네트워크 에러: " + t.getMessage());
                callback.onFailure("데이터 업로드 실패: 네트워크 에러");
            }
        });
    }

    // SleepUploadPayload를 서버에 업로드하는 interface
    public interface SleepDataAPI {
        @POST("/sleepapp/android/")
        Call<Void> uploadSleepData(@Body SleepUploadPayload payload);

    }

    // Sleep data 업로드의 성공, 실패를 알려주는 callback 함수
    public interface UploadCallback {
        void onSuccess();
        void onFailure(String errorMessage);
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