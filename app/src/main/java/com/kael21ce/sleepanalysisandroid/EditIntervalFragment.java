package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class EditIntervalFragment extends Fragment implements ButtonTextUpdater {

    public Button startTimeEditButton;
    public Button endTimeEditButton;
    private String buttonStartText;
    private String buttonEndText;

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
            timePickerDialog.setData(true);
            timePickerDialog.show();
        });
        endTimeEditButton.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), editIntervalFragment);
            timePickerDialog.setData(false);
            timePickerDialog.show();
        });

        //Set the text of editIntervalText
        intervalTextView.setText("수면 시간: "
               + getInterval(v.getContext(), (String) startTimeEditButton.getText(), (String) endTimeEditButton.getText()));

        //Delete interval if deleteButton is clicked
        deleteButton.setOnClickListener(view -> {
            //Delete
        });

        //Edit interval if editButton is clicked
        editButton.setOnClickListener(view -> {

            //Edit
        });

        return v;
    }

    public void setDateButtonText(String text, Boolean isStartButton) {

    }

    public void setTimeButtonText(String text, Boolean isStartButton) {
        if (isStartButton) {
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