package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.kael21ce.sleepanalysisandroid.databinding.FragmentEditIntervalBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import nl.joery.timerangepicker.TimeRangePicker;

public class EditIntervalFragment extends Fragment implements ButtonTextUpdater {

    SimpleDateFormat sdf;
    SimpleDateFormat sdfSimple = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
    SimpleDateFormat sdfBundle = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
    SimpleDateFormat sdfAMPM;

    private static final String TAG = "EditIntervalFragment";
    private static final long oneDay = 1000*60*60*24;
    private final String languageSetting = Locale.getDefault().getLanguage();
    Sleep targetSleep;
    FragmentEditIntervalBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditIntervalBinding.inflate(inflater, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();

        // Setup for alert dialog
        View dimBackground = getParentFragment() != null ? getParentFragment().getView().findViewById(R.id.dimBackgroundSched) : binding.dimBackgroundEditIntv;
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

        TimeZone timeZone = TimeZone.getDefault();
        sdfSimple.setTimeZone(timeZone);
        sdfBundle.setTimeZone(timeZone);

        // backButton이 눌렸을 때, scheduleFragment로 이동
        ScheduleFragment scheduleFragment = new ScheduleFragment();
        Bundle bundle = this.getArguments();
        assert bundle != null;
        // scheduleFragment.setArguments(bundle.getBundle("bundle"));
        binding.backButtonEdit.setOnClickListener(view -> getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.mainFrame, scheduleFragment).commit());

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("isSchedule", true);
        editor.apply();

        // Sleep data의 sleep_id를 받아오기 -> sleeps에서 ID와 일치하는 수면 가져오기
        long sleep_id = bundle.getLong("sleep_id");
        assert mainActivity != null;
        List<Sleep> sleeps = mainActivity.getSleeps();
        targetSleep = sleeps.get(0);
        for (Sleep sleep : sleeps) {
            if (sleep.sleep_id == sleep_id) {
                targetSleep = sleep;
                break;
            }
        }

        // Target sleep의 날짜 계산
        long targetStart = targetSleep.sleepStart;
        Date targetDate = new Date(targetStart);
        String targetDateStr = sdfSimple.format(targetDate);

        String date = bundle.getString("date");

        sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);
        sdfAMPM = new SimpleDateFormat("HH:mm", Locale.KOREA);
        sdf.setTimeZone(timeZone);
        sdfAMPM.setTimeZone(timeZone);

        // 기본 날짜 설정
        binding.startDateButton.setText(targetDateStr);

        // Date button이 클릭될 때 DatePickerDialog를 띄우기
        EditIntervalFragment editIntervalFragment = this;
        binding.startDateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(binding.getRoot().getContext(), editIntervalFragment);
            datePickerDialog.setData(1);
            datePickerDialog.setDatePicker((String) binding.startDateButton.getText());
            datePickerDialog.show();
        });


        // TimeRangePicker 초기 설정
        Calendar initialCalendar = Calendar.getInstance();
        // 시작 시간
        initialCalendar.setTimeInMillis(targetSleep.sleepStart);
        int startHourInt = initialCalendar.get(Calendar.HOUR_OF_DAY);
        int startMinuteInt = initialCalendar.get(Calendar.MINUTE);
        binding.EditTimePicker.setEndTimeMinutes(startHourInt * 60 + startMinuteInt);
        // 끝 시간
        initialCalendar = Calendar.getInstance();
        initialCalendar.setTimeInMillis(targetSleep.sleepEnd);
        int endHourInt = initialCalendar.get(Calendar.HOUR_OF_DAY);
        int endMinuteInt = initialCalendar.get(Calendar.MINUTE);
        binding.EditTimePicker.setStartTimeMinutes(endHourInt * 60 + endMinuteInt);
        final String[] startSleepTime = {AddIntervalFragment.time2String(startHourInt * 60 + startMinuteInt)};
        final String[] endSleepTime = {AddIntervalFragment.time2String(endHourInt * 60 + endMinuteInt)};
        binding.editRangeText.setText(startSleepTime[0] + " → " + endSleepTime[0]);

        // editIntervalText에 들어갈 시간 계산
        Date startHourD = new Date(targetSleep.sleepStart);
        Date endHourD = new Date(targetSleep.sleepEnd);
        binding.editIntervalText.setText("수면 시간: "
                + getInterval(sdfAMPM.format(startHourD), sdfAMPM.format(endHourD)));

        // TimeRangePicker에 대한 설정: Start thumb가 offset, end thumb가 onset
        binding.EditTimePicker.setOnTimeChangeListener(new TimeRangePicker.OnTimeChangeListener() {
            @Override
            public void onStartTimeChange(@NonNull TimeRangePicker.Time time) {
                endSleepTime[0] = AddIntervalFragment.time2String(time.getTotalMinutes());
                String results = startSleepTime[0] + " → " + endSleepTime[0];
                binding.editRangeText.setText(results);

                // 시간 간격 계산
                String resultInterval = getInterval(startSleepTime[0], endSleepTime[0]);
                binding.editIntervalText.setText("수면 시간: " + resultInterval);
            }

            @Override
            public void onEndTimeChange(@NonNull TimeRangePicker.Time time) {
                startSleepTime[0] = AddIntervalFragment.time2String(time.getTotalMinutes());
                String results = startSleepTime[0] + " → " + endSleepTime[0];
                binding.editRangeText.setText(results);

                // 시간 간격 계산
                String resultInterval = getInterval(startSleepTime[0], endSleepTime[0]);
                binding.editIntervalText.setText("수면 시간: " + resultInterval);
            }

            @Override
            public void onDurationChange(@NonNull TimeRangePicker.TimeDuration timeDuration) {

            }
        });


        //Delete interval if deleteButton is clicked
        binding.deleteButton.setOnClickListener(view -> {
            Log.v("DELETED", "DELETED");
            mainActivity.deleteSleep(targetSleep);

            Intent scheduleIntent = new Intent(mainActivity, SplashActivity.class);

            //Send the information of the selected date
            Calendar calendar = Calendar.getInstance();
            try {
                Date curDate = sdfBundle.parse(date);
                calendar.setTime(curDate);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                Log.v(TAG, "Selected: " + year + "-" + (month + 1) + "-" + day);

                scheduleIntent.putExtra("Year", year);
                scheduleIntent.putExtra("Month", month);
                scheduleIntent.putExtra("Day", day);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            startActivity(scheduleIntent);
        });

        //Edit interval if editButton is clicked
        binding.editButton.setOnClickListener(view -> {
            Log.v("EDITED", "EDITED");
            Sleep edit_sleep = new Sleep();
            String startTime = startSleepTime[0];
            String endTime = endSleepTime[0];

            String endDate = AddIntervalFragment.calculateSleepEndDate(targetDateStr, startTime, endTime);

            String startSDF = targetDateStr + ' ' + startTime;
            String endSDF = endDate + ' ' + endTime;
            Log.v("START SDF", startSDF);
            Log.v("END SDF", endSDF);

            Date sleepStartDate = null;
            Date sleepEndDate = null;
            try {
                sleepStartDate = sdf.parse(startSDF);
                sleepEndDate = sdf.parse(endSDF);
                Log.v("START DATE", sleepStartDate.toString());
                Log.v("END DATE", sleepEndDate.toString());
            } catch (ParseException e) {
                Log.e("EditIntervalFragment", e.toString());

                // Display alert message
                if (languageSetting.equals("ko")) {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "시스템 에러",
                            "앱을 잠시 후 다시 시작해주세요. 문제가 계속되면 개발자에게 연락바랍니다.", "확인");
                } else {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "System Error",
                            "Please restart the app after a moment. If the problem persists, please contact the developer.", "OK");
                }
            }

            Date curDate = new Date();
            try {
                curDate = sdfBundle.parse(date);
            } catch (ParseException e) {
                Log.e("EditIntervalFragment", e.toString());

                // Display alert message
                if (languageSetting.equals("ko")) {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "시스템 에러",
                            "앱을 잠시 후 다시 시작해주세요. 문제가 계속되면 개발자에게 연락바랍니다.", "확인");
                } else {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "System Error",
                            "Please restart the app after a moment. If the problem persists, please contact the developer.", "OK");
                }
            }

            long sleepStartMillis = sleepStartDate.getTime();
            long sleepEndMillis = sleepEndDate.getTime();

            if(sleepStartMillis <= sleepEndMillis) {
                edit_sleep.sleepStart = sleepStartMillis;
                edit_sleep.sleepEnd = sleepEndMillis;

                Intent scheduleIntent = new Intent(mainActivity, SplashActivity.class);

                //Send the information of the selected date
                Calendar calendar = Calendar.getInstance();
                int year, month, day;
                calendar.setTime(curDate);
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);

                Log.v(TAG, "Selected: " + year + "-" + (month + 1) + "-" + day);

                scheduleIntent.putExtra("Year", year);
                scheduleIntent.putExtra("Month", month);
                scheduleIntent.putExtra("Day", day);
                mainActivity.editSleep(targetSleep, edit_sleep);

                startActivity(scheduleIntent);
            }else{
                if (languageSetting.equals("ko")) {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "오류",
                            "수면 시작 시각이 종료 시각보다 일러야 합니다.", "확인");
                } else {
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView,
                            "ERROR",
                            "Start of the sleep has to be before the end of the sleep", "OK");
                }
            }
        });

        return binding.getRoot();
    }

    public void setDateButtonText(String text, int isStartButton) {
        if (isStartButton == 1) {
            binding.startDateButton.setText(text);
        }
    }

    public void setTimeButtonText(String text, int isStartButton) {
    }

    public String getDateButtonText(int isStartButton) {
        String nullString = "2024.01.01";
        if (isStartButton == 1) {
            return (String) binding.startDateButton.getText();
        }
        return nullString;
    }

    // "HH:mm" 형태의 시간을 받아서 시간 간격을 계산
    public String getInterval(String start, String end) {
        DateTimeFormatter formatter;
        formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA);

        try {
            LocalTime startTime = LocalTime.parse(start, formatter);
            LocalTime endTime = LocalTime.parse(end, formatter);
            long differenceInMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
            if (differenceInMinutes < 0) {
                differenceInMinutes = differenceInMinutes + 24*60;
            }

            long intervalHour = differenceInMinutes / 60;
            long intervalMinute = differenceInMinutes % 60;

            return intervalHour + "시간 " + intervalMinute + "분";
        } catch (Exception e) {
            Log.e("EditIntervalFragment", e.toString());
            return "0분";
        }
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

    private void showAlertDialogNoDim(String title, String message, String buttonText) {
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

        dialogButton.setOnClickListener(dialogV -> dialog.dismiss());
        dialog.setCancelable(true);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }
}