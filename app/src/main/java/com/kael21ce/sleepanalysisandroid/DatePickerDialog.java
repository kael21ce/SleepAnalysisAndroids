package com.kael21ce.sleepanalysisandroid;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        int initialYear = 0;
        int initialMonth = 1;
        int initialDay = 0;
        try {
            Date initialDate = sdf.parse(initialDateStr);
            Calendar initialCalendar = Calendar.getInstance();
            initialCalendar.setTime(initialDate);

            initialYear = initialCalendar.get(Calendar.YEAR);
            initialMonth = initialCalendar.get(Calendar.MONTH);
            initialDay = initialCalendar.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
}