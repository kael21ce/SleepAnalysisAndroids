package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.BackendAPI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class WhenWorkFragment extends Fragment implements WorkTypeAdapter.OnWorkTypeSelectedListener {
    String sleepOnsetTime, workOnsetTime, workOffsetTime;
    SimpleDateFormat inputSdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
    long now = System.currentTimeMillis();
    long oneDay = (1000*60*60*24);
    long sleepOnsetResult, sleepOnsetShowResult, workOnsetResult, workOffsetResult;
    long sleepOnsetType, workOnsetType, workOffsetType;
    int selectedWorkType = 1;
    Button whenWorkButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_when_work, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();

        //Hide action bar
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        //Set the user name
        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);

        //Back to RecommendFragment
        ImageButton sleepBackButton = v.findViewById(R.id.workBackButton);
        sleepBackButton.setOnClickListener(view -> {
            WhenSleepFragment whenSleepFragment = new WhenSleepFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, whenSleepFragment).commit();
        });

        //Work onset과 work type 선택
        RecyclerView whenWorkTypeRecyclerView = v.findViewById(R.id.whenWorkTypeRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(v.getContext(),
                LinearLayoutManager.VERTICAL, false);
        whenWorkTypeRecyclerView.setLayoutManager(layoutManager);
        WorkTypeAdapter whenWorkTypeAdapter = new WorkTypeAdapter(this);

        // WorkType 아이템 설정
        // 1. SharedPreference에 workType과 각 타입에 맞는 working time 설정
        if (!sharedPref.contains("workType")) {
            sharedPref.edit().putInt("workType", 0).apply(); // Default: 0 (휴무)
        }
        if (!sharedPref.contains("workOnset_1") || !sharedPref.contains("workOffset_1")) {
            sharedPref.edit().putString("workOnset_1", "09:00").apply();
            sharedPref.edit().putString("workOffset_1", "18:00").apply();
        }
        if (!sharedPref.contains("workOnset_2") || !sharedPref.contains("workOffset_2")) {
            sharedPref.edit().putString("workOnset_2", "13:00").apply();
            sharedPref.edit().putString("workOffset_2", "22:00").apply();
        }
        if (!sharedPref.contains("workOnset_3") || !sharedPref.contains("workOffset_3")) {
            sharedPref.edit().putString("workOnset_3", "22:00").apply();
            sharedPref.edit().putString("workOffset_3", "07:00").apply();
        }
        WorkType workTypeItem;
        Boolean isChosen;
        String workStart, workEnd;
        for (int i = 0; i < 4; i++) {
            isChosen = sharedPref.getInt("workType", 0) == i;
            if (i == 0) {
                workTypeItem = new WorkType(i, "-", "-", isChosen);
            } else {
                String key_onset_i = "workOnset_" + i;
                String key_offset_i = "workOffset_" + i;
                workStart = sharedPref.getString(key_onset_i, "00:00");
                workEnd = sharedPref.getString(key_offset_i, "00:00");
                workTypeItem = new WorkType(i, workStart, workEnd, isChosen);
            }
            whenWorkTypeAdapter.addItem(workTypeItem);
        }

        whenWorkTypeRecyclerView.setAdapter(whenWorkTypeAdapter);
        whenWorkButton = v.findViewById(R.id.whenWorkButton);
        //Initial setting
        whenWorkButton.setEnabled(false);
        whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));

        // WhenSleepFragment에서 sleepOnset 받아오기
        Bundle sleepBundle = getArguments();
        if (sleepBundle != null) {
            sleepOnsetTime = sleepBundle.getString("SleepOnset");
            Log.d("SleepOnset", sleepOnsetTime);
        }

        //Save work onset and offset to database
        whenWorkButton.setOnClickListener(view -> {
            // WorkType에 해당하는 workOnset, workOffset 가져오기
            String selectedOnsetKey = "workOnset_" + selectedWorkType;
            String selectedOffsetKey = "workOffset_" + selectedWorkType;
            workOnsetTime = sharedPref.getString(selectedOnsetKey, "00:00");
            workOffsetTime = sharedPref.getString(selectedOffsetKey, "00:00");
            Log.v("WhenWorkFragment", "SleepOnset: " + sleepOnsetTime + " /  WorkOnset: " + workOnsetTime);

            if (sleepOnsetTime.equals(workOnsetTime)) {
                Toast.makeText(v.getContext(), "취침 시간과 근무 시작 시간은 일치하면 안됩니다!",Toast.LENGTH_SHORT).show();
            } else {
                Date sleepOnsetInput, workOnsetInput, workOffsetInput;
                Calendar sleepOnsetCal, workOnsetCal, workOffsetCal;
                try {
                    sleepOnsetInput = inputSdfTime.parse(sleepOnsetTime);


                    workOnsetInput = inputSdfTime.parse(workOnsetTime);


                    workOffsetInput = inputSdfTime.parse(workOffsetTime);
                } catch (ParseException e) {
                    sleepOnsetInput = new Date();
                    workOnsetInput = new Date();
                    workOffsetInput = new Date();
                }
                sleepOnsetCal = Calendar.getInstance();
                Calendar timeCal = Calendar.getInstance();
                timeCal.setTime(sleepOnsetInput);
                sleepOnsetCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                sleepOnsetCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                sleepOnsetCal.set(Calendar.SECOND, 0);
                sleepOnsetCal.set(Calendar.MILLISECOND, 0);
                sleepOnsetType = sleepOnsetCal.getTimeInMillis();

                workOnsetCal = Calendar.getInstance();
                timeCal = Calendar.getInstance();
                timeCal.setTime(workOnsetInput);
                workOnsetCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                workOnsetCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                workOnsetCal.set(Calendar.SECOND, 0);
                workOnsetCal.set(Calendar.MILLISECOND, 0);
                workOnsetType = workOnsetCal.getTimeInMillis();

                timeCal = Calendar.getInstance();
                timeCal.setTime(workOffsetInput);
                workOffsetCal = Calendar.getInstance();
                workOffsetCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                workOffsetCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                workOffsetCal.set(Calendar.SECOND, 0);
                workOffsetCal.set(Calendar.MILLISECOND, 0);
                workOffsetType = workOffsetCal.getTimeInMillis();

                if (sleepOnsetType <= now) {
                    sleepOnsetType = sleepOnsetType + oneDay;
                }

                Long[] updateDates = updateOnsetDate(now, sleepOnsetType, sleepOnsetType,
                        workOnsetType, workOffsetType);
                sleepOnsetResult = updateDates[0];
                sleepOnsetShowResult = updateDates[1];
                workOnsetResult = updateDates[2];
                workOffsetResult = updateDates[3];

                // Setup for alert dialog
                View dimBackground = v.findViewById(R.id.dimBackgroundWhenWk);
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

                if(isValid(sleepOnsetResult, workOnsetResult, workOffsetResult)) {
                    mainActivity.setSleepOnset(sleepOnsetResult);
                    mainActivity.setWorkOnset(workOnsetResult);
                    mainActivity.setWorkOffset(workOffsetResult);
                    sharedPref.edit().putInt("workType", selectedWorkType).apply();
                    sharedPref.edit().putLong("sleepOnsetShow", sleepOnsetShowResult).apply();

                    Log.v("SplashActivity", "Onset: " + sleepOnsetResult + " / Onset Show: " + sleepOnsetShowResult +
                            " / Work onset: " + workOnsetResult + " / Work offset: " + workOffsetResult);
                    Locale currentLocale = Locale.getDefault();
                    String language = currentLocale.getLanguage();

                    BackendAPI.sendSurvey(v.getContext(), sleepOnsetResult, workOnsetResult, workOffsetResult, selectedWorkType, new BackendAPI.SurveyCallback() {
                        @Override
                        public void onSuccess() {
                            if (language.equals("ko")) {
//                              Toast.makeText(getActivity(), "데이터가 전송되었습니다", Toast.LENGTH_SHORT).show();
                            } else {
//                              Toast.makeText(getActivity(), "Data added to API", Toast.LENGTH_SHORT).show();
                            }
                            mainActivity.finish();
                            startActivity(new Intent(mainActivity, SplashActivity.class));
                        }

                        @Override
                        public void onFailure(String errorMsg) {
                            if (language.equals("ko")) {
//                        Toast.makeText(getActivity(), "데이터 전송에 실패했습니다", Toast.LENGTH_SHORT).show();
                            } else {
//                        Toast.makeText(getActivity(), "Data sending failed", Toast.LENGTH_SHORT).show();
                            }
                            mainActivity.finish();
                            startActivity(new Intent(mainActivity, SplashActivity.class));
                        }
                    });
                }else{
                    String languageSetting = Locale.getDefault().getLanguage();
                    if (languageSetting.equals("ko")) {
                        showAlertDialog(dimBackground, actionBar, bottomNavigationView, "경고", "잘못된 입력값입니다.", "확인");
                    } else {
                        showAlertDialog(dimBackground, actionBar, bottomNavigationView, "Error", "Invalid input", "OK");
                    }
                }
            }

        });
        return v;
    }

    @Override
    public void onWorkTypeSelected(WorkType item) {
        // 어댑터에서 선택된 WorkType을 받기
        selectedWorkType = item.getWorkType();
        Log.d("WhenWorkFragment", "선택된 work Type: " + selectedWorkType);

        // 버튼을 활성화
        if (whenWorkButton != null) {
            whenWorkButton.setEnabled(true);
            whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
        }
    }

    public boolean isValid(long sleepOnset1, long workOnset1, long workOffset1) {
        if(sleepOnset1 <= workOnset1 && workOnset1 <= workOffset1){
            if(workOnset1 - sleepOnset1 <= 1000*60*60*24 && workOffset1 - workOnset1 <= 1000*60*60*24){
                return true;
            }
        }
        return false;
    }

    //Check whether today is the weekend
    public boolean isWeekend() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
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
                                 String message, String buttonText) {
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
                getActivity().findViewById(R.id.DialogLayout));
        TextView dialogTitle = viewDialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = viewDialog.findViewById(R.id.dialogMessage);
        Button dialogButton = viewDialog.findViewById(R.id.dialogButton);
        dialogTitle.setText(title);
        dialogMessage.setText(message);
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