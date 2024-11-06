package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;

public class LatencyActivity extends AppCompatActivity {
    private int latency = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latency);

        EditText latencyMinutes = findViewById(R.id.latencyMinutes);
        Button latencyButton = findViewById(R.id.latencyButton);

        //Turn off the latencyButton if there is no input
        latencyButton.setEnabled(false);

        //Get mood data from SQMoodSendingActivity
        Intent sentIntent = getIntent();
        Bundle moodData = sentIntent.getBundleExtra("moodData");
        Intent endIntent = new Intent(this, SurveyActivity.class);


        latencyMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (latencyMinutes.getText().toString().isEmpty()) {
                    latencyButton.setEnabled(false);
                } else {
                    latencyButton.setEnabled(true);
                    latency = Integer.parseInt(latencyMinutes.getText().toString());

                    latencyButton.setOnClickListener(v -> {
                        assert moodData != null;
                        moodData.putInt("latency", latency);
                        endIntent.putExtra("moodData", moodData);
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