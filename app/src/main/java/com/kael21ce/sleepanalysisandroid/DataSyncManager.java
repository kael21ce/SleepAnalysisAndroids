package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.room.Room;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kael21ce.sleepanalysisandroid.data.ApiClient;
import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.DailySurveyResponse;
import com.kael21ce.sleepanalysisandroid.data.DataMood;
import com.kael21ce.sleepanalysisandroid.data.DataSurvey;
import com.kael21ce.sleepanalysisandroid.data.KssSurveyResponse;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.kael21ce.sleepanalysisandroid.data.SleepBackend;
import com.kael21ce.sleepanalysisandroid.data.SleepFetchResponse;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 로그인 직후 서버에 저장된 데이터를 다시 읽어와 로컬(Room + SharedPreferences)에 복원한다.
// iOS WaitingView의 pullSleepsViaJSON / fetchDailySurveyAsync / fetchSurveyRecordsAsync에 대응.
// 각 항목은 서로 독립적으로 실패해도 나머지 복원을 막지 않는다(에러는 로그만 남김).
public class DataSyncManager {

    private static final String TAG = "DataSyncManager";
    private static final String MoodArrayKey = "MoodArray";
    private static final String AlertnessArrayKey = "AlertnessArray";

    public interface OnRestoreDone {
        void onDone();
    }

    public static void restoreFromServer(Context context, OnRestoreDone onDone) {
        Context appContext = context.getApplicationContext();
        AtomicInteger remaining = new AtomicInteger(3);
        Runnable countDown = () -> {
            if (remaining.decrementAndGet() == 0 && onDone != null) {
                onDone.onDone();
            }
        };

        restoreSleeps(appContext, countDown);
        restoreDailySurvey(appContext, countDown);
        restoreWeeklySurvey(appContext, countDown);
    }

