package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Calendar;

public class LatencyActivity extends AppCompatActivity {
    private int latency = 0;
    private static final String survey_key = "SQMood";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latency);

        MainActivity mainActivity = new MainActivity();
        mainActivity.surveyList().add(this);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 건너뛰기 누르면 activity 종료
        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        TextView skipTextButton = findViewById(R.id.latencySkipTextButton);
        skipTextButton.setOnClickListener(view -> {
            // 설문이 띄워진 것을 저장 -> 계속 설문을 요청하지 않도록 하기
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            sharedPref.edit().putInt(survey_key, day).apply();

            Intent skipIntent = new Intent(LatencyActivity.this, SplashActivity.class);
            skipIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(skipIntent);
        });

        EditText latencyMinutes = findViewById(R.id.latencyMinutes);
        Button latencyButton = findViewById(R.id.latencyButton);

        //Turn off the latencyButton if there is no input
        latencyButton.setEnabled(false);
        latencyButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));

        //Get mood data from SQMoodSendingActivity
        Intent sentIntent = getIntent();
        Bundle moodData = sentIntent.getBundleExtra("moodData");
        int firstDone = sentIntent.getIntExtra("firstDone", 1);
        Intent endIntent = new Intent(this, SurveyActivity.class);


        latencyMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (latencyMinutes.getText().toString().isEmpty()) {
                    latencyButton.setEnabled(false);
                    latencyButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else {
                    latencyButton.setEnabled(true);
                    latencyButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    latency = Integer.parseInt(latencyMinutes.getText().toString());

                    latencyButton.setOnClickListener(v -> {
                        assert moodData != null;
                        moodData.putInt("latency", latency);
                        endIntent.putExtra("moodData", moodData);
                        endIntent.putExtra("firstDone", 1);
                        startActivity(endIntent);
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }
}