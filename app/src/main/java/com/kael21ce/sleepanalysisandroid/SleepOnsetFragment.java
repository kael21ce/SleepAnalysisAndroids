package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SleepOnsetFragment extends Fragment implements ButtonTextUpdater{

    private Button sleepOnsetDateButton;
    private Button sleepOnsetTimeButton;
    private Button workOnsetDateButton;
    private Button workOnsetTimeButton;
    private Button workOffsetDateButton;
    private Button workOffsetTimeButton;
    public DatePickerDialog datePickerDialog;
    public TimePickerDialog timePickerDialog;
    long sleepOnset, workOnset, workOffset;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm aaa");
    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy.MM.dd");
    SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm aaa");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sleep_onset, container, false);
        sleepOnsetDateButton = v.findViewById(R.id.sleepOnsetDateButton);
        sleepOnsetTimeButton = v.findViewById(R.id.sleepOnsetTimeButton);
        workOnsetDateButton = v.findViewById(R.id.workOnsetDateButton);
        workOnsetTimeButton = v.findViewById(R.id.workOnsetTimeButton);
        workOffsetDateButton = v.findViewById(R.id.workOffsetDateButton);
        workOffsetTimeButton = v.findViewById(R.id.workOffsetTimeButton);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        sleepOnset = sharedPref.getLong("sleepOnset", System.currentTimeMillis());
        workOnset = sharedPref.getLong("workOnset", System.currentTimeMillis());
        workOffset = sharedPref.getLong("workOffset", System.currentTimeMillis());

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

        // Inflate the layout for this fragment
        return v;
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