    //수면 기록: 서버가 돌려준 구간과 겹치는 로컬 기록을 지운 뒤 새로 삽입 (iOS saveSleepsToCoreData와 동일 전략)
    private static void restoreSleeps(Context context, Runnable countDown) {
        RetrofitAPI api = ApiClient.api(context);
        api.getSleeps().enqueue(new Callback<SleepFetchResponse>() {
            @Override
            public void onResponse(Call<SleepFetchResponse> call, Response<SleepFetchResponse> response) {
                try {
                    List<SleepBackend> incoming = (response.isSuccessful() && response.body() != null)
                            ? response.body().sleep : null;
                    if (incoming != null && !incoming.isEmpty()) {
                        long minStart = Long.MAX_VALUE;
                        long maxEnd = Long.MIN_VALUE;
                        List<Sleep> toInsert = new ArrayList<>();
                        for (SleepBackend s : incoming) {
                            if (s.sleepStart > s.sleepEnd) continue;
                            minStart = Math.min(minStart, s.sleepStart);
                            maxEnd = Math.max(maxEnd, s.sleepEnd);
                            Sleep sleep = new Sleep();
                            sleep.sleepStart = s.sleepStart;
                            sleep.sleepEnd = s.sleepEnd;
                            toInsert.add(sleep);
                        }
                        if (!toInsert.isEmpty()) {
                            AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "sleep_wake")
                                    .allowMainThreadQueries().build();
                            try {
                                db.sleepDao().deleteOverlapping(maxEnd, minStart);
                                db.sleepDao().insertAll(toInsert);
                            } finally {
                                db.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.v(TAG, "restoreSleeps error: " + e.getMessage());
                }
                countDown.run();
            }

            @Override
            public void onFailure(Call<SleepFetchResponse> call, Throwable t) {
                Log.v(TAG, "restoreSleeps failed: " + t.getMessage());
                countDown.run();
            }
        });
    }

    //일일 설문(Hooper Index): 최근 7일치를 날짜별로 merge (iOS fetchDailySurvey와 동일 전략)
    private static void restoreDailySurvey(Context context, Runnable countDown) {
        RetrofitAPI api = ApiClient.api(context);
        String from = isoStringDaysAgo(7);
        String to = isoStringDaysAgo(0);

        api.getDailySurvey(from, to).enqueue(new Callback<DailySurveyResponse>() {
            @Override
            public void onResponse(Call<DailySurveyResponse> call, Response<DailySurveyResponse> response) {
                try {
                    List<DataMood> incoming = (response.isSuccessful() && response.body() != null)
                            ? response.body().results : null;
                    if (incoming != null && !incoming.isEmpty()) {
                        SharedPreferences sharedPref = context.getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
                        Gson gson = new Gson();
                        ArrayList<Records> recordsArrayList;
                        if (sharedPref.contains(MoodArrayKey)) {
                            Type type = new TypeToken<ArrayList<Records>>() {}.getType();
                            ArrayList<Records> loaded = gson.fromJson(sharedPref.getString(MoodArrayKey, "[]"), type);
                            recordsArrayList = loaded != null ? loaded : new ArrayList<>();
                        } else {
                            recordsArrayList = new ArrayList<>();
                        }
                        for (DataMood mood : incoming) {
                            recordsArrayList = SQMoodSendingActivity.findDateGroup(recordsArrayList, mood);
                        }
                        sharedPref.edit().putString(MoodArrayKey, gson.toJson(recordsArrayList)).apply();
                    }
                } catch (Exception e) {
                    Log.v(TAG, "restoreDailySurvey error: " + e.getMessage());
                }
                countDown.run();
            }

            @Override
            public void onFailure(Call<DailySurveyResponse> call, Throwable t) {
                Log.v(TAG, "restoreDailySurvey failed: " + t.getMessage());
                countDown.run();
            }
        });
    }

    //주간/즉시 졸림도(KSS) 설문: 최근 7일치로 전체 교체 (iOS fetchSurveyRecords와 동일 전략 - merge 아님)
    private static void restoreWeeklySurvey(Context context, Runnable countDown) {
        RetrofitAPI api = ApiClient.api(context);
        String from = isoStringDaysAgo(7);
        String to = isoStringDaysAgo(0);

        api.getSurvey(from, to, 1).enqueue(new Callback<KssSurveyResponse>() {
            @Override
            public void onResponse(Call<KssSurveyResponse> call, Response<KssSurveyResponse> response) {
                try {
                    List<DataSurvey> incoming = (response.isSuccessful() && response.body() != null)
                            ? response.body().results : null;
                    if (incoming != null && !incoming.isEmpty()) {
                        ArrayList<Records> recordsArrayList = new ArrayList<>();
                        for (DataSurvey survey : incoming) {
                            recordsArrayList = bucketByDay(recordsArrayList, survey);
                        }
                        SharedPreferences sharedPref = context.getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
                        sharedPref.edit().putString(AlertnessArrayKey, new Gson().toJson(recordsArrayList)).apply();
                    }
                } catch (Exception e) {
                    Log.v(TAG, "restoreWeeklySurvey error: " + e.getMessage());
                }
                countDown.run();
            }

            @Override
            public void onFailure(Call<KssSurveyResponse> call, Throwable t) {
                Log.v(TAG, "restoreWeeklySurvey failed: " + t.getMessage());
                countDown.run();
            }
        });
    }

    private static ArrayList<Records> bucketByDay(ArrayList<Records> recordsList, DataSurvey survey) {
        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.setTimeInMillis(survey.getTime());
        baseCalendar.set(Calendar.HOUR_OF_DAY, 0);
        baseCalendar.set(Calendar.MINUTE, 0);
        baseCalendar.set(Calendar.SECOND, 0);
        baseCalendar.set(Calendar.MILLISECOND, 0);

        for (Records records : recordsList) {
            Calendar recordCalendar = Calendar.getInstance();
            recordCalendar.setTime(records.getRecordDate());
            if (recordCalendar.get(Calendar.YEAR) == baseCalendar.get(Calendar.YEAR)
                    && recordCalendar.get(Calendar.MONTH) == baseCalendar.get(Calendar.MONTH)
                    && recordCalendar.get(Calendar.DAY_OF_MONTH) == baseCalendar.get(Calendar.DAY_OF_MONTH)) {
                records.getDataSurvey().add(survey);
                return recordsList;
            }
        }
        ArrayList<DataSurvey> dataSurveys = new ArrayList<>();
        dataSurveys.add(survey);
        recordsList.add(new Records(baseCalendar.getTime(), true, dataSurveys, new ArrayList<>()));
        return recordsList;
    }

    private static String isoStringDaysAgo(int days) {
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));
        long time = System.currentTimeMillis() - days * 24L * 60 * 60 * 1000;
        return iso.format(new Date(time));
    }
}
