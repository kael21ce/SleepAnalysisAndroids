package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;


public class ScheduleFragment extends Fragment {

    public CalendarView calendarView;
    public ImageButton plusButton;
    public Fragment intervalFragment;
    public Fragment addIntervalFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);

        Context context = v.getContext();
        intervalFragment = new IntervalFragment();

        calendarView = v.findViewById(R.id.CalendarView);
        getChildFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit();

        //Add sleep interval to specific date
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            //Set Fragment

            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                //Load save data for specific time
            }
        });

        return v;
    }
}