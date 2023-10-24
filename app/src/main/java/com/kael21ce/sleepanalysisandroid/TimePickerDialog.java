package com.kael21ce.sleepanalysisandroid;

import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TimePicker;

import io.reactivex.annotations.NonNull;

public class TimePickerDialog extends Dialog {

    public ImageButton backPopTimeButton;
    public Button checkTimeButton;
    public TimePicker timePicker;

    public TimePickerDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.activity_time_picker);

        backPopTimeButton = findViewById(R.id.backPopTimeButton);
        checkTimeButton = findViewById(R.id.checkTimeButton);
        timePicker = findViewById(R.id.timePicker);

        //Finish activity if backPopTimeButton is clicked
        backPopTimeButton.setOnClickListener(view -> dismiss());

        //Send selected time to AddIntervalFragment when checkTimeButton is clicked
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                //Get time in format
                checkTimeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Send time to AddIntervalFragment
                        dismiss();
                    }
                });
            }
        });
    }
}
