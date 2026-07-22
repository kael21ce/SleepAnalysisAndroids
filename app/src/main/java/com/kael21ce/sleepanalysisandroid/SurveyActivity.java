package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kael21ce.sleepanalysisandroid.data.ApiClient;
import com.kael21ce.sleepanalysisandroid.data.DataSurvey;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Karolinska Sleepiness Scale (KSS), 표준 9점 척도. 1=매우 또렷함 ... 9=매우 졸림(졸음과 싸움)
public class SurveyActivity extends AppCompatActivity {
    private int level = 0;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private static final String AlertnessArrayKey = "AlertnessArray";
    private ArrayList<Records> recordsArrayList = new ArrayList<>();
    private ArrayList<Records> baseArrayList = new ArrayList<>();
    private Gson gson = new Gson();
    private String baseJson, alertJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        MainActivity mainActivity = new MainActivity();
        mainActivity.surveyList().add(this);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        //Back button
        ImageButton backSurveyButton = findViewById(R.id.surveyBackButton);
        Button endSurveyButton = findViewById(R.id.endSurveyButton);

        //KSS 옵션 (1~9)
        ArrayList<LinearLayout> optionArrayList = new ArrayList<>();
        ArrayList<TextView> optionContentArrayList = new ArrayList<>();
        int[] optionIds = {R.id.kssOption1, R.id.kssOption2, R.id.kssOption3, R.id.kssOption4,
                R.id.kssOption5, R.id.kssOption6, R.id.kssOption7, R.id.kssOption8, R.id.kssOption9};
        int[] contentIds = {R.id.kssContent1, R.id.kssContent2, R.id.kssContent3, R.id.kssContent4,
                R.id.kssContent5, R.id.kssContent6, R.id.kssContent7, R.id.kssContent8, R.id.kssContent9};
        for (int i = 0; i < optionIds.length; i++) {
            optionArrayList.add(findViewById(optionIds[i]));
            optionContentArrayList.add(findViewById(contentIds[i]));
        }

