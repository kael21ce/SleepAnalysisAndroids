package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.kael21ce.sleepanalysisandroid.data.Sleep;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
            sdf = new SimpleDateFormat("yyyy.MM.dd aaa hh:mm");
        } else {
            sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm aaa");
        }


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
        if (languageSetting.equals("ko")) {
            current_time = new SimpleDateFormat("aaa hh:mm").format(new Date(1000*60*60*15));
        } else {
            current_time = new SimpleDateFormat("hh:mm aaa").format(new Date(1000*60*60*15));
        }
        String current_date = new SimpleDateFormat("yyyy.MM.dd").format(ref.curDate);

        startDateButton.setText(current_date);
        endDateButton.setText(current_date);
        startTimeButton.setText(current_time);
        endTimeButton.setText(current_time);

        //Open Picker when buttons about date are clicked
        AddIntervalFragment addIntervalFragment = this;
        startDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(v.getContext(), addIntervalFragment);
            datePickerDialog.setData(1);
            datePickerDialog.show();
        });
        endDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(v.getContext(), addIntervalFragment);
            datePickerDialog.setData(0);
            datePickerDialog.show();
        });
        startTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(v.getContext(), addIntervalFragment);
            timePickerDialog.setData(1);
            if (languageSetting.equals("ko")) {
                timePickerDialog.setTimePicker("오전 00:00");
            } else {
                timePickerDialog.setTimePicker("00:00 AM");
            }
            timePickerDialog.show();
        });
        endTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(v.getContext(), addIntervalFragment);
            timePickerDialog.setData(0);
            if (languageSetting.equals("ko")) {
                timePickerDialog.setTimePicker("오전 00:00");
            } else {
                timePickerDialog.setTimePicker("00:00 AM");
            }
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
            if(sleepStartDate.getTime() <= sleepEndDate.getTime() && !mainActivity.isOverlap(mainActivity.getSleeps(), add_sleep, -1)) {
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
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                if (languageSetting.equals("ko")) {
                    builder.setTitle("경고");
                    builder.setMessage("잘못된 수면 입력값입니다");
                    builder.setNegativeButton("확인", (dialogInterface, i) -> dialogInterface.cancel());
                } else {
                    builder.setTitle("ERROR");
                    builder.setMessage("INVALID SLEEP VALUE");
                    builder.setNegativeButton("OK", (dialogInterface, i) -> dialogInterface.cancel());
                }

                AlertDialog alert = builder.create();
                alert.setOnShowListener(arg0 -> {
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
                });
                alert.show();
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
}