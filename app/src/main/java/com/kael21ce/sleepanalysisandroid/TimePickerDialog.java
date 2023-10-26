package com.kael21ce.sleepanalysisandroid;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TimePicker;

import io.reactivex.annotations.NonNull;

public class TimePickerDialog extends Dialog {

    public ImageButton backPopTimeButton;
    public Button checkTimeButton;
    public TimePicker timePicker;
    public Boolean isStartButton;

    public TimePickerDialog(@NonNull Context context, ButtonTextUpdater buttonTextUpdater) {
        super(context);
        setContentView(R.layout.activity_time_picker);

        backPopTimeButton = findViewById(R.id.backPopTimeButton);
        checkTimeButton = findViewById(R.id.checkTimeButton);
        timePicker = findViewById(R.id.timePicker);

        //Finish activity if backPopTimeButton is clicked
        backPopTimeButton.setOnClickListener(view -> dismiss());

        //Send selected time to AddIntervalFragment when checkTimeButton is clicked
        timePicker.setOnTimeChangedListener((timePicker, hour, minute) -> {
            //Get time in format
            String format;
            String minuteStr;
            if (minute < 10) {
                minuteStr = "0" + minute;
            } else {
                minuteStr = String.valueOf(minute);
            }
            if (hour >= 12) {
                if (hour - 12 < 10) {
                    format = "PM " + "0" + (hour - 12) + ":" + minuteStr;
                } else {
                    format = "PM " + (hour - 12) + ":" + minuteStr;
                }
            } else {
                if (hour < 10) {
                    format = "AM " + "0" + hour + ":" + minuteStr;
                } else {
                    format = "AM " + hour + ":" + minuteStr;
                }
            }
            checkTimeButton.setOnClickListener(view -> {
                //Send time format to AddIntervalFragment
                buttonTextUpdater.setTimeButtonText(format, isStartButton);
                Log.w("Time Text Setting", format);
                dismiss();
            });
        });
    }

    //To set the data from AddIntervalFragment
    public void setData(Boolean isStartButton) {
        this.isStartButton = isStartButton;
    }
}
