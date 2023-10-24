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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ScheduleFragment extends Fragment {

    public CalendarView calendarView;
    public Fragment intervalFragment;

    public Map<Integer, List<Sleep>> sleepsData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();

        //get sleep data and calculate map values
        List<Sleep> sleeps = mainActivity.getSleeps();
        int oneDayToMils = 1000*60*60*24;
        sleepsData = new HashMap<>();
        List<Sleep> listSleep = new ArrayList<>();
        if(sleeps.size() > 0) {
            int curDate = (int) sleeps.get(0).sleepStart / oneDayToMils;
            for (Sleep sleep : sleeps) {
                int sleepStartDate = (int) sleep.sleepStart / oneDayToMils;
                if (curDate != sleepStartDate) {
                    sleepsData.put(curDate, listSleep);
                    listSleep.clear();
                    listSleep.add(sleep);
                }
            }
        }

        Bundle bundle = new Bundle();
        //get today's day
        int todayDate = (int)System.currentTimeMillis()/oneDayToMils;
        List<Sleep> initSleepData = sleepsData.get(todayDate);
        if(initSleepData == null){
            initSleepData = new ArrayList<>();
        }
        int count = 0;
        for(Sleep sleep: initSleepData){
            bundle.putLong("sleepStart"+count, sleep.sleepStart);
            bundle.putLong("sleepEnd"+count, sleep.sleepEnd);
            count++;
        }
        bundle.putInt("count", count);

        Context context = v.getContext();
        intervalFragment = new IntervalFragment();
        intervalFragment.setArguments(bundle);

        calendarView = v.findViewById(R.id.CalendarView);
        getChildFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit();

        //Load sleep intervals and send to IntervalFragment

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