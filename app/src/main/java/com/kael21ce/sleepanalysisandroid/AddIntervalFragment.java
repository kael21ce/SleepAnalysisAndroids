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

import com.kael21ce.sleepanalysisandroid.data.Sleep;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddIntervalFragment extends Fragment {

    public ImageButton backButton;
    public Button startDateButton;
    public Button startTimeButton;
    public Button endDateButton;
    public Button endTimeButton;
    public Button addButton;
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

        MainActivity mainActivity = (MainActivity)getActivity();

        startDateButton = v.findViewById(R.id.startDateButton);
        startTimeButton = v.findViewById(R.id.startTimeButton);
        endDateButton = v.findViewById(R.id.endDateButton);
        endTimeButton = v.findViewById(R.id.endTimeButton);
        addButton = v.findViewById(R.id.addButton);

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

        SimpleDateFormat sdf = new SimpleDateFormat("yyy.MM.dd aaa hh:mm", Locale.KOREA);

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
        addButton.setOnClickListener(view -> {
            Sleep add_sleep = new Sleep();
            String startDate = (String) startDateButton.getText();
            String startTime = (String) startTimeButton.getText();
            String startSDF = startDate + ' ' + startTime;
            String endDate = (String) endDateButton.getText();
            String endTime = (String) endTimeButton.getText();
            String endSDF = endDate + ' ' + endTime;
            Log.v("START SDF", startSDF);
            Log.v("END SDF", endSDF);
            Date sleepStartDate = null;
            Date sleepEndDate = null;
            try {
                sleepStartDate = sdf.parse(startSDF);
                sleepEndDate = sdf.parse(endSDF);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            assert sleepStartDate != null;
            assert sleepEndDate != null;
            add_sleep.sleepStart = sleepStartDate.getTime();
            add_sleep.sleepEnd = sleepEndDate.getTime();
            mainActivity.addSleep(add_sleep);
        });

        return  v;
    }
}