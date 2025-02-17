package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotifyActivity extends AppCompatActivity implements ButtonTextUpdater{

    private static final String NotifyKey = "Notify_At";
    private static final String AlertKey1 = "alertTime1";
    private static final String AlertKey2 = "alertTime2";
    private static final String AlertKey3 = "alertTime3";
    private static final String WorkOnsetKey = "workOnset";
    private static final String WorkOffsetKey = "workOffset";
    private Button recommendButton, alert1TimeButton, alert2TimeButton, alert3TimeButton, notifySubmitButton;
    private TimePickerDialog timePickerDialog;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    SimpleDateFormat sdfComplexTime = new SimpleDateFormat( "a h:mm", Locale.KOREA);
    SimpleDateFormat sdfComplexTime_En = new SimpleDateFormat( "h:mm a");
    SimpleDateFormat sdfSimpleTime = new SimpleDateFormat("H:mm", Locale.KOREA);
    SimpleDateFormat sdfSimpleTime_En = new SimpleDateFormat("H:mm");
    String notifyResult, alert1Result, alert2Result, alert3Result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        NotifyActivity notifyActivity = this;

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton notifyBackButton = findViewById(R.id.NotifyBackButton);
        //Click back button
        notifyBackButton.setOnClickListener(view -> {
            finish();
        });

        // Recommendation notification
        if (!sharedPref.contains(NotifyKey)) {
            editor.putString(NotifyKey, "21:00").apply();
        }
        String notifyAt = sharedPref.getString(NotifyKey, "21:00");
        String notifyAt_complex = changeTimeFormatComplex(notifyAt);

        recommendButton = findViewById(R.id.recommendNotifyButton);
        recommendButton.setText(notifyAt_complex);
        recommendButton.setOnClickListener(recommendV -> {
            timePickerDialog = new TimePickerDialog(this, notifyActivity);
            timePickerDialog.setData(0);
            timePickerDialog.setTimePicker((String) recommendButton.getText());
            timePickerDialog.show();
        });

        // Alertness 1
        alert1TimeButton = findViewById(R.id.alert1TimeButton);
        String workOnsetFormat;
        if (!sharedPref.contains(AlertKey1)) {
            if (sharedPref.contains(WorkOnsetKey)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(sharedPref.getLong(WorkOnsetKey, System.currentTimeMillis()));
                Date date = calendar.getTime();
                workOnsetFormat = sdfSimpleTime.format(date);
                editor.putString(AlertKey1, workOnsetFormat).apply();
            } else {
                editor.putString(AlertKey1, "9:00").apply();
            }
        }
        String alert1At = sharedPref.getString(AlertKey1, "9:00");
        String alert1At_complex = changeTimeFormatComplex(alert1At);

        alert1TimeButton.setText(alert1At_complex);
        alert1TimeButton.setOnClickListener(alert1V -> {
            timePickerDialog = new TimePickerDialog(this, notifyActivity);
            timePickerDialog.setData(1);
            timePickerDialog.setTimePicker((String) alert1TimeButton.getText());
            timePickerDialog.show();
        });

        // Alertness 2
        alert2TimeButton = findViewById(R.id.alert2TimeButton);
        String workMiddleFormat;
        if (!sharedPref.contains(AlertKey2)) {
            if (sharedPref.contains(WorkOnsetKey) && sharedPref.contains(WorkOffsetKey)) {
                Calendar calendar = Calendar.getInstance();
                long workOnset, workOffset;
                workOnset = sharedPref.getLong(WorkOnsetKey, System.currentTimeMillis());
                workOffset = sharedPref.getLong(WorkOffsetKey, System.currentTimeMillis());
                calendar.setTimeInMillis((workOnset + workOffset)/2);
                Date date = calendar.getTime();
                workMiddleFormat = sdfSimpleTime.format(date);
                editor.putString(AlertKey2, workMiddleFormat).apply();
            } else {
                editor.putString(AlertKey2, "13:00").apply();
            }
        }
        String alert2At = sharedPref.getString(AlertKey2, "13:00");
        String alert2At_complex = changeTimeFormatComplex(alert2At);

        alert2TimeButton.setText(alert2At_complex);
        alert2TimeButton.setOnClickListener(alert2V -> {
            timePickerDialog = new TimePickerDialog(this, notifyActivity);
            timePickerDialog.setData(2);
            timePickerDialog.setTimePicker((String) alert2TimeButton.getText());
            timePickerDialog.show();
        });

        // Alertness 3
        alert3TimeButton = findViewById(R.id.alert3TimeButton);
        String workOffsetFormat;
        if (!sharedPref.contains(AlertKey3)) {
            if (sharedPref.contains(WorkOffsetKey)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(sharedPref.getLong(WorkOffsetKey, System.currentTimeMillis()));
                Date date = calendar.getTime();
                workOffsetFormat = sdfSimpleTime.format(date);
                editor.putString(AlertKey3, workOffsetFormat).apply();
            } else {
                editor.putString(AlertKey3, "18:00").apply();
            }
        }
        String alert3At = sharedPref.getString(AlertKey3, "18:00");
        String alert3At_complex = changeTimeFormatComplex(alert3At);

        alert3TimeButton.setText(alert3At_complex);
        alert3TimeButton.setOnClickListener(alert1V -> {
            timePickerDialog = new TimePickerDialog(this, notifyActivity);
            timePickerDialog.setData(3);
            timePickerDialog.setTimePicker((String) alert3TimeButton.getText());
            timePickerDialog.show();
        });

        // Update the notification time
        notifySubmitButton = findViewById(R.id.notifySubmitButton);
        notifySubmitButton.setOnClickListener(submitV -> {
            notifyResult = changeTimeFormatSimple(recommendButton.getText().toString());
            alert1Result = changeTimeFormatSimple(alert1TimeButton.getText().toString());
            alert2Result = changeTimeFormatSimple(alert2TimeButton.getText().toString());
            alert3Result = changeTimeFormatSimple(alert3TimeButton.getText().toString());

            editor.putString(NotifyKey, notifyResult);
            editor.putString(AlertKey1, alert1Result);
            editor.putString(AlertKey2, alert2Result);
            editor.putString(AlertKey3, alert3Result);
            editor.apply();

            String languageSetting = Locale.getDefault().getLanguage();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            if (languageSetting.equals("ko")) {
                builder.setMessage("알림 시간이 업데이트되었습니다.");

                builder.setNegativeButton("확인", (dialogInterface, i) -> dialogInterface.cancel());
            } else {
                builder.setMessage("Notification time is updated");

                builder.setNegativeButton("OK", (dialogInterface, i) -> dialogInterface.cancel());
            }
            AlertDialog alert = builder.create();
            alert.setOnShowListener(arg0 -> {
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
            });
            alert.show();
        });
    }

    public String changeTimeFormatComplex(String value) {
        //Change time format of notifyAt
        String languageSetting = Locale.getDefault().getLanguage();
        Date date;
        try {
            if (languageSetting == "en") {
                date = sdfSimpleTime_En.parse(value);
                return sdfComplexTime_En.format(date);
            } else {
                date = sdfSimpleTime.parse(value);
                return sdfComplexTime.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String changeTimeFormatSimple(String value) {
        //Change time format of notifyAt
        String languageSetting = Locale.getDefault().getLanguage();
        Date date;
        try {
            if (languageSetting == "en") {
                date = sdfComplexTime_En.parse(value);
                return sdfSimpleTime_En.format(date);
            } else {
                date = sdfComplexTime.parse(value);
                return sdfSimpleTime.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void setDateButtonText(String text, int isStartButton) {}

    @Override
    public void setTimeButtonText(String text, int isStartButton) {
        if (isStartButton==1) {
            if (alert1TimeButton != null) {
                alert1TimeButton.setText(text);
            }
        } else if(isStartButton == 2) {
            if (alert2TimeButton != null) {
                alert2TimeButton.setText(text);
            }
        } else if (isStartButton == 3) {
            if (alert3TimeButton != null) {
                alert3TimeButton.setText(text);
            }
        } else {
            if (recommendButton != null) {
                recommendButton.setText(text);
            }
        }
    }

    @Override
    public String getDateButtonText(int isStartButton) {
        return null;
    }
}