        //Click back button
        backSurveyButton.setOnClickListener(view -> {
            Intent nextIntent = new Intent(SurveyActivity.this, SplashActivity.class);
            nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextIntent);
        });

        //제출 전까지 비활성화 (미선택 상태 방지)
        endSurveyButton.setEnabled(false);
        endSurveyButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.blue_2, null));

        //KSS 옵션 클릭
        for (int i = 0; i < optionArrayList.size(); i++) {
            final int position = i + 1;
            optionArrayList.get(i).setOnClickListener(view -> {
                setKssSelection(optionArrayList, optionContentArrayList, position);
                level = position;
                endSurveyButton.setEnabled(true);
                endSurveyButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.blue_1, null));
            });
        }

        // Make alertness survey list
        baseJson = gson.toJson(baseArrayList);
        if (!sharedPref.contains(AlertnessArrayKey)) {
            sharedPref.edit().putString(AlertnessArrayKey, baseJson).apply();
        } else {
            Log.v("SurveyActivity", "Alertness list is loaded");
            alertJson = sharedPref.getString(AlertnessArrayKey, baseJson);
            Type type = new TypeToken<ArrayList<Records>>() {}.getType();
            Gson loadGson = new Gson();
            recordsArrayList = loadGson.fromJson(alertJson, type);
        }

        endSurveyButton.setOnClickListener(view -> {
            if (level == 0) return;

            Intent nextIntent = new Intent(this, SplashActivity.class);
            nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            sendSurvey();

            String userEmail = sharedPref.getString("User_Email", "tester33");
            long time = System.currentTimeMillis();
            long sleep_onset = sharedPref.getLong("sleepOnset", time);
            long work_onset = sharedPref.getLong("workOnset", time);
            long work_offset = sharedPref.getLong("workOffset", time);
            DataSurvey dataSurvey = new DataSurvey(userEmail, sleep_onset, work_onset, work_offset, getLevel(), time);
            recordsArrayList = findAlertGroup(recordsArrayList, dataSurvey);
            Gson gson1 = new Gson();
            alertJson = gson1.toJson(recordsArrayList);
            editor.putString(AlertnessArrayKey, alertJson).apply();

            editor.putLong("LastSurveyTime", System.currentTimeMillis()).apply();

            startActivity(nextIntent);
            for (int i = 0; i < mainActivity.surveyList().size(); i++) {
                mainActivity.surveyList().get(i).finish();
            }
        });
    }

    //KSS 옵션 선택 표시 (단일 선택)
    private void setKssSelection(ArrayList<LinearLayout> optionArrayList, ArrayList<TextView> contentArrayList, int position) {
        for (int i = 0; i < optionArrayList.size(); i++) {
            if (i + 1 == position) {
                optionArrayList.get(i).setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.corner_8_black_stroke, null));
                contentArrayList.get(i).setTextColor(ResourcesCompat.getColor(getResources(), R.color.black, null));
            } else {
                optionArrayList.get(i).setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.corner_8_gray_stroke, null));
                contentArrayList.get(i).setTextColor(ResourcesCompat.getColor(getResources(), R.color.gray_4, null));
            }
        }
    }

    public ArrayList<Records> findAlertGroup(ArrayList<Records> recordsList, DataSurvey alert) {
        // Extract date information from daily survey result
        long baseTime = alert.getTime();
        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.setTimeInMillis(baseTime);
        baseCalendar.set(Calendar.HOUR_OF_DAY, 0);
        baseCalendar.set(Calendar.MINUTE, 0);
        baseCalendar.set(Calendar.SECOND, 0);
        baseCalendar.set(Calendar.MILLISECOND, 0);

        Date baseDate = baseCalendar.getTime();
        if (recordsList.size() == 0) {

            ArrayList<DataSurvey> alertList = new ArrayList<>();
            alertList.add(alert);

            ArrayList<com.kael21ce.sleepanalysisandroid.data.DataMood> moodList = new ArrayList<>();

            Records records = new Records(baseDate, true, alertList, moodList);
            recordsList.add(records);

            Log.v("SurveyActivity", "Record list is created");

            return recordsList;
        } else {
            int n = recordsList.size();
            boolean isUpdateNeeded = true;
            for (int i = 0; i < Math.min(n, 14); i++) {
                Records records = recordsList.get(n-i-1);
                Date date = records.getRecordDate();
                ArrayList<DataSurvey> dataSurveys = records.getDataSurvey();
                ArrayList<com.kael21ce.sleepanalysisandroid.data.DataMood> dataMoods = records.getDataMood();

                Calendar calendaR = Calendar.getInstance();
                calendaR.setTime(date);
                if (calendaR.get(Calendar.YEAR) == baseCalendar.get(Calendar.YEAR)
                        && calendaR.get(Calendar.MONTH) == baseCalendar.get(Calendar.MONTH)
                        && calendaR.get(Calendar.DAY_OF_MONTH) == baseCalendar.get(Calendar.DAY_OF_MONTH)) {
                    dataSurveys.add(alert);

                    Records r = new Records(date, true, dataSurveys, dataMoods);
                    recordsList.set(n-i-1, r);
                    isUpdateNeeded = false;
                    Log.v("SurveyActivity", "Record list is added");
                }
            }
            if (isUpdateNeeded) {
                Log.v("SurveyActivity", "Record list is added newly");
                ArrayList<DataSurvey> alertList = new ArrayList<>();
                alertList.add(alert);

                ArrayList<com.kael21ce.sleepanalysisandroid.data.DataMood> moodList = new ArrayList<>();

                Records r = new Records(baseDate, true, alertList, moodList);
                recordsList.add(r);
            }
            Log.v("SurveyActivity", "Record list size: " + recordsList.size());
            return recordsList;
        }
    }

    private void sendSurvey(){
        RetrofitAPI retrofitAPI = ApiClient.api(this);
        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString("User_Email", "tester33");
        long time = System.currentTimeMillis();
        long sleep_onset = sharedPref.getLong("sleepOnset", time);
        long work_onset = sharedPref.getLong("workOnset", time);
        long work_offset = sharedPref.getLong("workOffset", time);

        DataSurvey survey = new DataSurvey(userEmail, sleep_onset, work_onset, work_offset, getLevel(), time);
        Call<DataSurvey> call = retrofitAPI.createSurvey(survey);
        call.enqueue(new Callback<DataSurvey>() {
            @Override
            public void onResponse(Call<DataSurvey> call, Response<DataSurvey> response) {
                // this method is called when we get response from our api.
                Locale currentLocale = Locale.getDefault();
                String language = currentLocale.getLanguage();
                if(response.code() <= 300) {
                    if (language.equals("ko")) {
                        Toast.makeText(SurveyActivity.this, "데이터가 전송되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SurveyActivity.this, "Data added to API", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (language.equals("ko")) {
                        Toast.makeText(SurveyActivity.this, "데이터 전송에 실패했습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SurveyActivity.this, "Data sending failed", Toast.LENGTH_SHORT).show();
                    }
                    // we are getting response from our body
                    // and passing it to our modal class.
                    DataSurvey responseFromAPI = response.body();

                    // on below line we are getting our data from modal class and adding it to our string.
                    String responseString = "Response Code : " + response.code() + "\nName : " + "\n";
                    Log.v("RESPONSE", responseString);
                }
            }

            @Override
            public void onFailure(Call<DataSurvey> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
                Log.v("ERROR", "Error found is : " + t.getMessage());
            }
        });
    }

    //Return level
    private int getLevel() {
        return this.level;
    }
}
