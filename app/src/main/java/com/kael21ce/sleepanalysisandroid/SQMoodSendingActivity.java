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
import com.kael21ce.sleepanalysisandroid.data.DataMood;
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

// Hooper Index 4항목(1~7점, 낮을수록 컨디션 양호). 한국스포츠정책과학원 국가대표스포츠과학지원센터 체력·컨디셔닝팀 설문 문항 그대로 사용.
public class SQMoodSendingActivity extends AppCompatActivity {

    private static final String name = "SurveyType"; // 0=피로도, 1=수면의질, 2=스트레스, 3=근육통
    private static final String MoodArrayKey = "MoodArray";

    private static final String[][] HOOPER_TITLES = {
            {"현재 얼마나 피로하다고 느끼십니까?", "피로도 (Fatigue)"},
            {"지난밤 수면의 질은 어떠했습니까?", "수면의 질 (Sleep Quality)"},
            {"현재 스트레스 수준은 어느 정도입니까?", "스트레스 (Stress)"},
            {"현재 근육통 또는 근육의 불편감은 어느 정도입니까?", "근육통 (Muscle Soreness)"}
    };
    private static final String[][] HOOPER_OPTIONS = {
            {"1. 매우 상쾌함", "2. 상쾌함", "3. 약간 피곤함", "4. 보통", "5. 다소 피곤함", "6. 피곤함", "7. 매우 피곤함"},
            {"1. 매우 좋음", "2. 좋음", "3. 비교적 좋음", "4. 보통", "5. 비교적 나쁨", "6. 나쁨", "7. 매우 나쁨"},
            {"1. 전혀 없음", "2. 매우 낮음", "3. 낮음", "4. 보통", "5. 다소 높음", "6. 높음", "7. 매우 높음"},
            {"1. 전혀 없음", "2. 매우 경미함", "3. 경미함", "4. 보통", "5. 다소 심함", "6. 심함", "7. 매우 심함"}
    };
    private static final String[] MOOD_DATA_KEYS = {"fatigue", "sleepQuality", "stress", "muscleSoreness"};

