package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;

import com.kael21ce.sleepanalysisandroid.data.Sleep;

import java.util.List;


public class ScheduleFragment extends Fragment {

    public CalendarView calendarView;
    public Fragment intervalFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();

        Context context = v.getContext();
        intervalFragment = new IntervalFragment();

        calendarView = v.findViewById(R.id.CalendarView);
        getChildFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit();

        //Load sleep intervals and send to IntervalFragment
        //get sleep data
        List<Sleep> sleeps = mainActivity.getSleeps();

        //Add sleep interval to specific date
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                //Load save data for specific time

            }
        });

        return v;
    }
}