package com.kael21ce.sleepanalysisandroid;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;

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
        datePicker.setOnDateChangedListener((datePicker, year, month, dayOfMonth) -> {
            //Get date in format
            String format;
            //I don't know the reason why month get from listener has -1 from real month...
            format = year + "." + (month + 1) + "." + dayOfMonth;
            checkDateButton.setOnClickListener(view -> {
                //Send date to AddIntervalFragment
                buttonTextUpdater.setDateButtonText(format, isStartButton);
                Log.w("Date Text Setting", format);
                dismiss();
            });
        });


    }

    public void setDatePicker(String theDate){
        Log.v("CURRENT DATE", theDate);
        int year = Integer.parseInt(theDate.substring(0, 4));
        int month = Integer.parseInt(theDate.substring(5, 7));
        int day = Integer.parseInt(theDate.substring(8, 10));
        datePicker.updateDate(year, month, day);
    }

    //To get the data from AddIntervalFragment
    public void setData(int isStartButton) {
        this.isStartButton = isStartButton;
    }
}