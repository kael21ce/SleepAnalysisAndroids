package com.kael21ce.sleepanalysisandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kael21ce.sleepanalysisandroid.data.DataMood;
import com.kael21ce.sleepanalysisandroid.data.DataSurvey;
import com.kael21ce.sleepanalysisandroid.data.Sleep;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {
    long now;
    private boolean creation = true;
    private static final String MoodArrayKey = "MoodArray";
    private static final String AlertnessArrayKey = "AlertnessArray";

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();

        List<Sleep> sleeps = mainActivity.getSleeps();

        //Survey Caption
        LinearLayout SurveyUpperView = v.findViewById(R.id.SurveyUpperView);
        TextView surveyDescription = v.findViewById(R.id.surveyDescription);
        ImageButton surveyUpperButton = v.findViewById(R.id.surveyUpperButton);

        //Daily Survey(Hooper Index) Caption
        LinearLayout DailySurveyUpperView = v.findViewById(R.id.DailySurveyUpperView);
        ImageButton dailySurveyUpperButton = v.findViewById(R.id.dailySurveyUpperButton);

        //Survey record lists
        LinearLayout alertnessSurveyView = v.findViewById(R.id.alertnessSurveyView);
        LinearLayout dailySurveyView = v.findViewById(R.id.dailySurveyView);
        alertnessSurveyView.setVisibility(View.VISIBLE);
        dailySurveyView.setVisibility(View.VISIBLE);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // System time
        now = System.currentTimeMillis();

        // 수면 추천 페이지는 삭제했고, 설문 버튼/기록만 보여준다.
        SurveyUpperView.setVisibility(View.VISIBLE);
        DailySurveyUpperView.setVisibility(View.VISIBLE);

        //Survey description and button: move to SurveyActivity
        surveyDescription.setText("🌙 지금 얼마나 졸리신가요?");
        SurveyUpperView.setOnClickListener(view -> {
            Log.v("HomeFragment", "Var now: " + now + " / System: " + System.currentTimeMillis());
            long lastSurvey = sharedPref.getLong("LastSurveyTime", 0);
            if (Math.abs(lastSurvey - System.currentTimeMillis()) < 30*60*1000) {
                Toast.makeText(getActivity().getApplicationContext(), "적어도 30분 뒤 설문을 진행해주세요.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent surveyIntent = new Intent(v.getContext(), SurveyActivity.class);
                surveyIntent.putExtra("firstDone", 0);
                startActivity(surveyIntent);
            }
        });
        surveyUpperButton.setOnClickListener(view -> {
            Log.v("HomeFragment", "Var now: " + now + " / System: " + System.currentTimeMillis());
            long lastSurvey = sharedPref.getLong("LastSurveyTime", 0);
            if (Math.abs(lastSurvey - System.currentTimeMillis()) < 30*60*1000) {
                Toast.makeText(getActivity().getApplicationContext(), "적어도 30분 뒤 설문을 진행해주세요.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent surveyIntent = new Intent(v.getContext(), SurveyActivity.class);
                surveyIntent.putExtra("firstDone", 0);
                startActivity(surveyIntent);
            }
        });

        //Daily Survey(Hooper Index) 버튼: move to SQMoodSendingActivity
        View.OnClickListener dailySurveyClickListener = view -> {
            Bundle temp = new Bundle();
            Intent surveyIntent = new Intent(v.getContext(), SQMoodSendingActivity.class);
            surveyIntent.putExtra("SurveyType", 0);
            surveyIntent.putExtra("moodData", temp);
            startActivity(surveyIntent);
        };
        DailySurveyUpperView.setOnClickListener(dailySurveyClickListener);
        dailySurveyUpperButton.setOnClickListener(dailySurveyClickListener);

        // Daily alertness summary
        Gson alertGson = new Gson();
        ArrayList<Records> baseArrayList = new ArrayList();
        ArrayList<Records> alertArrayList;
        String baseJson = alertGson.toJson(baseArrayList);

        RecyclerView alertRecyclerView = v.findViewById(R.id.AlertnessSurveyRecyclerView);
        RecordsAdapter alertAdapter = new RecordsAdapter();
        String alertJson = sharedPref.getString(AlertnessArrayKey, baseJson);
        Log.v("HomeFragment", "Alert Json " + alertJson);
        Type type = new TypeToken<ArrayList<Records>>() {}.getType();
        alertArrayList = alertGson.fromJson(alertJson, type);
        int alertRecordsSize = alertArrayList.size();
        Log.v("HomeFragment", "Alert list size: " + alertRecordsSize);
        int alertTotalRecords = Math.min(14, alertRecordsSize);

        // Add empty records if there is no records in current day
        long baseTime = System.currentTimeMillis();
        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.setTimeInMillis(baseTime);
        baseCalendar.set(Calendar.HOUR_OF_DAY, 0);
        baseCalendar.set(Calendar.MINUTE, 0);
        baseCalendar.set(Calendar.SECOND, 0);
        baseCalendar.set(Calendar.MILLISECOND, 0);
        boolean isEmptyAlertNeeded = true;
        for (int l = 0; l < alertTotalRecords; l++) {
            Records r = alertArrayList.get(alertRecordsSize-l-1);
            Date rDate = r.getRecordDate();
            Calendar calendaR = Calendar.getInstance();
            calendaR.setTime(rDate);
            if (calendaR.get(Calendar.YEAR) == baseCalendar.get(Calendar.YEAR)
                    && calendaR.get(Calendar.MONTH) == baseCalendar.get(Calendar.MONTH)
                    && calendaR.get(Calendar.DAY_OF_MONTH) == baseCalendar.get(Calendar.DAY_OF_MONTH)) {
                isEmptyAlertNeeded = false;
                break;
            }
        }
        if (isEmptyAlertNeeded || alertTotalRecords == 0) {
            ArrayList<DataSurvey> emptyAlerts = new ArrayList<>();
            ArrayList<DataMood> emptyMoods = new ArrayList<>();
            Records emptyRecords = new Records(baseCalendar.getTime(), true, emptyAlerts, emptyMoods);
            alertArrayList.add(emptyRecords);
        }
        alertRecordsSize = alertArrayList.size();
        Log.v("HomeFragment", "Alert list size 2: " + alertRecordsSize);
        alertTotalRecords = Math.min(14, alertArrayList.size());

        for (int k = 0; k < alertTotalRecords; k++) {
            Records r = alertArrayList.get(alertRecordsSize-k-1);
            Log.v("HomeFragment", "isAlertness in Alertness: " + r.isAlertness());
            if (r.isAlertness()) {
                alertAdapter.addItem(r);
            }
        }
        alertRecyclerView.setAdapter(alertAdapter);
        LinearLayoutManager alertLayoutManager = new LinearLayoutManager(v.getContext(),
                LinearLayoutManager.VERTICAL, false);
        alertRecyclerView.setLayoutManager(alertLayoutManager);

        alertRecyclerView.post(() -> {
            if (alertAdapter.getItemCount() > 0) {
                View firstItemView = alertRecyclerView.getChildAt(0);
                if (firstItemView != null) {
                    int itemHeight = firstItemView.getHeight();
                    ViewGroup.LayoutParams params = alertRecyclerView.getLayoutParams();
                    params.height = itemHeight;
                    alertRecyclerView.setLayoutParams(params);
                }
            }
        });

        // Daily survey summary
        Gson gson = new Gson();
        ArrayList<Records> dailyArrayList = new ArrayList<>();

        RecyclerView dailyRecyclerView = v.findViewById(R.id.DailySurveyRecyclerView);
        RecordsAdapter dailyAdapter = new RecordsAdapter();
        String dailyJson = sharedPref.getString(MoodArrayKey, baseJson);
        Log.v("HomeFragment", "Daily Json " + dailyJson);
        dailyArrayList = gson.fromJson(dailyJson, type);
        int recordsSize = dailyArrayList.size();

        // Delete the wrong data in dailyArrayList
        int space = 0;
        boolean isNeedToRecover = false;
        for (int l = 0; l < recordsSize; l++) {
            Records r = dailyArrayList.get(l-space);
            if (r.isAlertness) {
                dailyArrayList.remove(l-space);
                space = space + 1;
                isNeedToRecover = true;
            }
        }

        // Recover the survey data from the server
        int recoverSpace = 0;
        if (isNeedToRecover) {
            // Recover the deleted daily mood surveys
        }

        dailyJson = gson.toJson(dailyArrayList);
        editor.putString(MoodArrayKey, dailyJson).apply();
        recordsSize = recordsSize - space + recoverSpace;
        int totalRecords = Math.min(14, recordsSize);

        // Add empty records if there is no records in current day
        boolean isEmptyNeeded = true;
        for (int l = 0; l < totalRecords; l++) {
            Records r = dailyArrayList.get(recordsSize-l-1);
            Date rDate = r.getRecordDate();
            Calendar calendaR = Calendar.getInstance();
            calendaR.setTime(rDate);
            if (calendaR.get(Calendar.YEAR) == baseCalendar.get(Calendar.YEAR)
                    && calendaR.get(Calendar.MONTH) == baseCalendar.get(Calendar.MONTH)
                    && calendaR.get(Calendar.DAY_OF_MONTH) == baseCalendar.get(Calendar.DAY_OF_MONTH)) {
                isEmptyNeeded = false;
                break;
            }
        }
        if (isEmptyNeeded || totalRecords == 0) {
            ArrayList<DataSurvey> emptyAlerts = new ArrayList<>();
            ArrayList<DataMood> emptyMoods = new ArrayList<>();
            Records emptyRecords = new Records(baseCalendar.getTime(), false, emptyAlerts, emptyMoods);
            dailyArrayList.add(emptyRecords);
        }
        recordsSize = dailyArrayList.size();
        totalRecords = Math.min(14, recordsSize);

        for (int k = 0; k < totalRecords; k++) {
            Records r = dailyArrayList.get(recordsSize-k-1);
            Log.v("HomeFragment", "isAlertness in Daily: " + r.isAlertness());
            if (!r.isAlertness()) {
                dailyAdapter.addItem(r);
            }
        }
        dailyRecyclerView.setAdapter(dailyAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(v.getContext(),
                LinearLayoutManager.VERTICAL, false);
        dailyRecyclerView.setLayoutManager(layoutManager);

        dailyRecyclerView.post(() -> {
            if (dailyAdapter.getItemCount() > 0) {
                View firstItemView = dailyRecyclerView.getChildAt(0);
                if (firstItemView != null) {
                    int itemHeight = firstItemView.getHeight();
                    ViewGroup.LayoutParams params = dailyRecyclerView.getLayoutParams();
                    params.height = itemHeight;
                    dailyRecyclerView.setLayoutParams(params);
                }
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v("HomeFragment", "onStart is called");
        if (creation) {
            creation = false;
        } else {
            Log.v("HomeFragment", "Resume");
            now = System.currentTimeMillis();
        }
    }


    private ArrayList<Records> sortRecordsList(ArrayList<Records> original) {
        // Reorder the dailyArrayList
        ArrayList<Long> timeList = new ArrayList<>();
        ArrayList<Pair> pairList = new ArrayList<>();
        for (int o = 0; o < original.size(); o++) {
            Records itemO = original.get(o);
            Date dateO = itemO.getRecordDate();
            long timeO = dateO.getTime();
            timeList.add(timeO);
            pairList.add(new Pair(timeO, itemO));
        }
        pairList.sort(Comparator.comparingLong(pair -> pair.time));

        ArrayList<Records> orderedArrayList = new ArrayList<>();
        for (Pair pair : pairList) {
            orderedArrayList.add(pair.records);
        }
        return orderedArrayList;
    }

    private ArrayList<Records> recoverDailyArrayList(SharedPreferences sharedPref, ArrayList<Records> original) {
//        ArrayList<DataMood> recoverMoodList = getMood(sharedPref);
        ArrayList<DataMood> recoverMoodList = new ArrayList<>();
        ArrayList<DataMood> restArray = new ArrayList<>();
        boolean isNeedToBeAdded = true;
        for (int m = 0; m < recoverMoodList.size(); m++) {
            DataMood mMood = recoverMoodList.get(m);
            Calendar mCal = Calendar.getInstance();
            mCal.setTimeInMillis(mMood.getTime());
            for (int n = 0; n < original.size(); n++) {
                Records nRecords = original.get(n);
                Calendar nCal = Calendar.getInstance();
                nCal.setTime(nRecords.getRecordDate());
                ArrayList<DataMood> nMoodArray = nRecords.getDataMood();
                ArrayList<DataSurvey> nAlertArray = nRecords.getDataSurvey();

                if (mCal.get(Calendar.YEAR) == nCal.get(Calendar.YEAR)
                        && mCal.get(Calendar.MONTH) == nCal.get(Calendar.MONTH)
                        && mCal.get(Calendar.DAY_OF_MONTH) == nCal.get(Calendar.DAY_OF_MONTH)) {
                    nMoodArray.add(mMood);

                    Records r = new Records(nRecords.getRecordDate(), false, nAlertArray, nMoodArray);
                    original.set(n, r);
                    isNeedToBeAdded = false;
                }
            }
            if (isNeedToBeAdded) {
                restArray.add(mMood);
                isNeedToBeAdded = false;
            }
        }
        for (int l = 0; l < restArray.size(); l++) {
            DataMood lItem = restArray.get(l);
            Calendar lCal = Calendar.getInstance();
            lCal.setTimeInMillis(lItem.getTime());
            lCal.set(Calendar.HOUR_OF_DAY, 0);
            lCal.set(Calendar.MINUTE, 0);
            lCal.set(Calendar.SECOND, 0);
            lCal.set(Calendar.MILLISECOND, 0);

            ArrayList<DataSurvey> dummyArray = new ArrayList<>();
            ArrayList<DataMood> lMoodArray = new ArrayList<>();
            lMoodArray.add(lItem);

            Date lDate = lCal.getTime();
            Records r = new Records(lDate, false, dummyArray, lMoodArray);
            original.add(r);
        }
        ArrayList<Records> orderedArrayList = sortRecordsList(original);
        return orderedArrayList;
    }
}

class Pair {
    long time;
    Records records;

    Pair(long time, Records records) {
        this.time = time;
        this.records = records;
    }
}