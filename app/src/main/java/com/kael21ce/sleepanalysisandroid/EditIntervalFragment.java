package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kael21ce.sleepanalysisandroid.data.Sleep;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditIntervalFragment extends Fragment implements ButtonTextUpdater {

    public Button startTimeEditButton;
    public Button endTimeEditButton;
    private String buttonStartText;
    private String buttonEndText;
    private TextView intervalTextView;

    SimpleDateFormat sdfDateTime = new SimpleDateFormat( "yyyy/MM/dd HH:mm", Locale.KOREA);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm aaa");
    SimpleDateFormat sdfSimple = new SimpleDateFormat("yyyy/MM/dd");

    SimpleDateFormat sdf24H = new SimpleDateFormat("HH:mm");
    SimpleDateFormat sdfAMPM = new SimpleDateFormat("hh:mm a");

    private static final String TAG = "EditIntervalFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_interval, container, false);
        ImageButton backButtonEdit = v.findViewById(R.id.backButtonEdit);
        intervalTextView = v.findViewById(R.id.editIntervalText);
        Button deleteButton = v.findViewById(R.id.deleteButton);
        Button editButton = v.findViewById(R.id.editButton);
        startTimeEditButton = v.findViewById(R.id.startTimeEditButton);
        endTimeEditButton = v.findViewById(R.id.endTimeEditButton);

        MainActivity mainActivity = (MainActivity)getActivity();

        IntervalFragment intervalFragment = new IntervalFragment();
        //Back to intervalFragment if backButtonEdit is clicked
        Bundle bundle = this.getArguments();
        intervalFragment.setArguments(bundle.getBundle("bundle"));
        backButtonEdit.setOnClickListener(view -> getParentFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit());



        //Set the time picker to each timeEditButton
        EditIntervalFragment editIntervalFragment = this;

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("isSchedule", true);
        editor.apply();

        //get the bundle
        if(bundle == null){
            Log.v("bundle", "bundle failed to be fetched");
        }
        String date = bundle.getString("date");
        String startHour = bundle.getString("startHour");
        String endHour = bundle.getString("endHour");
        long startSleep = 0;
        long endSleep = 0;
        Date startHourD = new Date();
        Date endHourD = new Date();
        try {
            startSleep = sdfDateTime.parse(date + " " + startHour).getTime();
            endSleep = sdfDateTime.parse(date + " " + endHour).getTime();
            startHourD = sdf24H.parse(startHour);
            endHourD = sdf24H.parse(endHour);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Log.v("AM PM FORMAT", sdfAMPM.format(startHourD));
        Log.v("AM PM FORMAT", sdfAMPM.format(endHourD));

        startTimeEditButton.setText(sdfAMPM.format(startHourD));
        endTimeEditButton.setText(sdfAMPM.format(endHourD));
        //Set the text of editIntervalText
        try {
            intervalTextView.setText("수면 시간: "
                   + getInterval(sdfAMPM.format(startHourD), sdfAMPM.format(endHourD)));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Date finalStartHourD = startHourD;
        startTimeEditButton.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), editIntervalFragment);
            timePickerDialog.setData(1);
            timePickerDialog.setTimePicker((String) startTimeEditButton.getText());
            timePickerDialog.show();
        });
        endTimeEditButton.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), editIntervalFragment);
            timePickerDialog.setData(0);
            timePickerDialog.setTimePicker((String) endTimeEditButton.getText());
            timePickerDialog.show();
        });

        //Delete interval if deleteButton is clicked
        long finalStartSleep = startSleep;
        long finalEndSleep = endSleep;
        Sleep initSleep = new Sleep();
        initSleep.sleepStart = finalStartSleep;
        initSleep.sleepEnd = finalEndSleep;
        deleteButton.setOnClickListener(view -> {
            Log.v("DELETED", "DELETED");
            mainActivity.deleteSleep(initSleep);

            Intent scheduleIntent = new Intent(mainActivity, SplashActivity.class);

            //Send the information of the selected date
            Calendar calendar = Calendar.getInstance();
            try {
                Date curDate = sdfSimple.parse(date);
                calendar.setTime(curDate);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                Log.v(TAG, "Selected: " + year + "-" + month + 1 + "-" + day);

                scheduleIntent.putExtra("Year", year);
                scheduleIntent.putExtra("Month", month);
                scheduleIntent.putExtra("Day", day);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            startActivity(scheduleIntent);
        });

        //Edit interval if editButton is clicked
        editButton.setOnClickListener(view -> {
            Log.v("EDITED", "EDITED");
            Sleep edit_sleep = new Sleep();
            String startTime = (String) startTimeEditButton.getText();
            String endTime = (String) endTimeEditButton.getText();
            String startSDF = date + ' ' + startTime;
            String endSDF = date + ' ' + endTime;
            Log.v("START SDF", startSDF);
            Log.v("END SDF", endSDF);

            Date sleepStartDate = null;
            Date sleepEndDate = null;
            try {
                sleepStartDate = sdf.parse(startSDF);
                sleepEndDate = sdf.parse(endSDF);
                //translate to local date time
//                LocalDateTime ldt1 = LocalDateTime.ofInstant(sleepStartDate.toInstant(), ZoneId.systemDefault());
//                LocalDateTime ldt2 = LocalDateTime.ofInstant(sleepEndDate.toInstant(), ZoneId.systemDefault());
//                sleepStartDate = Date.from(ldt1.atZone(ZoneId.systemDefault()).toInstant());
//                sleepEndDate = Date.from(ldt2.atZone(ZoneId.systemDefault()).toInstant());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Log.v("START DATE", sleepStartDate.toString());
            Log.v("END DATE", sleepEndDate.toString());
            assert sleepStartDate != null;
            assert sleepEndDate != null;
            if(sleepStartDate.getTime() <= sleepEndDate.getTime()) {
                edit_sleep.sleepStart = sleepStartDate.getTime();
                edit_sleep.sleepEnd = sleepEndDate.getTime();

                mainActivity.editSleep(initSleep, edit_sleep);

                Intent scheduleIntent = new Intent(mainActivity, SplashActivity.class);

                //Send the information of the selected date
                Calendar calendar = Calendar.getInstance();
                try {
                    Date curDate = sdfSimple.parse(date);
                    calendar.setTime(curDate);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    Log.v(TAG, "Selected: " + year + "-" + month + 1 + "-" + day);

                    scheduleIntent.putExtra("Year", year);
                    scheduleIntent.putExtra("Month", month);
                    scheduleIntent.putExtra("Day", day);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                startActivity(scheduleIntent);
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle("ERROR");
                builder.setMessage("Start of the sleep has to be before the end of the sleep");

                builder.setNegativeButton("OK", (dialogInterface, i) -> dialogInterface.cancel());

                AlertDialog alert = builder.create();
                alert.setOnShowListener(arg0 -> {
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
                });
                alert.show();
            }
        });

        return v;
    }

    public void setDateButtonText(String text, int isStartButton) {

    }

    public void setTimeButtonText(String text, int isStartButton) {
        if (isStartButton==1) {
            if (startTimeEditButton != null && endTimeEditButton != null) {
                startTimeEditButton.setText(text);

                Date startHourD = new Date();
                Date endHourD = new Date();
                try {
                    startHourD = sdfAMPM.parse(text);
                    endHourD = sdfAMPM.parse(endTimeEditButton.getText().toString());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                try {
                    intervalTextView.setText("수면 시간: "
                            + getInterval(sdfAMPM.format(startHourD),
                            sdfAMPM.format(endHourD)));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (startTimeEditButton != null && endTimeEditButton != null) {
                endTimeEditButton.setText(text);

                Date startHourD = new Date();
                Date endHourD = new Date();

                try {
                    startHourD = sdfAMPM.parse(startTimeEditButton.getText().toString());
                    endHourD = sdfAMPM.parse(text);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                try {
                    intervalTextView.setText("수면 시간: "
                            + getInterval(sdfAMPM.format(startHourD),
                            sdfAMPM.format(endHourD)));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //Convert Format of "aaa HH:mm" to "HH:mm"
    public String convertAToFormat(String time) throws ParseException {
        Date date = sdfAMPM.parse(time);
        return sdf24H.format(date);
    }

    //Calculate the interval between two time in format of "HH:mm"
    public String getInterval(String start, String end) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        LocalTime startTime = LocalTime.parse(start, formatter);
        LocalTime endTime = LocalTime.parse(end, formatter);

        long differenceInMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        if (differenceInMinutes < 0) {
            differenceInMinutes = differenceInMinutes + 24*60;
        }

        long intervalHour = differenceInMinutes / 60;
        long intervalMinute = differenceInMinutes % 60;

        return intervalHour + "시간 " + intervalMinute + "분";
    }
}