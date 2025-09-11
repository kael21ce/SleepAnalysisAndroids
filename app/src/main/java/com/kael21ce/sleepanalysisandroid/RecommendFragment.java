package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.AppDatabaseSingleton;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.kael21ce.sleepanalysisandroid.data.SleepDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RecommendFragment extends Fragment {

    public ImageButton sleepButton;
    public ImageButton napButton;
    public ImageButton workButton;
    SimpleDateFormat sdfDateTimeRecomm = new SimpleDateFormat("a hh:mm", Locale.KOREA);
    SimpleDateFormat sdfDateTimeRecomm2 = new SimpleDateFormat("H : mm", Locale.getDefault());
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.KOREA);
    String mainSleepStartString,sleepOnsetString, mainSleepEndString, workOnsetString, workOffsetString, napSleepStartString, napSleepEndString;
    String sleepOnsetDisplaying, workOnsetDisplaying, workOffsetDisplaying;
    long now, nineHours;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy"+ "HH:mm", Locale.KOREA);
        nineHours = (1000*60*60*9);
        now = System.currentTimeMillis();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        long sleepOnset = sharedPref.getLong("sleepOnset", now);
        String test = sdfDateTime.format(new Date(sleepOnset));
        Log.v("tag_test", test);

        AppDatabase db = AppDatabaseSingleton.getInstance(getActivity().getApplicationContext());
        SleepDao sleepDao = db.sleepDao();
        List<Sleep> sleeps = sleepDao.getAll();
        for(Sleep sleep: sleeps){
            String sleepStart = sdfDateTime.format(new Date(sleep.sleepStart));
            String sleepEnd = sdfDateTime.format(new Date(sleep.sleepEnd));
            Log.v("theSleepR", sleepStart);
            Log.v("theSleepR2", sleepEnd);
        }

        MainActivity mainActivity = (MainActivity)getActivity();

        SharedPreferences sharedPref2 = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref2.edit();

        String user_name = sharedPref2.getString("User_Name", "UserName");

        // Update
        Long [] updatedDates = updateOnsetDate(now, sharedPref2.getLong("sleepOnset", now), sharedPref2.getLong("sleepOnsetShow", now),
                sharedPref2.getLong("workOnset", now), sharedPref2.getLong("workOffset", now));
        mainActivity.setSleepOnset(updatedDates[0]);
        editor.putLong("sleepOnsetShow", updatedDates[1]).apply();
        mainActivity.setWorkOnset(updatedDates[2]);
        mainActivity.setWorkOffset(updatedDates[3]);

        TimeZone timeZone = TimeZone.getDefault();
        sdfDateTime.setTimeZone(timeZone);
        sdfDateTimeRecomm.setTimeZone(timeZone);
        sdfDateTimeRecomm2.setTimeZone(timeZone);

        mainSleepStartString = sdfTime.format(new Date(mainActivity.getMainSleepStart()));
        mainSleepEndString = sdfTime.format(new Date(mainActivity.getMainSleepEnd()));
        napSleepStartString = sdfTime.format(new Date(mainActivity.getNapSleepStart()));
        napSleepEndString = sdfTime.format(new Date(mainActivity.getNapSleepEnd()));
        sleepOnsetString = sdfTime.format(new Date(mainActivity.getSleepOnset()));
        workOnsetString = sdfTime.format(new Date(sharedPref2.getLong("workOnset", now)));
        workOffsetString = sdfTime.format(new Date(sharedPref2.getLong("workOffset", now)));
        sleepOnsetDisplaying = sdfDateTimeRecomm2.format(new Date(sharedPref2.getLong("sleepOnsetShow", now)));
        workOnsetDisplaying = sdfDateTimeRecomm2.format(new Date(sharedPref2.getLong("workOnset", now)));
        workOffsetDisplaying = sdfDateTimeRecomm2.format(new Date(sharedPref2.getLong("workOffset", now)));

        View v = inflater.inflate(R.layout.fragment_recommend, container, false);

        //No data
        LinearLayout noDataLayout = v.findViewById(R.id.noDataLayout);
        ImageView no_data = v.findViewById(R.id.no_data);
        Glide.with(v.getContext()).load(R.raw.no_data).into(no_data);
        Button addDataButton = v.findViewById(R.id.addDataButton);

        //If no onset, offset data, show noDataLayout
        LinearLayout infoView = v.findViewById(R.id.InfoView);
        TextView noDataDescription = v.findViewById(R.id.noDataDescription);
        noDataDescription.setText(user_name + "님에게 딱 맞는 수면 패턴을 추천해 드릴게요");

        // 일정이 입력되었는지 저장 및 확인
        if (!sharedPref2.contains("isRecommended")) {
            editor.putBoolean("isRecommended", false).apply();
        }

        boolean isRecommended = sharedPref2.getBoolean("isRecommended", false);
        if (sharedPref2.contains("sleepOnset") && sharedPref2.contains("workOnset") && sharedPref2.contains("workOffset")) {
            if (!isRecommended) {
                noDataLayout.setVisibility(View.VISIBLE);
                infoView.setVisibility(View.GONE);
            } else {
                noDataLayout.setVisibility(View.GONE);
                infoView.setVisibility(View.VISIBLE);
            }
        } else {
            infoView.setVisibility(View.GONE);
        }

        //Move to WhenSleepFragment
        addDataButton.setOnClickListener(view -> {
            WhenSleepFragment whenSleepFragment = new WhenSleepFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, whenSleepFragment).commit();
            mainActivity.setGoneBottomNavi();
        });

        TextView hopeTimeText = v.findViewById(R.id.HopeTimeDetail);
        TextView workTimeStart = v.findViewById(R.id.WorkTimeStart);
        TextView workTimeEnd = v.findViewById(R.id.WorkTimeEnd);
        TextView infoText = v.findViewById(R.id.InfoText);
        ImageButton infoButton = v.findViewById(R.id.infoButton);

        hopeTimeText.setText(sleepOnsetDisplaying);
        workTimeStart.setVisibility(View.INVISIBLE);
        int workType = sharedPref2.getInt("workType", 0);
        if (workType != 0) {
            workTimeEnd.setText(workOnsetDisplaying + " - " + workOffsetDisplaying);
        } else {
            workTimeEnd.setText("휴무");
        }


        //User name setting
        infoText.setText("일정");

        //Recommendation information
        ImageButton recommendInfoButton = v.findViewById(R.id.RecommendInfoButton);
        recommendInfoButton.setOnClickListener(view->{
            Intent infoIntent = new Intent(v.getContext(), InfoActivity.class);
            infoIntent.putExtra("Location",5);
            startActivity(infoIntent);
        });

        // InfoButton을 클릭하면 WhenSleepFragment -> WhenWorkFragment로 이동
        infoButton.setOnClickListener(view -> {
            WhenSleepFragment whenSleepFragment = new WhenSleepFragment();
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .addToBackStack(null)
                    .replace(R.id.mainFrame, whenSleepFragment).commit();
            mainActivity.setGoneBottomNavi();
        });
        return v;
    }

    public static Long[] updateOnsetDate(long currentTime, long sleepOnset, long sleepOnsetShow, long workOnset, long workOffset) {
        long oneDayToMils = 1000*60*60*24;
        long tenMinToMils = 1000*60*10;
        long oneHourToMils = 1000*60*60;

        // Keep sleepOnsetShow before workOnset minus 1 day
        while (sleepOnsetShow < workOnset - oneDayToMils) {
            sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            sleepOnset = sleepOnsetShow;
        }

        // Ensure workOnset is after sleepOnset
        while (workOnset < sleepOnset) {
            workOnset = workOnset + oneDayToMils;
        }

        // Ensure workOffset is after workOnset
        while (workOffset < workOnset) {
            workOffset = workOffset + oneDayToMils;
        }

        // Adjust sleepOnset if currentTime is within sleepOnset and workOnset
        if (sleepOnset <= currentTime && currentTime <= workOnset) {
            while (sleepOnset < currentTime) {
                sleepOnset = currentTime + tenMinToMils;
            }
        }

        // Ensure sleepOnsetShow is not before currentTime
        if (workOnset - oneHourToMils <= sleepOnset) {
            while (sleepOnsetShow < currentTime) {
                sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            }
            sleepOnset = sleepOnsetShow;
        }

        // Repeat the adjustments for sleepOnsetShow, workOnset, and workOffset
        while (sleepOnsetShow < workOnset - oneDayToMils) {
            sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            sleepOnset = sleepOnsetShow;
        }
        while (workOnset < sleepOnset) {
            workOnset = workOnset + oneDayToMils;
        }
        while (workOffset < workOnset) {
            workOffset = workOffset + oneDayToMils;
        }

        // Update work if it is ended
        while (currentTime > workOffset) {
            workOnset = workOnset + oneDayToMils;
            workOffset = workOffset + oneDayToMils;
        }

        return new Long[]{sleepOnset, sleepOnsetShow, workOnset, workOffset};
    }


}