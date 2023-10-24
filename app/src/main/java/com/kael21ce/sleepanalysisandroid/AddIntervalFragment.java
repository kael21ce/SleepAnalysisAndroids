package com.kael21ce.sleepanalysisandroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddIntervalFragment extends Fragment {

    public ImageButton backButton;
    public Button startDateButton;
    public Button startTimeButton;
    public Button endDateButton;
    public Button endTimeButton;
    public IntervalFragment intervalFragment;
    public DatePickerDialog datePickerDialog;
    public TimePickerDialog timePickerDialog;
    public String startTime;
    public String endTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_interval, container, false);

        startDateButton = v.findViewById(R.id.startDateButton);
        startTimeButton = v.findViewById(R.id.startTimeButton);
        endDateButton = v.findViewById(R.id.endDateButton);
        endTimeButton = v.findViewById(R.id.endTimeButton);


        //Return to IntervalFragment if backButton is clicked
        intervalFragment = new IntervalFragment();
        //get bundle and give it back
        Bundle bundle = this.getArguments();
        if(bundle == null){
            Log.v("bundle", "bundle failed to be fetched");
        }
        intervalFragment.setArguments(bundle);
        backButton = v.findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getParentFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit());

        //Set the initial added time to current time
        String current_date = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        String current_time = new SimpleDateFormat("aaa hh:mm").format(new Date());
        startDateButton.setText(current_date);
        startTimeButton.setText(current_time);
        endDateButton.setText(current_date);
        endTimeButton.setText(current_time);

        //Open Picker when buttons about date are clicked
        startDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(v.getContext());
            datePickerDialog.show();
        });
        endDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(v.getContext());
            datePickerDialog.show();
        });
        startTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(v.getContext());
            timePickerDialog.show();
        });
        endTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(v.getContext());
            timePickerDialog.show();
        });

        return  v;
    }
}