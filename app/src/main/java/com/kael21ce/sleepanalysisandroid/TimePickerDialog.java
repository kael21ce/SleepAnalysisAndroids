package com.kael21ce.sleepanalysisandroid;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

import io.reactivex.annotations.NonNull;

public class TimePickerDialog extends Dialog {

    public ImageButton backPopTimeButton;
    public Button checkTimeButton;
    public TimePicker timePicker;
    public int isStartButton;
    public String languageSetting;

    public TimePickerDialog(@NonNull Context context, ButtonTextUpdater buttonTextUpdater) {
        super(context);
        setContentView(R.layout.activity_time_picker);

        backPopTimeButton = findViewById(R.id.backPopTimeButton);
        checkTimeButton = findViewById(R.id.checkTimeButton);
        timePicker = findViewById(R.id.timePicker);

        languageSetting = Locale.getDefault().getLanguage();

        //Finish activity if backPopTimeButton is clicked
        backPopTimeButton.setOnClickListener(view -> dismiss());

        //Send selected time to AddIntervalFragment when checkTimeButton is clicked
        timePicker.setOnTimeChangedListener((timePicker, hour, minute) -> {
            //Get time in format
            String format;
            String input = hour + ":" + minute;
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("H:m", Locale.getDefault());
            DateTimeFormatter outputFormatter;
            if (languageSetting.equals("ko")) {
                outputFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.getDefault());
            } else {
                outputFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault());
            }
            LocalTime time = LocalTime.parse(input, inputFormatter);
            format = time.format(outputFormatter);
            checkTimeButton.setOnClickListener(view -> {
                //Send time format to AddIntervalFragment
                buttonTextUpdater.setTimeButtonText(format, isStartButton);
                Log.w("Time Text Setting", format);
                dismiss();
            });
        });
    }

    //To set the data from AddIntervalFragment
    public void setData(int isStartButton) {
        this.isStartButton = isStartButton;
    }

    public void setTimePicker(String current_time){
        DateTimeFormatter df;
        String languageSetting = Locale.getDefault().getLanguage();
        if (languageSetting.equals("ko")) {
            df = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA);
        } else {
            df = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault());
        }
        LocalTime time = LocalTime.parse(current_time, df);
        int hour = time.getHour();
        int minutes = time.getMinute();
        timePicker.setHour(hour);
        timePicker.setMinute(minutes);
    }
}
