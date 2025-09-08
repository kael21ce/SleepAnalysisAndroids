package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.AppDatabaseSingleton;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.kael21ce.sleepanalysisandroid.data.SleepDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class RecommendFragment extends Fragment {

    public ImageButton sleepButton;
    public ImageButton napButton;
    public ImageButton workButton;
    private TextView startTime;
    private TextView endTime;
    private TextView sleepImportanceText;
    private TextView sleepTypeText;
    private TextView stateDescriptionText;
    private TextView stateDescriptionSmallText;
    private ImageView stateDescriptionImage;
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy"+ "HH:mm", Locale.KOREA);
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
//        long sleepOnset = AppDatabase.sleepOnset;
//        String test = sdfDateTime.format(new Date(sleepOnset));
//        Log.v("tag_test", test);
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
        LinearLayout recommendClockView = v.findViewById(R.id.RecommendClockView);
        TextView noDataDescription = v.findViewById(R.id.noDataDescription);
        noDataDescription.setText(user_name + "님에게 딱 맞는 수면 패턴을 추천해 드릴게요");

        // Work Type View
        LinearLayout workTypeView = v.findViewById(R.id.WorkTypeView);

        //Check whether recommendation is hidden
        if (!sharedPref2.contains("isHidden")) {
            editor.putBoolean("isHidden", true).apply();
        }
        boolean isHidden = sharedPref2.getBoolean("isHidden", true);
        long K1 = sharedPref2.getLong("workOnset",now);
        long K2 = sharedPref2.getLong("workOffset",now);
        if (sharedPref2.contains("sleepOnset") && sharedPref2.contains("workOnset") && sharedPref2.contains("workOffset")) {
            if (K1 == K2) {
                noDataLayout.setVisibility(View.VISIBLE);
                infoView.setVisibility(View.GONE);
                workTypeView.setVisibility(View.GONE);
            } else {
                noDataLayout.setVisibility(View.GONE);
                if (!isHidden) {
                    infoView.setVisibility(View.VISIBLE);
                    recommendClockView.setVisibility(View.VISIBLE);
                    workTypeView.setVisibility(View.VISIBLE);
                } else {
                    infoView.setVisibility(View.VISIBLE);
                    recommendClockView.setVisibility(View.GONE);
                    workTypeView.setVisibility(View.VISIBLE);
                }
            }
        } else {
            infoView.setVisibility(View.GONE);
            recommendClockView.setVisibility(View.GONE);
            workTypeView.setVisibility(View.GONE);
        }
        recommendClockView.setVisibility(View.GONE);
        workTypeView.setVisibility(View.GONE);

        //Move to WhenSleepFragment
        addDataButton.setOnClickListener(view -> {
            WhenSleepFragment whenSleepFragment = new WhenSleepFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, whenSleepFragment).commit();
            mainActivity.setGoneBottomNavi();
        });

        sleepButton = v.findViewById(R.id.sleepButton);
        napButton = v.findViewById(R.id.napButton);
        workButton = v.findViewById(R.id.workButton);
        startTime = v.findViewById(R.id.StartTime);
        endTime = v.findViewById(R.id.EndTime);
        sleepTypeText = v.findViewById(R.id.sleepTypeText);
        sleepImportanceText = v.findViewById(R.id.sleepImportanceText);
        stateDescriptionText = v.findViewById(R.id.StateDescriptionText);
        stateDescriptionSmallText = v.findViewById(R.id.StateDescriptionSmallText);
        stateDescriptionImage = v.findViewById(R.id.StateDescriptionImage);
        ClockView clockView = v.findViewById(R.id.sweepingClockRecommend);
        TextView hopeTimeText = v.findViewById(R.id.HopeTimeDetail);
        TextView workTimeStart = v.findViewById(R.id.WorkTimeStart);
        TextView workTimeEnd = v.findViewById(R.id.WorkTimeEnd);
        TextView infoText = v.findViewById(R.id.InfoText);
        TextView clockTitleRecommend = v.findViewById(R.id.ClockTitleRecommend);
        ImageButton infoButton = v.findViewById(R.id.infoButton);

        hopeTimeText.setText(sleepOnsetDisplaying);
        workTimeStart.setVisibility(View.INVISIBLE);
        workTimeEnd.setText(workOnsetDisplaying + " - " + workOffsetDisplaying);

        //User name setting
        infoText.setText("일정");
        clockTitleRecommend.setText(user_name + "님을 위한 추천 수면");

        //Recommendation information
        ImageButton recommendInfoButton = v.findViewById(R.id.RecommendInfoButton);
        recommendInfoButton.setOnClickListener(view->{
            Intent infoIntent = new Intent(v.getContext(), InfoActivity.class);
            infoIntent.putExtra("Location",5);
            startActivity(infoIntent);
        });

        //Move to setting if infoButton is clicked
        infoButton.setOnClickListener(view -> {
            Intent sleepOnsetIntent = new Intent(v.getContext(), SleepOnsetActivity.class);
            startActivity(sleepOnsetIntent);
        });

        //Initial Button Setting
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        sleepButton.setOnClickListener(v1 -> sleepButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        napButton.setOnClickListener(v1 -> napButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        workButton.setOnClickListener(v1 -> workButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        sleepButton.performClick();
        clockView.setTypeOfInterval(1);
        clockView.setAngleFromTime(mainSleepStartString, mainSleepEndString);

        //startTime.setText(mainSleepStartString);
        //endTime.setText(mainSleepEndString);
        if (mainActivity.getMainSleepStart() == mainActivity.getMainSleepEnd()) {
            startTime.setText("--:--");
            endTime.setText("--:--");
        } else {
            startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepStart())));
            endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepEnd())));
        }

        // Work Type Selection
        TabLayout workTypeTabView = v.findViewById(R.id.workTypeTabView);
        View dimBackground = v.findViewById(R.id.dimBackground);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
        int workType = sharedPref2.getInt("workType", 0);
        Log.v("RecommendFragment", "Recorded work type: " + workType);
        workTypeTabView.addTab(workTypeTabView.newTab().setText("휴무"));
        workTypeTabView.addTab(workTypeTabView.newTab().setText("아침"));
        workTypeTabView.addTab(workTypeTabView.newTab().setText("저녁"));
        workTypeTabView.addTab(workTypeTabView.newTab().setText("야간"));
        int initialTab = Math.abs(workType);
        Objects.requireNonNull(workTypeTabView.getTabAt(initialTab)).select();
        final int[] selectedType = {workType};
        workTypeTabView.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0 -> {
                        selectedType[0] = 0;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                    case 1 -> {
                        selectedType[0] = -1;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                    case 2 -> {
                        selectedType[0] = -2;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                    case 3 -> {
                        selectedType[0] = -3;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0 -> {
                        selectedType[0] = 0;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                    case 1 -> {
                        selectedType[0] = -1;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                    case 2 -> {
                        selectedType[0] = -2;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                    case 3 -> {
                        selectedType[0] = -3;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0 -> {
                        selectedType[0] = 0;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                    case 1 -> {
                        selectedType[0] = -1;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                    case 2 -> {
                        selectedType[0] = -2;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                    case 3 -> {
                        selectedType[0] = -3;
                        Log.v("RecommendFragment", "Selected Work type: " + selectedType[0]);
                    }
                }
            }
        });

        Button workTypeSubmitButton = v.findViewById(R.id.workTypeSubmitButton);
        workTypeSubmitButton.setOnClickListener(typeV -> {
            // Update work type
            editor.putInt("workType", selectedType[0]).apply();
            Log.v("RecommendFragment", "Submit work type: " + selectedType[0]);

            String languageSetting = Locale.getDefault().getLanguage();
            if (languageSetting.equals("ko")) {
                showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                        "내일 근무 종류가 변경되었습니다.", "확인");
            } else {
                showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                        "Your work type for tomorrow has been updated.", "OK");
            }
        });

        return v;
    }

    public void sleepButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                                 ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                                 TextView sleepTypeText, TextView sleepImportanceText,
                                 TextView stateDescriptionText, TextView stateDescriptionSmallText,
                                 ImageView stateDescriptionImage, ClockView clockView)
    {
        Log.v("THE MAIN SLEEP STRING", mainSleepEndString);
        Log.v("THE MAIN SLEEP STRING", mainSleepStartString);
        //startTime.setText(mainSleepStartString);
        //endTime.setText(mainSleepEndString);
        stateDescriptionSmallText.setVisibility(View.VISIBLE);
        boolean isearly = mainActivity.getIsEarlySleep();
        boolean isenough = mainActivity.getIsEnoughSleep();
        //Change the color of buttons
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        napButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        workButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        //Change the content of displaying text
        if (mainSleepStartString.equals(mainSleepEndString)) {
            startTime.setText("--:--");
            endTime.setText("--:--");
            stateDescriptionText.setText("잠에 들기 어려운 시각이에요");
            stateDescriptionSmallText.setText("다른 취침 시각을 선택해보세요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sad, null));
        } else {
            startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepStart())));
            endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepEnd())));
            if (isenough) {
                if (isearly) {
                    stateDescriptionText.setText("선택하신 취침 시각이\n너무 일러요");
                    stateDescriptionSmallText.setText("조금 더 늦은 수면을 추천드려요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sad, null));
                } else {
                    stateDescriptionText.setText("잠을 자기 좋은 시간이에요");
                    stateDescriptionSmallText.setText("기상시간을 지켜주세요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sleep, null));
                }
            } else {
                if (isearly) {
                    stateDescriptionText.setText("잠이 충분하지 않아요");
                    stateDescriptionSmallText.setText("최대한 잠을 자도 피곤할 수 있어요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
                } else {
                    stateDescriptionText.setText("잠이 충분하지 않아요");
                    stateDescriptionSmallText.setText("희망 취침 시각을 앞으로 당겨보세요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
                }
            }
        }
        sleepTypeText.setText("밤잠");
        sleepImportanceText.setText("중요");
        sleepImportanceText.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.important_caption, null));

        //Change the clock angle using setAngle and color using setTypeOfInterval
        //Just example
        if(!mainSleepStartString.equals(mainSleepEndString)) {
            Log.v("FDLSJK", "DFSLJ");
            clockView.setIsRecommended(true);
            clockView.setTypeOfInterval(1);
            clockView.setAngleFromTime(mainSleepStartString, mainSleepEndString);
        }else{
            Log.v("GONE", "GONE");
            clockView.setTypeOfInterval(1);
            clockView.setIsRecommended(false);
        }
    }

    public void napButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                               ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                               TextView sleepTypeText, TextView sleepImportanceText,
                               TextView stateDescriptionText, TextView stateDescriptionSmallText,
                               ImageView stateDescriptionImage, ClockView clockView)
    {
        stateDescriptionSmallText.setVisibility(View.VISIBLE);
        startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getNapSleepStart())));
        endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getNapSleepEnd())));
        boolean isearly = mainActivity.getIsEarlySleep();
        boolean isenough = mainActivity.getIsEnoughSleep();
        //Change the color of buttons
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        napButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        workButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        //Change the content of displaying text
        sleepTypeText.setText("낮잠");
        sleepImportanceText.setText("권장");
        sleepImportanceText.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.recommend_caption, null));
        clockView.setTypeOfInterval(2);
        //Just example
        if(!napSleepStartString.equals(napSleepEndString)) {
            clockView.setIsRecommended(true);
            clockView.setAngleFromTime(napSleepStartString, napSleepEndString);
            stateDescriptionText.setText("낮잠을 주무세요");
            stateDescriptionSmallText.setText("이 시간에 충분히 자야해요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sleep, null));
        }else{
            clockView.setIsRecommended(false);
            if (isenough) {
                stateDescriptionText.setText("낮잠이 필요하지 않아요");
                stateDescriptionSmallText.setText("낮잠 없이도 맑은 정신을 유지할 수 있어요");
                stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.smile, null));
            } else {
                stateDescriptionText.setText("낮잠을 잘 수 없어요");
                stateDescriptionSmallText.setText("수면이 충분하지 않아요");
                stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
            }
            startTime.setText("--:--");
            endTime.setText("--:--");
        }
    }

    public void workButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                                ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                                TextView sleepTypeText, TextView sleepImportanceText,
                                TextView stateDescriptionText, TextView stateDescriptionSmallText,
                                ImageView stateDescriptionImage, ClockView clockView)
    {
        clockView.setIsRecommended(true);
        //startTime.setText(workOnsetString);
        //endTime.setText(workOffsetString);
        startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getWorkOnset())));
        endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getWorkOffset())));
        boolean isearly = mainActivity.getIsEarlySleep();
        boolean isenough = mainActivity.getIsEnoughSleep();
        //Change the color of buttons
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        napButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        workButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        //Change the content of displaying text
        sleepTypeText.setText("근무");
        sleepImportanceText.setText("중요");
        sleepImportanceText.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.important_caption, null));
        if (isenough) {
            stateDescriptionText.setText("근무 시간이에요");
            stateDescriptionSmallText.setText("맑은 정신을 유지할 수 있어요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.smile, null));
        } else {
            stateDescriptionText.setText("근무 시간이에요");
            stateDescriptionSmallText.setText("맑은 정신을 유지하기 어려워요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
        }
        clockView.setTypeOfInterval(3);
        //Just example
        clockView.setAngleFromTime(workOnsetString, workOffsetString);
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


    private void showAlertDialog(View dimBackground, ActionBar actionBar,
                                 BottomNavigationView bottomNavigationView, String title,
                                 String buttonText) {
        // Save the original color
        // Change this part if someone tries to change the primary color
        int originalActionBarColor = getResources().getColor(R.color.white, null);
        Window window = getActivity().getWindow();

        // Dim effect
        dimBackground.setVisibility(View.VISIBLE);
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dim, null)));
        }
        if (bottomNavigationView != null) {
            bottomNavigationView.setItemBackground(new ColorDrawable(Color.parseColor("#80000000")));
        }
        window.setStatusBarColor(getResources().getColor(R.color.dim, null));

        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.layout_custom_dialog,
                (LinearLayout) getActivity().findViewById(R.id.DialogLayout));
        TextView dialogTitle = (TextView) viewDialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = (TextView) viewDialog.findViewById(R.id.dialogMessage);
        Button dialogButton = (Button) viewDialog.findViewById(R.id.dialogButton);
        dialogTitle.setText(title);
        dialogMessage.setVisibility(View.GONE);
        dialogButton.setText(buttonText);

        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                .setView(viewDialog)
                .create();

        dialogButton.setOnClickListener(dialogV -> {
            dialog.dismiss();
            dimBackground.setVisibility(View.GONE);
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(originalActionBarColor));
            }
            if (bottomNavigationView != null) {
                bottomNavigationView.setItemBackground(new ColorDrawable(Color.parseColor("#FFFFFF")));
            }
            window.setStatusBarColor(getResources().getColor(R.color.white, null));
        });
        dialog.setCancelable(true);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }
}