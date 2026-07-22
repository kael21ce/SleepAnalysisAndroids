package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NotifyActivity extends AppCompatActivity implements ButtonTextUpdater{

    private static final String NotifyKey = "Notify_At";
    private Button recommendButton;
    private TimePickerDialog timePickerDialog;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    DateTimeFormatter dfComplexTime = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA);
    DateTimeFormatter dfComplexTime_En = DateTimeFormatter.ofPattern("h:mm a");
    DateTimeFormatter dfSimpleTime = DateTimeFormatter.ofPattern("H:mm", Locale.KOREA);
    DateTimeFormatter dfSimpleTime_En = DateTimeFormatter.ofPattern("H:mm");
    String notifyResult;

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

        // Recommendation notification (추천 수면 확인)
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

        // Setup for alert dialog
        View dimBackground = findViewById(R.id.dimBackgroundNoti);
        ActionBar actionBar = getSupportActionBar();

        // Update the notification time
        Button notifySubmitButton = findViewById(R.id.notifySubmitButton);
        notifySubmitButton.setOnClickListener(submitV -> {
            notifyResult = changeTimeFormatSimple(recommendButton.getText().toString());
            editor.putString(NotifyKey, notifyResult);
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
        if (recommendButton != null) {
            recommendButton.setText(text);
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
