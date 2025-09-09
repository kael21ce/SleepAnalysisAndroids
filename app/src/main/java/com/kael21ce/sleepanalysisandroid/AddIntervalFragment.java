package com.kael21ce.sleepanalysisandroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.Sleep;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import nl.joery.timerangepicker.TimeRangePicker;

public class AddIntervalFragment extends Fragment implements ButtonTextUpdater {

    private Button startDateButton;
    private IntervalFragment intervalFragment;
    private ScheduleFragment scheduleFragment;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private TimeRangePicker sleepTimePicker;
    private TextView sleepRangeText;
    SimpleDateFormat sdf;
    SimpleDateFormat sdfDateTimeSchedule = new SimpleDateFormat( "yyyy/MM/dd", Locale.KOREA);
    private static final String TAG = "AddIntervalFragment";
    private final String languageSetting = Locale.getDefault().getLanguage();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Actionbar 숨기기
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_interval, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("isSchedule", true);
        editor.apply();

        TimeZone timeZone = TimeZone.getDefault();

        startDateButton = v.findViewById(R.id.startDateButton);
        Button addButton = v.findViewById(R.id.addButton);

        // backButton이 눌렸을 때, scheduleFragment로 이동
        Bundle bundle = this.getArguments();
        scheduleFragment = new ScheduleFragment();
        ImageButton backButton = v.findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.mainFrame, scheduleFragment).commit());

        sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);
        sdf.setTimeZone(timeZone);


        //Set the initial added time to current time
        var ref = new Object() {
            Date curDate = null;
        };
        try {
            ref.curDate = sdfDateTimeSchedule.parse(bundle.getString("date"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        sdfDate.setTimeZone(timeZone);
        String current_date = sdfDate.format(ref.curDate);

        // 기본 날짜 및 시간 설정
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        // 설정할 시간
        final String[] sleepOnsetTime = {time2String(currentHour * 60 + currentMinute)};
        final String[] sleepOffsetTime = {time2String(currentHour * 60 + currentMinute)};
        startDateButton.setText(current_date);
        sleepTimePicker = v.findViewById(R.id.SleepTimePicker);
        sleepTimePicker.setStartTimeMinutes(currentHour * 60 + currentMinute);
        sleepTimePicker.setEndTimeMinutes(currentHour * 60 + currentMinute);
        sleepRangeText = v.findViewById(R.id.sleepRangeText);
        sleepRangeText.setText(sleepOnsetTime[0] + " → " + sleepOffsetTime[0]);


        // Date button이 클릭될 때 DatePickerDialog를 띄우기
        AddIntervalFragment addIntervalFragment = this;
        startDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(v.getContext(), addIntervalFragment);
            datePickerDialog.setData(1);
            datePickerDialog.setDatePicker((String) startDateButton.getText());
            datePickerDialog.show();
        });

        // TimeRangePicker에 대한 설정: Start thumb가 offset, end thumb가 onset
        sleepTimePicker.setOnTimeChangeListener(new TimeRangePicker.OnTimeChangeListener() {
            @Override
            public void onStartTimeChange(@NonNull TimeRangePicker.Time time) {
                sleepOffsetTime[0] = time2String(time.getTotalMinutes());
                String results = sleepOnsetTime[0] + " → " + sleepOffsetTime[0];
                sleepRangeText.setText(results);
            }

            @Override
            public void onEndTimeChange(@NonNull TimeRangePicker.Time time) {
                sleepOnsetTime[0] = time2String(time.getTotalMinutes());
                String results = sleepOnsetTime[0] + " → " + sleepOffsetTime[0];
                sleepRangeText.setText(results);
            }

            @Override
            public void onDurationChange(@NonNull TimeRangePicker.TimeDuration timeDuration) {

            }
        });

        addButton.setOnClickListener(view -> {
            Sleep add_sleep = new Sleep();
            String startDate = (String) startDateButton.getText();
            String startTime = sleepOnsetTime[0];
            String startSDF = startDate + ' ' + startTime;
            String endTime = sleepOffsetTime[0];

            // startTime과 endTime을 기반으로 endDate를 계산
            String endDate = calculateSleepEndDate(startDate, startTime, endTime);
            String endSDF = endDate + ' ' + endTime;
            Log.v("START SDF", startSDF);
            Log.v("END SDF", endSDF);
            Date sleepStartDate;
            Date sleepEndDate;
            try {
                sleepStartDate = sdf.parse(startSDF);
                sleepEndDate = sdf.parse(endSDF);

                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(sleepStartDate);
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(sleepEndDate);

                //translate to local date time
                LocalDateTime ldt1 = LocalDateTime.ofInstant(sleepStartDate.toInstant(), ZoneId.systemDefault());
                LocalDateTime ldt2 = LocalDateTime.ofInstant(sleepEndDate.toInstant(), ZoneId.systemDefault());
                sleepStartDate = Date.from(ldt1.atZone(ZoneId.systemDefault()).toInstant());
                sleepEndDate = Date.from(ldt2.atZone(ZoneId.systemDefault()).toInstant());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Log.v("START DATE", sleepStartDate.toString());
            Log.v("END DATE", sleepEndDate.toString());
            assert sleepStartDate != null;
            assert sleepEndDate != null;
            add_sleep.sleepStart = sleepStartDate.getTime();
            add_sleep.sleepEnd = sleepEndDate.getTime();

            View dimBackground = getParentFragment() != null ? getParentFragment().getView().findViewById(R.id.dimBackgroundSched) : v.findViewById(R.id.dimBackgroundAddIntv);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

            if(sleepStartDate.getTime() < sleepEndDate.getTime() && !mainActivity.isOverlap(mainActivity.getSleeps(), add_sleep, -1)) {
                if (Math.abs(sleepStartDate.getTime()-sleepEndDate.getTime()) > 24*60*60*1000) {
                    if (languageSetting.equals("ko")) {
                        showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                                "경고", "24시간 미만으로 수면을 입력해주세요", "확인");
                    } else {
                        showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                                "ERROR", "Please enter sleep time of less than 24 hours", "OK");
                    }
                } else {

                    mainActivity.addSleep(add_sleep);

                    mainActivity.finish();
                    Intent scheduleIntent = new Intent(mainActivity, SplashActivity.class);

                    //Send the information of the selected date
                    Calendar intentCalendar = Calendar.getInstance();
                    intentCalendar.setTime(ref.curDate);
                    int year = intentCalendar.get(Calendar.YEAR);
                    int month = intentCalendar.get(Calendar.MONTH);
                    int day = intentCalendar.get(Calendar.DAY_OF_MONTH);

                    Log.v(TAG, "Selected: " + year + "-" + (month + 1) + "-" + day);

                    scheduleIntent.putExtra("Year", year);
                    scheduleIntent.putExtra("Month", month);
                    scheduleIntent.putExtra("Day", day);

                    startActivity(scheduleIntent);
                }
            }else{
                if (languageSetting.equals("ko")) {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "경고", "잘못된 수면 입력값입니다", "확인");
                } else {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "ERROR", "Invalid sleep value", "OK");
                }
            }
        });

        return  v;
    }

    //Change the text of Button
    public void setDateButtonText(String text, int isStartButton) {
        if (isStartButton==1) {
            if (startDateButton != null) {
                startDateButton.setText(text);
            }
        }
    }

    public void setTimeButtonText(String text, int isStartButton) {
    }

    public String getDateButtonText(int isStartButton) {
        String nullString = "2024.01.01";
        if (isStartButton==1) {
            if (startDateButton != null) {
                return (String) startDateButton.getText();
            } else {
                return nullString;
            }
        }
        return nullString;
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

    // Change minutes from TimeRangePicker to String HH:MM
    public String time2String(int minutes) {
        int hour = minutes / 60;
        int minute = minutes % 60;
        @SuppressLint("DefaultLocale") String hourStr = String.format("%02d", hour);
        @SuppressLint("DefaultLocale") String minuteStr = String.format("%02d", minute);
        return hourStr + ":" + minuteStr;
    }

    // 수면 시작 날짜, 수면 시작 시간, 수면 종료 시간이 String으로 주어져있을 때, 수면 종료 날짜를 String으로 출력
    private static String calculateSleepEndDate(String startDateString, String startTimeString, String endTimeString) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
            LocalDate startDate = LocalDate.parse(startDateString, dateFormatter);
            LocalTime startTime = LocalTime.parse(startTimeString, timeFormatter);
            LocalTime endTime = LocalTime.parse(endTimeString, timeFormatter);

            LocalDate endDate;

            // 종료 시간이 시작 시간보다 이전이거나 같으면, 날짜가 바뀐 것으로 간주
            // 예: 시작 23:00, 종료 07:00 -> 다음날
            // 예: 시작 01:00, 종료 08:00 -> 같은날
            if (!endTime.isAfter(startTime)) {
                endDate = startDate.plusDays(1);
            } else {
                endDate = startDate;
            }

            return endDate.format(dateFormatter);

        } catch (DateTimeParseException e) {
            System.err.println("입력된 날짜 또는 시간의 형식이 잘못되었습니다. (yyyy.MM.dd, HH:mm)");
            return startDateString;
        }
    }
}