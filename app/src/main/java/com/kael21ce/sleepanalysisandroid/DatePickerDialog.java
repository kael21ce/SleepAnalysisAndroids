package com.kael21ce.sleepanalysisandroid;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.annotations.NonNull;

public class DatePickerDialog extends Dialog {

    public ImageButton backPopButton;
    public Button checkDateButton;
    public DatePicker datePicker;
    public int isStartButton;

    public DatePickerDialog(@NonNull Context context, ButtonTextUpdater buttonTextUpdater) {
        super(context);
        setContentView(R.layout.activity_date_picker);

        backPopButton = findViewById(R.id.backPopButton);
        checkDateButton = findViewById(R.id.checkDateButton);
        datePicker = findViewById(R.id.datePicker);

        //Finish activity if backPopButton is clicked
        backPopButton.setOnClickListener(view -> dismiss());

        //Send selected date to AddIntervalFragment when checkDateButton is clicked
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String initialDateStr = buttonTextUpdater.getDateButtonText(isStartButton);
        Log.d("DatePickerDialog", initialDateStr);
        List<Integer> initialList = str2Date(initialDateStr);
        int initialYear = initialList.get(0);
        int initialMonth = initialList.get(1);
        int initialDay = initialList.get(2);

        //CheckDateButton without datePicker
        checkDateButton.setOnClickListener(v -> {
            Log.w("DatePickerDialog", "Without selection");
            dismiss();
        });

        //Select date via datePicker
        datePicker.init(initialYear, initialMonth, initialDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker v, int year, int month, int dayOfMonth) {
                //Get date in format
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                String format = sdf.format(calendar.getTime());
                checkDateButton.setOnClickListener(view -> {
                    //Send date to AddIntervalFragment
                    buttonTextUpdater.setDateButtonText(format, isStartButton);
                    Log.w("Date Text Setting", format);
                    dismiss();
                });
            }
        });
    }

    public void setDatePicker(String theDate){
        Log.v("CURRENT DATE", theDate);
        int year = Integer.parseInt(theDate.substring(0, 4));
        int month = Integer.parseInt(theDate.substring(5, 7));
        int day = Integer.parseInt(theDate.substring(8, 10));
        datePicker.updateDate(year, month - 1, day);
    }

    //To get the data from AddIntervalFragment
    public void setData(int isStartButton) {
        this.isStartButton = isStartButton;
    }

    //Convert string into year, month, and day
    public List<Integer> str2Date(String theDate) {
        List<Integer> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        int year = 0;
        int month = 1;
        int day = 0;
        try {
            Date date = sdf.parse(theDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dateList.add(0, year);
        dateList.add(1, month);
        dateList.add(2, day);

        return dateList;
    }
}