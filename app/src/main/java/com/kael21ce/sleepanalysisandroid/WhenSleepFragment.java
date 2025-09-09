package com.kael21ce.sleepanalysisandroid;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import nl.joery.timerangepicker.TimeRangePicker;

public class WhenSleepFragment extends Fragment {
    String hour1, hour2, minute1, minute2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_when_sleep, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();

        //Hide action bar
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        //Back to RecommendFragment
        ImageButton sleepBackButton = v.findViewById(R.id.sleepBackButton);
        sleepBackButton.setOnClickListener(view -> {
            assert mainActivity != null;
            mainActivity.setVisibleBottomNavi();
            mainActivity.setBottomNaviItem(R.id.tabRecommend);
            RecommendFragment recommendFragment = new RecommendFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, recommendFragment).commit();

            //Show action bar
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            }
        });

        //Get the sleep onset
        TimeRangePicker whenSleepPicker = v.findViewById(R.id.WhenSleepPicker);
        TextView whenSleepText = v.findViewById(R.id.whenSleepText);
        whenSleepPicker.setStartTimeMinutes(0);
        whenSleepPicker.setEndTimeMinutes(0);
        whenSleepText.setText(time2String(0));
        int whenSleep = whenSleepPicker.getStartTimeMinutes();
        final String[] whenSleepStr = {time2String(whenSleep)};
        whenSleepPicker.setOnDragChangeListener(new TimeRangePicker.OnDragChangeListener() {
            @Override
            public boolean onDragStart(@NonNull TimeRangePicker.Thumb thumb) {
                if (thumb.equals(TimeRangePicker.Thumb.START)) {
                    return false; // 시작 thumb는 움직이지 못하게 하기
                } else {
                    return true;
                }
            }

            @Override
            public void onDragStop(@NonNull TimeRangePicker.Thumb thumb) {
            }
        });

        whenSleepPicker.setOnTimeChangeListener(new TimeRangePicker.OnTimeChangeListener() {
            @Override
            public void onStartTimeChange(@NonNull TimeRangePicker.Time time) {

            }

            @Override
            public void onEndTimeChange(@NonNull TimeRangePicker.Time time) {
                int endTimeMinutes = time.getTotalMinutes();
                // Start thumb가 end thumb와 일치하도록 수정
                whenSleepPicker.setStartTimeMinutes(endTimeMinutes);

                // 시간 변화를 화면에 표시
                whenSleepStr[0] = time2String(endTimeMinutes);
                whenSleepText.setText(whenSleepStr[0]);

            }

            @Override
            public void onDurationChange(@NonNull TimeRangePicker.TimeDuration timeDuration) {
            }
        });

        //Send sleep onset to whenWorkFragment
        Button whenSleepButton = v.findViewById(R.id.whenSleepButton);
        whenSleepButton.setOnClickListener(view -> {
            Bundle onSetBundle = new Bundle();
            onSetBundle.putString("SleepOnset", whenSleepStr[0]);
            //Move to whenWorkFragment
            WhenWorkFragment whenWorkFragment = new WhenWorkFragment();
            whenWorkFragment.setArguments(onSetBundle);
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, whenWorkFragment).commit();
        });
        return v;
    }

    // Change minutes from TimeRangePicker to String HH:MM
    public String time2String(int minutes) {
        int hour = minutes / 60;
        int minute = minutes % 60;
        @SuppressLint("DefaultLocale") String hourStr = String.format("%02d", hour);
        @SuppressLint("DefaultLocale") String minuteStr = String.format("%02d", minute);
        return hourStr + ":" + minuteStr;
    }
}