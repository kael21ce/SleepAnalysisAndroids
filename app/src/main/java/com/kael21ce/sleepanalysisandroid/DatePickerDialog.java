package com.kael21ce.sleepanalysisandroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;

import io.reactivex.annotations.NonNull;

public class DatePickerDialog extends Dialog {

    public ImageButton backPopButton;
    public Button checkDateButton;
    public DatePicker datePicker;

    public DatePickerDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.activity_date_picker);

        backPopButton = findViewById(R.id.backPopButton);
        checkDateButton = findViewById(R.id.checkDateButton);
        datePicker = findViewById(R.id.datePicker);

        //Finish activity if backPopButton is clicked
        backPopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        //Send selected date to AddIntervalFragment when checkDateButton is clicked
        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                //Get date in format
                checkDateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Send date to AddIntervalFragment
                        dismiss();
                    }
                });
            }
        });


    }
}