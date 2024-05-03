package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SleepOnsetActivity extends AppCompatActivity implements ButtonTextUpdater{

    private Button sleepOnsetDateButton;
    private Button sleepOnsetTimeButton;
    private Button workOnsetDateButton;
    private Button workOnsetTimeButton;
    private Button workOffsetDateButton;
    private Button workOffsetTimeButton;
    private Button sleepSettingSubmitButton;
    public DatePickerDialog datePickerDialog;
    public TimePickerDialog timePickerDialog;
    long sleepOnset, workOnset, workOffset;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm aaa");
    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy.MM.dd");
    SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm aaa");
    long now, nineHours;

    @Override
    protected void onCreate(Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_sleep_onset);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton sleepOnsetBackButton = findViewById(R.id.SleepOnsetBackButton);
        //Click back button
        sleepOnsetBackButton.setOnClickListener(view -> {
            Intent nextIntent = new Intent(SleepOnsetActivity.this, SplashActivity.class);
            nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextIntent);
        });

        sleepOnsetDateButton = findViewById(R.id.sleepOnsetDateButton);
        sleepOnsetTimeButton = findViewById(R.id.sleepOnsetTimeButton);
        workOnsetDateButton = findViewById(R.id.workOnsetDateButton);
        workOnsetTimeButton = findViewById(R.id.workOnsetTimeButton);
        workOffsetDateButton = findViewById(R.id.workOffsetDateButton);
        workOffsetTimeButton = findViewById(R.id.workOffsetTimeButton);
        sleepSettingSubmitButton = findViewById(R.id.sleepSettingSubmitButton);

        nineHours = (1000*60*60*9);
        now = System.currentTimeMillis();

        MainActivity mainActivity = new MainActivity();

        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        sleepOnset = sharedPref.getLong("sleepOnset", now);
        workOnset = sharedPref.getLong("workOnset", now);
        workOffset = sharedPref.getLong("workOffset", now);

