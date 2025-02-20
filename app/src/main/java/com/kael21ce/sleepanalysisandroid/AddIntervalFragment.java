package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddIntervalFragment extends Fragment implements ButtonTextUpdater {

    private Button startDateButton, startTimeButton, endDateButton, endTimeButton;
    private IntervalFragment intervalFragment;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    SimpleDateFormat sdf;
    SimpleDateFormat sdfDateTimeSchedule = new SimpleDateFormat( "yyyy/MM/dd", Locale.KOREA);
    private static final String TAG = "AddIntervalFragment";
    private String languageSetting = Locale.getDefault().getLanguage();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_interval, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("isSchedule", true);
        editor.apply();

        TimeZone timeZone = TimeZone.getDefault();

        startDateButton = v.findViewById(R.id.startDateButton);
        startTimeButton = v.findViewById(R.id.startTimeButton);
        endDateButton = v.findViewById(R.id.endDateButton);
        endTimeButton = v.findViewById(R.id.endTimeButton);
        Button addButton = v.findViewById(R.id.addButton);

        //Return to IntervalFragment if backButton is clicked
        intervalFragment = new IntervalFragment();
        //get bundle and give it back
        Bundle bundle = this.getArguments();
        if(bundle == null){
            Log.v("bundle", "bundle failed to be fetched");
        }
        intervalFragment.setArguments(bundle);
        ImageButton backButton = v.findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getParentFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit());
        
        if (languageSetting.equals("ko")) {
            sdf = new SimpleDateFormat("yyyy.MM.dd a h:mm", Locale.KOREA);
        } else {
            sdf = new SimpleDateFormat("yyyy.MM.dd h:mm a", Locale.getDefault());
        }
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
        String current_time;
        SimpleDateFormat sdfCurrent, sdfDate;
        if (languageSetting.equals("ko")) {
            sdfCurrent = new SimpleDateFormat("a h:mm", Locale.KOREA);
        } else {
            sdfCurrent = new SimpleDateFormat("h:mm a", Locale.getDefault());
        }
        sdfCurrent.setTimeZone(timeZone);
        current_time = sdfCurrent.format(getMidnight());

        sdfDate = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        sdfDate.setTimeZone(timeZone);
        String current_date = sdfDate.format(ref.curDate);

        startDateButton.setText(current_date);
        endDateButton.setText(current_date);
        startTimeButton.setText(current_time);
        endTimeButton.setText(current_time);

        // String of noon time
        String noonInput = "0:0";
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("H:m", Locale.getDefault());
        DateTimeFormatter outputFormatter;
        if (languageSetting.equals("ko")) {
            outputFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.getDefault());
        } else {
            outputFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault());
        }
        LocalTime noonTime = LocalTime.parse(noonInput, inputFormatter);
        String noonOutput = noonTime.format(outputFormatter);
        Log.v("AddIntervalFragment", "Locale: " + Locale.getDefault());

        //Open Picker when buttons about date are clicked
        AddIntervalFragment addIntervalFragment = this;
        startDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(v.getContext(), addIntervalFragment);
            datePickerDialog.setData(1);
            datePickerDialog.setDatePicker((String) startDateButton.getText());
            datePickerDialog.show();
        });
        endDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(v.getContext(), addIntervalFragment);
            datePickerDialog.setData(0);
            datePickerDialog.setDatePicker((String) endDateButton.getText());
            datePickerDialog.show();
        });
        startTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(v.getContext(), addIntervalFragment);
            timePickerDialog.setData(1);
            timePickerDialog.setTimePicker(noonOutput);
            timePickerDialog.show();
        });
        endTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(v.getContext(), addIntervalFragment);
            timePickerDialog.setData(0);
            timePickerDialog.setTimePicker(noonOutput);
            timePickerDialog.show();
        });
        addButton.setOnClickListener(view -> {
            Sleep add_sleep = new Sleep();
            String startDate = (String) startDateButton.getText();
            String startTime = (String) startTimeButton.getText();
            String startSDF = startDate + ' ' + startTime;
            String endDate = (String) endDateButton.getText();
            String endTime = (String) endTimeButton.getText();
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

                if (languageSetting.equals("ko")) {
                    if (startTime.startsWith("오전") && startTime.contains("12:")) {
                        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        Log.v("AddIntervalFragment", "1");
                    }
                    if (endTime.startsWith("오전") && endTime.contains("12:")) {
                        endCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        Log.v("AddIntervalFragment", "2");
                    }
                } else {
                    if (startTime.endsWith("AM") && startTime.contains("12:")) {
                        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        Log.v("AddIntervalFragment", "3");
                    }
                    if (endTime.endsWith("AM") && endTime.contains("12:")) {
                        endCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        Log.v("AddIntervalFragment", "4");
                    }
                }

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
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(ref.curDate);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

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
        } else {
            if (endDateButton != null) {
                endDateButton.setText(text);
            }
        }
    }

    public void setTimeButtonText(String text, int isStartButton) {
        if (isStartButton==1) {
            if (startTimeButton != null) {
                startTimeButton.setText(text);
            }
        } else {
            if (endTimeButton != null) {
                endTimeButton.setText(text);
            }
        }
    }

    public String getDateButtonText(int isStartButton) {
        String nullString = "2024.01.01";
        if (isStartButton==1) {
            if (startDateButton != null) {
                return (String) startDateButton.getText();
            } else {
                return nullString;
            }
        } else {
            if (endDateButton != null) {
                return (String) endDateButton.getText();
            } else {
                return nullString;
            }
        }
    }

    public Date getMidnight() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
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
                (LinearLayout) getActivity().findViewById(R.id.DialogLayout));
        TextView dialogTitle = (TextView) viewDialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = (TextView) viewDialog.findViewById(R.id.dialogMessage);
        Button dialogButton = (Button) viewDialog.findViewById(R.id.dialogButton);
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