package com.kael21ce.sleepanalysisandroid;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.kael21ce.sleepanalysisandroid.data.Awareness;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ScheduleFragment extends Fragment {

    public MaterialCalendarView calendarView;
    private boolean isFolded;
    public Fragment intervalFragment;

    public Map<Long, List<Sleep>> sleepsData;

    SimpleDateFormat sdfDateTime = new SimpleDateFormat( "yyyy/MM/dd", Locale.KOREA);
    long now, nineHours;
    private static final String TAG = "ScheduleFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();

        Log.v("FRAGMENT", "SCHEDULE FRAGMENT");

        nineHours = (1000*9*60*60);
        now = System.currentTimeMillis();
        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        //get sleep data and calculate map values
        List<Sleep> sleeps = mainActivity.getSleeps();
        long oneDayToMils = 1000*60*60*24;
        sleepsData = new HashMap<>();
        List<Sleep> listSleep = new ArrayList<>();
        Log.v("SIZE OF SLEEP", String.valueOf(sleeps.size()));
        if(sleeps.size() > 0) {
            long curDate = (sleeps.get(0).sleepStart + nineHours) / oneDayToMils;
            for (Sleep sleep : sleeps) {
                Log.v("SLEEPSSS", String.valueOf(sleep.sleepStart));
                long sleepStartDate = ((sleep.sleepStart + nineHours) / oneDayToMils);
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
            if(listSleep.size() > 0){
                List<Sleep> putSleep = new ArrayList<>(listSleep);
                sleepsData.put(curDate, putSleep);
                listSleep.clear();
            }
        }

        //debug map
//        for (Map.Entry<Long, List<Sleep>> entry : sleepsData.entrySet()) {
//            Log.v("KEY", String.valueOf(entry.getKey()));
//            for(Sleep sleep2 : entry.getValue()){
//                Log.v("SLEEP START D", String.valueOf(sleep2.sleepStart));
//                Log.v("SLEEP END D", String.valueOf(sleep2.sleepEnd));
//            }
//        }
//        Log.v("THE MAP", String.valueOf(sleepsData.size()));

        //get today's day
        long todayDate = (now+nineHours)/oneDayToMils;
        Log.v("TODAY DATE", String.valueOf(todayDate));
        Log.v("TODAY DATE 2: ", sdfDateTime.format(new Date(now)));
        Log.v("today date 3", String.valueOf(now));

        //get awareness
        List<Awareness> awarenesses = mainActivity.getAwarenesses();
        long goodDuration = 0;
        long badDuration = 0;
        for(Awareness awareness: awarenesses){
            Log.v("AWARENESS VALUE IN SCHEDULE", String.valueOf(awareness.awarenessDay));
            Log.v("AWARENESS VALUE IN SCHEDULE", String.valueOf(awareness.goodDuration));
            Log.v("AWARENESS VALUE IN SCHEDULE", String.valueOf(awareness.badDuration));

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
        bundle.putString("date", sdfDateTime.format(new Date((todayDate*oneDayToMils)-nineHours)));

        Context context = v.getContext();
        intervalFragment = new IntervalFragment();
        intervalFragment.setArguments(bundle);

        calendarView = v.findViewById(R.id.CalendarView);
        calendarView.setSelectedDate(CalendarDay.today());
        getChildFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit();

        //Get the selected date after editing, adding and deleting the sleep data
        Bundle selectedBundle = getArguments();
        if (selectedBundle != null) {
            int year = selectedBundle.getInt("Year");
            int month = selectedBundle.getInt("Month");
            int day = selectedBundle.getInt("Day");
            Log.v(TAG, "Selected: " + year + "-" + month + 1 + "-" + day);
            calendarView.setSelectedDate(CalendarDay.from(year, month + 1, day));

            //Load IntervalFragment
            String myDate = year + "/" + month + 1 + "/" + day;
            Date date = null;
            try {
                date = sdfDateTime.parse(myDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            assert date != null;
            long dayInMillis = date.getTime();
            Log.v("DATE CHOSEN", String.valueOf(dayInMillis));
            long selectedDay = (dayInMillis+nineHours) / oneDayToMils;
            Log.v("day", String.valueOf(selectedDay));

            //get awareness
            List<Awareness> awarenesses1 = mainActivity.getAwarenesses();
            long goodDuration1 = 0;
            long badDuration1 = 0;
            for(Awareness awareness: awarenesses1){
                if(awareness.awarenessDay == selectedDay){
                    Log.v("SELECTED", "SELECTED");
                    goodDuration1 = awareness.goodDuration;
                    badDuration1 = awareness.badDuration;
                    break;
                }
            }

            Bundle bundle1 = new Bundle();
            //put awareness
            bundle1.putLong("goodDuration", goodDuration1);
            bundle1.putLong("badDuration", badDuration1);

            List<Sleep> initSleepData1 = sleepsData.get(selectedDay);
            if(initSleepData1 == null){
                initSleepData1 = new ArrayList<>();
            }
            int count1 = 0;
            for(Sleep sleep: initSleepData1){
                Log.v("THE INTERVAL'S SLEEP", String.valueOf(sleep.sleepStart));
                bundle1.putLong("sleepStart"+ count1, sleep.sleepStart);
                bundle1.putLong("sleepEnd"+ count1, sleep.sleepEnd);
                count1++;
            }
            bundle1.putInt("count", count1);
            bundle1.putString("date", myDate);

            intervalFragment = new IntervalFragment();
            intervalFragment.setArguments(bundle1);

            getChildFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit();
        }

        //Add decorator
        ReportedDecorator reportedDecorator = new ReportedDecorator();
        reportedDecorator.setResources(getResources());
        reportedDecorator.setSleepsData(sleepsData);
        calendarView.addDecorator(reportedDecorator);
        ImageButton handle = v.findViewById(R.id.calendarHandler);

        //Set the default mode of calendar
        if (sharedPref.contains("isFolded")) {
            boolean isFolded = sharedPref.getBoolean("isFolded", false);
            Log.v("isFolded", "isFolded: " + isFolded);
            if (!isFolded) {
                calendarView.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS).commit();
            } else {
                ObjectAnimator.ofFloat(handle, View.ROTATION, 0f, 180f).setDuration(200).start();
                calendarView.state().edit().setCalendarDisplayMode(CalendarMode.WEEKS).commit();
            }
        }

        //Make foldable calendar
        handle.setOnClickListener(view -> {
            //Save whether folded or not
            if (!sharedPref.contains("isFolded")) {
                editor.putBoolean("isFolded", false);
                editor.apply();
            }
            boolean isFolded = sharedPref.getBoolean("isFolded", false);
            Log.v("CalendarHandler", "isFolded: " + isFolded);
            if (!isFolded) {
                ObjectAnimator.ofFloat(handle, View.ROTATION, 0f, 180f).setDuration(200).start();
                calendarView.state().edit().setCalendarDisplayMode(CalendarMode.WEEKS).commit();
                editor.putBoolean("isFolded", true);
                editor.apply();
            } else {
                calendarView.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS).commit();
                ObjectAnimator.ofFloat(handle, View.ROTATION, 180f, 0f).setDuration(200).start();
                editor.putBoolean("isFolded", false);
                editor.apply();
            }
        });

        //Add sleep interval to specific date
        calendarView.setOnDateChangedListener((widget, Cdate, selected) -> {
            //Load save data for specific time
            int year = Cdate.getYear();
            int month = Cdate.getMonth();
            int dayOfMonth = Cdate.getDay();
            String myDate = String.valueOf(year)+'/'+ month +'/'+ dayOfMonth;
            Log.v("MY DATE", myDate);
            Date date = null;
            try {
                date = sdfDateTime.parse(myDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            assert date != null;
            long dayInMillis = date.getTime();
            Log.v("DATE CHOSEN", String.valueOf(dayInMillis));
            long selectedDay = (dayInMillis+nineHours) / oneDayToMils;
            Log.v("day", String.valueOf(selectedDay));

            //get awareness
            List<Awareness> awarenesses1 = mainActivity.getAwarenesses();
            long goodDuration1 = 0;
            long badDuration1 = 0;
            for(Awareness awareness: awarenesses1){
                if(awareness.awarenessDay == selectedDay){
                    Log.v("SELECTED", "SELECTED");
                    goodDuration1 = awareness.goodDuration;
                    badDuration1 = awareness.badDuration;
                    break;
                }
            }

            Bundle bundle1 = new Bundle();
            //put awareness
            bundle1.putLong("goodDuration", goodDuration1);
            bundle1.putLong("badDuration", badDuration1);

            List<Sleep> initSleepData1 = sleepsData.get(selectedDay);
            if(initSleepData1 == null){
                initSleepData1 = new ArrayList<>();
            }
            int count1 = 0;
            for(Sleep sleep: initSleepData1){
                Log.v("THE INTERVAL'S SLEEP", String.valueOf(sleep.sleepStart));
                bundle1.putLong("sleepStart"+ count1, sleep.sleepStart);
                bundle1.putLong("sleepEnd"+ count1, sleep.sleepEnd);
                count1++;
            }
            bundle1.putInt("count", count1);
            bundle1.putString("date", myDate);

            intervalFragment = new IntervalFragment();
            intervalFragment.setArguments(bundle1);

            getChildFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit();
        });


        return v;
    }
}