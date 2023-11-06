package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.Intent;
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
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditIntervalFragment extends Fragment implements ButtonTextUpdater {

    public Button startTimeEditButton;
    public Button endTimeEditButton;
    private String buttonStartText;
    private String buttonEndText;

    SimpleDateFormat sdfDateTime = new SimpleDateFormat( "yyyy/MM/dd HH:mm", Locale.KOREA);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm aaa");

    SimpleDateFormat sdf24H = new SimpleDateFormat("HH:mm");
    SimpleDateFormat sdfAMPM = new SimpleDateFormat("hh:mm aaa");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_interval, container, false);
        ImageButton backButtonEdit = v.findViewById(R.id.backButtonEdit);
        TextView intervalTextView = v.findViewById(R.id.editIntervalText);
        Button deleteButton = v.findViewById(R.id.deleteButton);
        Button editButton = v.findViewById(R.id.editButton);
        startTimeEditButton = v.findViewById(R.id.startTimeEditButton);
        endTimeEditButton = v.findViewById(R.id.endTimeEditButton);

        MainActivity mainActivity = (MainActivity)getActivity();

        IntervalFragment intervalFragment = new IntervalFragment();
        //Back to intervalFragment if backButtonEdit is clicked
        backButtonEdit.setOnClickListener(view -> getParentFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit());

        //Set the time picker to each timeEditButton
        EditIntervalFragment editIntervalFragment = this;
        startTimeEditButton.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), editIntervalFragment);
            timePickerDialog.setData(1);
            timePickerDialog.show();
        });
        endTimeEditButton.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), editIntervalFragment);
            timePickerDialog.setData(0);
            timePickerDialog.show();
        });

        //get the bundle
        Bundle bundle = this.getArguments();
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

        //Set the text of editIntervalText
        intervalTextView.setText("수면 시간: "
               + getInterval(v.getContext(), sdfAMPM.format(startHourD), sdfAMPM.format(endHourD)));

        //Delete interval if deleteButton is clicked
        long finalStartSleep = startSleep;
        long finalEndSleep = endSleep;
        Sleep initSleep = new Sleep();
        initSleep.sleepStart = finalStartSleep;
        initSleep.sleepEnd = finalEndSleep;
        deleteButton.setOnClickListener(view -> {
            Log.v("DELETED", "DELETED");
            mainActivity.deleteSleep(initSleep);
            startActivity(new Intent(mainActivity, SplashActivity.class));
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
            edit_sleep.sleepStart = sleepStartDate.getTime();
            edit_sleep.sleepEnd = sleepEndDate.getTime();

            mainActivity.editSleep(initSleep, edit_sleep);
            startActivity(new Intent(mainActivity, SplashActivity.class));
        });

        return v;
    }

    public void setDateButtonText(String text, int isStartButton) {

    }

    public void setTimeButtonText(String text, int isStartButton) {
        if (isStartButton==1) {
            if (startTimeEditButton != null) {
                startTimeEditButton.setText(text);
            }
        } else {
            if (endTimeEditButton != null) {
                endTimeEditButton.setText(text);
            }
        }
    }

    //Convert Format of "aaa HH:mm" to "HH:mm"
    public String convertAToFormat(String time) {
        int index = time.indexOf(" ");
        String aaa = time.substring(index + 1);
        String hrmm = time.substring(0, index);
        if (aaa.equals("AM")) {
            return hrmm;
        } else {
            int indexhr = hrmm.indexOf(":");
            String hr = hrmm.substring(0, indexhr);
            String convertedHr = hr;
            if (!hr.equals("12")) {
                convertedHr = String.valueOf(Integer.parseInt(hr) + 12);
            }
            return convertedHr + ":" + hrmm.substring(indexhr + 1);
        }
    }

    //Calculate the interval between two time in format of "HH:mm"
    public String getInterval(Context context, String start, String end) {
        ClockView clockView = new ClockView(context);
        String startTime = convertAToFormat(start);
        String endTime = convertAToFormat(end);
        List<Integer> startInts = clockView.convertTimeFormat(startTime);
        List<Integer> endInts = clockView.convertTimeFormat(endTime);
        String intervalHour;
        String intervalMinute;

        //Calculate interval
        if (startInts.get(1) > endInts.get(1)) {
            intervalMinute = String.valueOf(60 - (startInts.get(1) - endInts.get(1)));
            intervalHour = String.valueOf(endInts.get(0) - startInts.get(0) - 1);
        } else {
            intervalMinute = String.valueOf(endInts.get(1) - startInts.get(1));
            intervalHour = String.valueOf(endInts.get(0) - startInts.get(0));
        }

        return intervalHour + "시간 " + intervalMinute + "분";
    }
}