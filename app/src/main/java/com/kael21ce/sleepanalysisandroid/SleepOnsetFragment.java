package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kael21ce.sleepanalysisandroid.data.Sleep;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class SleepOnsetFragment extends Fragment implements ButtonTextUpdater{

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sleep_onset, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();

        sleepOnsetDateButton = v.findViewById(R.id.sleepOnsetDateButton);
        sleepOnsetTimeButton = v.findViewById(R.id.sleepOnsetTimeButton);
        workOnsetDateButton = v.findViewById(R.id.workOnsetDateButton);
        workOnsetTimeButton = v.findViewById(R.id.workOnsetTimeButton);
        workOffsetDateButton = v.findViewById(R.id.workOffsetDateButton);
        workOffsetTimeButton = v.findViewById(R.id.workOffsetTimeButton);
        sleepSettingSubmitButton = v.findViewById(R.id.sleepSettingSubmitButton);

        nineHours = (1000*60*60*9);
        now = System.currentTimeMillis();

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
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

        SleepOnsetFragment sleepOnsetFragment = this;
        sleepOnsetDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(v.getContext(), sleepOnsetFragment);
            datePickerDialog.setData(0);
            datePickerDialog.show();
        });
        sleepOnsetTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(v.getContext(), sleepOnsetFragment);
            timePickerDialog.setData(0);
            timePickerDialog.show();
        });
        workOnsetDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(v.getContext(), sleepOnsetFragment);
            datePickerDialog.setData(1);
            datePickerDialog.show();
        });
        workOnsetTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(v.getContext(), sleepOnsetFragment);
            timePickerDialog.setData(1);
            timePickerDialog.show();
        });
        workOffsetDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(v.getContext(), sleepOnsetFragment);
            datePickerDialog.setData(2);
            datePickerDialog.show();
        });
        workOffsetTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(v.getContext(), sleepOnsetFragment);
            timePickerDialog.setData(2);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

        // Inflate the layout for this fragment
        return v;
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