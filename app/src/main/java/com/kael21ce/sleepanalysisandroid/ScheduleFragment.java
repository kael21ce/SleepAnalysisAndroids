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
import com.kael21ce.sleepanalysisandroid.data.Awareness;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ScheduleFragment extends Fragment {

    public CalendarView calendarView;
    public Fragment intervalFragment;

    public Map<Long, List<Sleep>> sleepsData;

    SimpleDateFormat sdfDateTime = new SimpleDateFormat( "yyyy/MM/dd", Locale.KOREA);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();

        //get sleep data and calculate map values
        List<Sleep> sleeps = mainActivity.getSleeps();
        long oneDayToMils = 1000*60*60*24;
        sleepsData = new HashMap<>();
        List<Sleep> listSleep = new ArrayList<>();
        if(sleeps.size() > 0) {
            long curDate = sleeps.get(0).sleepStart / oneDayToMils;
            for (Sleep sleep : sleeps) {
                long sleepStartDate = (sleep.sleepStart / oneDayToMils);
                if (curDate != sleepStartDate) {
                    List<Sleep> putSleep = new ArrayList<>(listSleep);
                    sleepsData.put(curDate, putSleep);
                    listSleep.clear();
                    listSleep.add(sleep);
                    curDate = sleepStartDate;
                }else{
                    listSleep.add(sleep);
                }
            }
        }

        //get today's day
        long todayDate = System.currentTimeMillis()/oneDayToMils;

        //get awareness
        List<Awareness> awarenesses = mainActivity.getAwarenesses();
        long goodDuration = 0;
        long badDuration = 0;
        for(Awareness awareness: awarenesses){
            if(awareness.awarenessDay == todayDate){
                goodDuration = awareness.goodDuration;
                badDuration = awareness.badDuration;
                break;
            }
        }

        Bundle bundle = new Bundle();
        //put awareness
        bundle.putLong("goodDuration", goodDuration);
        bundle.putLong("badDuration", badDuration);

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

        //debug map
//        for (Map.Entry<Long, List<Sleep>> entry : sleepsData.entrySet()) {
//            Log.v("KEY", String.valueOf(entry.getKey()));
//            for(Sleep sleep2 : entry.getValue()){
//                Log.v("SLEEP START D", String.valueOf(sleep2.sleepStart));
//                Log.v("SLEEP END D", String.valueOf(sleep2.sleepEnd));
//            }
//        }

        //Load sleep intervals and send to IntervalFragment

        //Add sleep interval to specific date
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                //Load save data for specific time
                String myDate = String.valueOf(year)+'/'+String.valueOf(month)+'/'+String.valueOf(dayOfMonth);
                Log.v("MY DATE", myDate);
                Date date = null;
                try {
                    date = sdfDateTime.parse(myDate);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                assert date != null;
                long dayInMillis = date.getTime();
                long selectedDay = dayInMillis / oneDayToMils;
                Log.v("day", String.valueOf(selectedDay));

                //get awareness
                List<Awareness> awarenesses = mainActivity.getAwarenesses();
                long goodDuration = 0;
                long badDuration = 0;
                for(Awareness awareness: awarenesses){
                    if(awareness.awarenessDay == selectedDay){
                        Log.v("SELECTED", "SELECTED");
                        goodDuration = awareness.goodDuration;
                        badDuration = awareness.badDuration;
                        break;
                    }
                }

                Bundle bundle = new Bundle();
                //put awareness
                bundle.putLong("goodDuration", goodDuration);
                bundle.putLong("badDuration", badDuration);

                List<Sleep> initSleepData = sleepsData.get(selectedDay);
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

                intervalFragment = new IntervalFragment();
                intervalFragment.setArguments(bundle);

                getChildFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit();
            }
        });

        return v;
    }
}