//        String sleepOnsetString = sdf.format(new Date(sleepOnset));
//        String workOnsetString = sdf.format(new Date(workOnset));
//        String workOffsetString = sdf.format(new Date(workOffset));

        sleepOnsetDateButton.setText(sdfDate.format(new Date(sleepOnset)));
        sleepOnsetTimeButton.setText(sdfTime.format(new Date(sleepOnset)));
        workOnsetDateButton.setText(sdfDate.format(new Date(workOnset)));
        workOnsetTimeButton.setText(sdfTime.format(new Date(workOnset)));
        workOffsetDateButton.setText(sdfDate.format(new Date(workOffset)));
        workOffsetTimeButton.setText(sdfTime.format(new Date(workOffset)));

        SleepOnsetActivity sleepOnsetActivity = this;
        sleepOnsetDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(this, sleepOnsetActivity);
            datePickerDialog.setData(0);
            datePickerDialog.setDatePicker((String) sleepOnsetDateButton.getText());
            datePickerDialog.show();
        });
        sleepOnsetTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(this, sleepOnsetActivity);
            timePickerDialog.setData(0);
            timePickerDialog.setTimePicker((String) sleepOnsetTimeButton.getText());
            timePickerDialog.show();
        });
        workOnsetDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(this, sleepOnsetActivity);
            datePickerDialog.setData(1);
            datePickerDialog.setDatePicker((String) workOnsetDateButton.getText());
            datePickerDialog.show();
        });
        workOnsetTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(this, sleepOnsetActivity);
            timePickerDialog.setData(1);
            timePickerDialog.setTimePicker((String) workOnsetTimeButton.getText());
            timePickerDialog.show();
        });
        workOffsetDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(this, sleepOnsetActivity);
            datePickerDialog.setData(2);
            datePickerDialog.setDatePicker((String) workOffsetDateButton.getText());
            datePickerDialog.show();
        });
        workOffsetTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(this, sleepOnsetActivity);
            timePickerDialog.setData(2);
            timePickerDialog.setTimePicker((String) workOffsetTimeButton.getText());
            timePickerDialog.show();
        });

        sleepSettingSubmitButton.setOnClickListener(view -> {
            String sleepOnsetDate = (String) sleepOnsetDateButton.getText();
            String sleepOnsetTime = (String) sleepOnsetTimeButton.getText();
            String sleepOnsetSDF = sleepOnsetDate + ' ' + sleepOnsetTime;
            String workOnsetDate = (String) workOnsetDateButton.getText();
            String workOnsetTime = (String) workOnsetTimeButton.getText();
            String workOnsetSDF = workOnsetDate + ' ' + workOnsetTime;
            String workOffsetDate = (String) workOffsetDateButton.getText();
            String workOffsetTime = (String) workOffsetTimeButton.getText();
            String workOffsetSDF = workOffsetDate + ' ' + workOffsetTime;
            Log.v("SLEEP ONSET SDF", sleepOnsetSDF);
            Log.v("WORK ONSET SDF", workOnsetSDF);
            Log.v("WORK OFFSET SDF", workOffsetSDF);

            Date sleepOnset = null;
            Date workOnset = null;
            Date workOffset = null;
            try {
                sleepOnset = sdf.parse(sleepOnsetSDF);
                workOnset = sdf.parse(workOnsetSDF);
                workOffset = sdf.parse(workOffsetSDF);
                //translate to local date time
//                LocalDateTime ldt1 = LocalDateTime.ofInstant(sleepOnset.toInstant(), ZoneId.systemDefault());
//                LocalDateTime ldt2 = LocalDateTime.ofInstant(workOnset.toInstant(), ZoneId.systemDefault());
//                LocalDateTime ldt3 = LocalDateTime.ofInstant(workOffset.toInstant(), ZoneId.systemDefault());
//                sleepOnset = Date.from(ldt1.atZone(ZoneId.systemDefault()).toInstant());
//                workOnset = Date.from(ldt2.atZone(ZoneId.systemDefault()).toInstant());
//                workOffset = Date.from(ldt3.atZone(ZoneId.systemDefault()).toInstant());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            assert sleepOnset != null;
            assert workOnset != null;
            assert workOffset != null;

            if(isValid(sleepOnset.getTime(), workOnset.getTime(), workOffset.getTime())) {
                mainActivity.setSleepOnset(sleepOnset.getTime());
                mainActivity.setWorkOnset(workOnset.getTime());
                mainActivity.setWorkOffset(workOffset.getTime());

                mainActivity.finish();
                startActivity(new Intent(mainActivity, SplashActivity.class));
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setTitle("ERROR");
                builder.setMessage("INVALID INPUT");

                builder.setNegativeButton("OK", (dialogInterface, i) -> dialogInterface.cancel());

                AlertDialog alert = builder.create();
                alert.setOnShowListener(arg0 -> {
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
                });
                alert.show();
            }
        });
    }

    public boolean isValid(long sleepOnset1, long workOnset1, long workOffset1){
        if(sleepOnset1 <= workOnset1 && workOnset1 <= workOffset1){
            if(workOnset1 - sleepOnset1 <= 1000*60*60*24 && workOffset1 - workOnset1 <= 1000*60*60*24){
                return true;
            }
        }
        return false;
    }

    //Change the text of Button
    public void setDateButtonText(String text, int isStartButton) {
        if (isStartButton==0) {
            if (sleepOnsetDateButton != null) {
                sleepOnsetDateButton.setText(text);
            }
        } else if(isStartButton == 1) {
            if (workOnsetDateButton != null) {
                workOnsetDateButton.setText(text);
            }
        } else{
            if (workOffsetDateButton != null) {
                workOffsetDateButton.setText(text);
            }
        }
    }

    public void setTimeButtonText(String text, int isStartButton) {
        if (isStartButton==0) {
            if (sleepOnsetTimeButton != null) {
                sleepOnsetTimeButton.setText(text);
            }
        } else if(isStartButton == 1) {
            if (workOnsetTimeButton != null) {
                workOnsetTimeButton.setText(text);
            }
        } else{
            if (workOffsetTimeButton != null) {
                workOffsetTimeButton.setText(text);
            }
        }
    }
}