package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private Button recommendButton;
    private Button alert1TimeButton;
    private Button alert2TimeButton;
    private Button alert3TimeButton;
    private TimePickerDialog timePickerDialog;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    DateTimeFormatter dfComplexTime = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA);
    DateTimeFormatter dfComplexTime_En = DateTimeFormatter.ofPattern("h:mm a");
    DateTimeFormatter dfSimpleTime = DateTimeFormatter.ofPattern("H:mm", Locale.KOREA);
    DateTimeFormatter dfSimpleTime_En = DateTimeFormatter.ofPattern("H:mm");
    SimpleDateFormat sdfSimpleTime = new SimpleDateFormat("H:mm", Locale.getDefault());
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
        notifyBackButton.setOnClickListener(view -> finish());

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

        // Setup for alert dialog
        View dimBackground = findViewById(R.id.dimBackgroundNoti);
        ActionBar actionBar = getSupportActionBar();

        // Update the notification time
        Button notifySubmitButton = findViewById(R.id.notifySubmitButton);
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
            if (languageSetting.equals("ko")) {
                showAlertDialog(dimBackground, actionBar,"알림 시간이 업데이트되었습니다.", "확인");
            } else {
                showAlertDialog(dimBackground, actionBar,"Notification time is updated", "OK");
            }
        });
    }

    public String changeTimeFormatComplex(String value) {
        //Change time format of notifyAt
        String languageSetting = Locale.getDefault().getLanguage();
        LocalTime time;
        if (languageSetting.equals("ko")) {
            time = LocalTime.parse(value, dfSimpleTime);
            return time.format(dfComplexTime);
        } else {
            time = LocalTime.parse(value, dfSimpleTime_En);
            return time.format(dfComplexTime_En);
        }
    }

    public String changeTimeFormatSimple(String value) {
        //Change time format of notifyAt
        String languageSetting = Locale.getDefault().getLanguage();
        LocalTime time;
        if (languageSetting.equals("ko")) {
            time = LocalTime.parse(value, dfComplexTime);
            return time.format(dfSimpleTime);
        } else {
            time = LocalTime.parse(value, dfComplexTime_En);
            return time.format(dfSimpleTime_En);
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

    private void showAlertDialog(View dimBackground, ActionBar actionBar,
                                 String title, String buttonText) {
        // Save the original color
        // Change this part if someone tries to change the primary color
        int originalActionBarColor = getResources().getColor(R.color.white, null);
        Window window = getWindow();

        // Dim effect
        dimBackground.setVisibility(View.VISIBLE);
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dim, null)));
        }
        Intent dimOnIntent = new Intent("DIM_EFFECT");
        dimOnIntent.putExtra("COLOR", "#80000000");
        sendBroadcast(dimOnIntent);
        window.setStatusBarColor(getResources().getColor(R.color.dim, null));

        View viewDialog = LayoutInflater.from(this).inflate(R.layout.layout_custom_dialog,
                (LinearLayout) findViewById(R.id.DialogLayout));
        TextView dialogTitle = (TextView) viewDialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = (TextView) viewDialog.findViewById(R.id.dialogMessage);
        Button dialogButton = (Button) viewDialog.findViewById(R.id.dialogButton);
        dialogTitle.setText(title);
        dialogMessage.setVisibility(View.GONE);
        dialogButton.setText(buttonText);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(viewDialog)
                .create();

        dialogButton.setOnClickListener(dialogV -> {
            dialog.dismiss();
            dimBackground.setVisibility(View.GONE);
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(originalActionBarColor));
            }
            Intent dimOffIntent = new Intent("DIM_EFFECT");
            dimOffIntent.putExtra("COLOR", "#FFFFFF");
            sendBroadcast(dimOffIntent);

            window.setStatusBarColor(getResources().getColor(R.color.white, null));
        });
        dialog.setCancelable(true);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }
}