    int surveyType = 0;
    int position = 0; // 1~7, 0=미선택
    Bundle moodData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqmood_sending);

        MainActivity mainActivity = new MainActivity();
        mainActivity.surveyList().add(this);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Back button - 이전 문항(스택에 남아있는 이전 SQMoodSendingActivity)으로 복귀, 첫 문항이면 설문 종료
        ImageButton sqMoodBackButton = findViewById(R.id.SQMoodBackButton);
        sqMoodBackButton.setOnClickListener(view -> finish());

        TextView sQMoodTitle = findViewById(R.id.sQMoodTitle);
        TextView sQDescription = findViewById(R.id.sQMoodDescription);

        ArrayList<LinearLayout> optionArrayList = new ArrayList<>();
        ArrayList<TextView> optionContentArrayList = new ArrayList<>();
        int[] optionIds = {R.id.firstButton, R.id.secondButton, R.id.thirdButton, R.id.fourthButton,
                R.id.fifthButton, R.id.sixthButton, R.id.seventhButton};
        int[] contentIds = {R.id.firstContent, R.id.secondContent, R.id.thirdContent, R.id.fourthContent,
                R.id.fifthContent, R.id.sixthContent, R.id.seventhContent};
        for (int i = 0; i < optionIds.length; i++) {
            optionArrayList.add(findViewById(optionIds[i]));
            optionContentArrayList.add(findViewById(contentIds[i]));
        }
        Button sQMoodButton = findViewById(R.id.SQMoodButton);

        //Get the sent survey type / accumulated Hooper 응답
        Intent sentIntent = getIntent();
        this.surveyType = sentIntent.getIntExtra(name, 0);
        Bundle incoming = sentIntent.getBundleExtra("moodData");
        this.moodData = incoming != null ? incoming : new Bundle();

        if (surveyType < 0 || surveyType > 3) surveyType = 0;

        sQMoodTitle.setText(HOOPER_TITLES[surveyType][0]);
        sQDescription.setText(HOOPER_TITLES[surveyType][1]);
        sQMoodButton.setText(surveyType == 3 ? "기록하기" : "다음");

        for (int i = 0; i < optionArrayList.size(); i++) {
            optionContentArrayList.get(i).setText(HOOPER_OPTIONS[surveyType][i]);
        }

        sQMoodButton.setEnabled(false);
        sQMoodButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.blue_2, null));

        for (int i = 0; i < optionArrayList.size(); i++) {
            final int level = i + 1;
            optionArrayList.get(i).setOnClickListener(view -> {
                setHooperSelection(optionArrayList, optionContentArrayList, level);
                position = level;
                sQMoodButton.setEnabled(true);
                sQMoodButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.blue_1, null));
            });
        }

        sQMoodButton.setOnClickListener(view -> {
            if (position == 0) return;
            moodData.putInt(MOOD_DATA_KEYS[surveyType], position);

            if (surveyType < 3) {
                Intent nextIntent = new Intent(this, SQMoodSendingActivity.class);
                nextIntent.putExtra(name, surveyType + 1);
                nextIntent.putExtra("moodData", moodData);
                startActivity(nextIntent);
            } else {
                submitHooperSurvey(mainActivity);
            }
        });
    }

    //Hooper 옵션 선택 표시 (단일 선택)
    private void setHooperSelection(ArrayList<LinearLayout> optionArrayList, ArrayList<TextView> contentArrayList, int level) {
        for (int i = 0; i < optionArrayList.size(); i++) {
            if (i + 1 == level) {
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

    // 서버 스키마는 그대로 두고 Hooper Index 4항목을 기존 daily_survey 필드에 매핑해 전송:
    // sleep_quality<-수면의질, mood_anx<-스트레스, daily_alertness<-피로도, mood_irr<-근육통.
    // latency/mood_high/mood_low는 더 이상 쓰지 않아 0으로 고정 전송.
    private void submitHooperSurvey(MainActivity mainActivity) {
        int fatigue = moodData.getInt("fatigue");
        int sleepQuality = moodData.getInt("sleepQuality");
        int stress = moodData.getInt("stress");
        int muscleSoreness = moodData.getInt("muscleSoreness");

        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String userEmail = sharedPref.getString("User_Email", "tester33");
        long time = System.currentTimeMillis();

        DataMood mood = new DataMood(userEmail, 0, fatigue, sleepQuality, 0, 0, stress, muscleSoreness, time);

        //로컬 기록 저장 (HomeFragment의 일일 설문 기록 리스트와 동일한 스키마)
        Gson baseGson = new Gson();
        ArrayList<Records> recordsArrayList;
        String baseJson = baseGson.toJson(new ArrayList<Records>());
        if (!sharedPref.contains(MoodArrayKey)) {
            recordsArrayList = new ArrayList<>();
        } else {
            String moodJson = sharedPref.getString(MoodArrayKey, baseJson);
            Type type = new TypeToken<ArrayList<Records>>() {}.getType();
            ArrayList<Records> loaded = baseGson.fromJson(moodJson, type);
            recordsArrayList = loaded != null ? loaded : new ArrayList<>();
        }
        recordsArrayList = findDateGroup(recordsArrayList, mood);
        String moodJson = baseGson.toJson(recordsArrayList);
        editor.putString(MoodArrayKey, moodJson).apply();

        //서버 전송
        RetrofitAPI retrofitAPI = ApiClient.api(this);
        Call<DataMood> call = retrofitAPI.createMood(mood);
        call.enqueue(new Callback<DataMood>() {
            @Override
            public void onResponse(Call<DataMood> call, Response<DataMood> response) {
                Locale currentLocale = Locale.getDefault();
                String language = currentLocale.getLanguage();
                if (response.code() <= 300) {
                    Toast.makeText(SQMoodSendingActivity.this,
                            language.equals("ko") ? "설문이 전송되었습니다" : "Survey added to API",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SQMoodSendingActivity.this,
                            language.equals("ko") ? "설문 전송에 실패했습니다" : "Survey sending failed",
                            Toast.LENGTH_SHORT).show();
                    Log.v("RESPONSE", "Response Code : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DataMood> call, Throwable t) {
                Log.v("ERROR", "Error found is : " + t.getMessage());
            }
        });

        Intent endIntent = new Intent(SQMoodSendingActivity.this, SplashActivity.class);
        endIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(endIntent);
        for (int i = 0; i < mainActivity.surveyList().size(); i++) {
            mainActivity.surveyList().get(i).finish();
        }
    }

    public static ArrayList<Records> findDateGroup(ArrayList<Records> recordsList, DataMood mood) {
        // Extract date information from daily survey result
        long baseTime = mood.getTime();
        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.setTimeInMillis(baseTime);
        baseCalendar.set(Calendar.HOUR_OF_DAY, 0);
        baseCalendar.set(Calendar.MINUTE, 0);
        baseCalendar.set(Calendar.SECOND, 0);
        baseCalendar.set(Calendar.MILLISECOND, 0);

        Date baseDate = baseCalendar.getTime();
        if (recordsList.size() == 0) {
            ArrayList<DataMood> moodList = new ArrayList<>();
            moodList.add(mood);
            ArrayList<DataSurvey> alertList = new ArrayList<>();
            Records records = new Records(baseDate, false, alertList, moodList);
            recordsList.add(records);
            Log.v("SQMoodSendingActivity", "Record list is created");
            return recordsList;
        } else {
            int n = recordsList.size();
            boolean isUpdateNeeded = true;
            for (int i = 0; i < Math.min(n, 14); i++) {
                Records records = recordsList.get(n - i - 1);
                Date date = records.getRecordDate();
                ArrayList<DataSurvey> dataSurveys = records.getDataSurvey();
                ArrayList<DataMood> dataMoods = records.getDataMood();

                Calendar calendaR = Calendar.getInstance();
                calendaR.setTime(date);
                if (calendaR.get(Calendar.YEAR) == baseCalendar.get(Calendar.YEAR)
                        && calendaR.get(Calendar.MONTH) == baseCalendar.get(Calendar.MONTH)
                        && calendaR.get(Calendar.DAY_OF_MONTH) == baseCalendar.get(Calendar.DAY_OF_MONTH)) {
                    dataMoods.add(mood);
                    Records r = new Records(date, false, dataSurveys, dataMoods);
                    recordsList.set(n - i - 1, r);
                    isUpdateNeeded = false;
                    Log.v("SQMoodSendingActivity", "Record list is added");
                }
            }
            if (isUpdateNeeded) {
                Log.v("SQMoodSendingActivity", "Record list is added newly");
                ArrayList<DataMood> moodList = new ArrayList<>();
                moodList.add(mood);
                ArrayList<DataSurvey> alertList = new ArrayList<>();
                Records r = new Records(baseDate, false, alertList, moodList);
                recordsList.add(r);
            }
            return recordsList;
        }
    }
}
