package com.kael21ce.sleepanalysisandroid;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SettingFragment extends Fragment implements ButtonTextUpdater {

    Boolean isFolded = true;
    private static final String NotifyKey = "Notify_At";
    Button notifyButton;
    SimpleDateFormat sdfComplexTime = new SimpleDateFormat( "a hh:mm", Locale.KOREA);
    SimpleDateFormat sdfComplexTime_En = new SimpleDateFormat( "hh:mm a");
    SimpleDateFormat sdfSimpleTime = new SimpleDateFormat("HH:mm", Locale.KOREA);
    SimpleDateFormat sdfSimpleTime_En = new SimpleDateFormat("HH:mm");
    String notifyAt, notifyAt_complex;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    OneTimeWorkRequest requested;
    Context context;
    long oneDay = 1000*60*60*24;
    private static final String RecommendName = "Recommend";
    private static final String survey_name = "SurveyType";
    private static final String survey_key = "SQMood";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();
        SettingFragment settingFragment = this;
        this.context = v.getContext();

        sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        //Setting the time of the periodic notification
        if (!sharedPref.contains(NotifyKey)) {
            editor.putString(NotifyKey, "21:00").apply();
        }
        notifyAt = sharedPref.getString(NotifyKey, "21:00");
        notifyAt_complex = changeTimeFormatComplex(notifyAt);


        notifyButton = v.findViewById(R.id.notifyButton);

        //initifal setting of notifySetting
        if (!sharedPref.contains("isNotifyOn")) {
            editor.putBoolean("isNotifyOn", true).apply();
        }

        notifyButton.setText(notifyAt_complex);
        TextView notifyDescription = v.findViewById(R.id.NotifyDescription);
        TextView noNotifyDescription = v.findViewById(R.id.NoNotifyDescription);
        LinearLayout notifyView = v.findViewById(R.id.NotifyView);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notifyDescription.setText("권한 없음");
            notifyButton.setVisibility(View.INVISIBLE);
            noNotifyDescription.setVisibility(View.VISIBLE);
        } else {
            notifyDescription.setText("알림 켜짐");
            notifyButton.setVisibility(View.VISIBLE);
            noNotifyDescription.setVisibility(View.GONE);
        }

        //Initial setting
        Log.v("SettingFragment", String.valueOf(sharedPref.getBoolean("isNotifyOn", true)));
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            if (sharedPref.getBoolean("isNotifyOn", true)) {
                notifyDescription.setText("알림 켜짐");
                notifyButton.setVisibility(View.VISIBLE);
            } else {
                notifyDescription.setText("알림 꺼짐");
                notifyButton.setVisibility(View.INVISIBLE);
            }
        }

        //On/Off the notification
        notifyView.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                if (sharedPref.getBoolean("isNotifyOn", true)) {
                    editor.putBoolean("isNotifyOn", false).apply();
                    notifyDescription.setText("알림 꺼짐");
                    notifyButton.setVisibility(View.INVISIBLE);
                } else {
                    editor.putBoolean("isNotifyOn", true).apply();
                    notifyDescription.setText("알림 켜짐");
                    notifyButton.setVisibility(View.VISIBLE);
                }
            }
        });


        notifyButton.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), settingFragment);
            timePickerDialog.setData(1);
            timePickerDialog.setTimePicker(notifyAt_complex);
            timePickerDialog.show();
        });

        //Move to HideActivity
        TextView hideDescription = v.findViewById(R.id.HideDescription);
        LinearLayout hideView = v.findViewById(R.id.HideView);
        if (!sharedPref.contains("isHidden")) {
            editor.putBoolean("isHidden", true).apply();
        }
        boolean isHidden = sharedPref.getBoolean("isHidden", true);
        if (!isHidden) {
            hideDescription.setText("켜짐");
        } else {
            hideDescription.setText("꺼짐");
        }
        hideView.setOnClickListener(view -> {
            Intent hideIntent = new Intent(v.getContext(), HideActivity.class);
            startActivity(hideIntent);
        });

        LinearLayout onsetView = v.findViewById(R.id.OnsetView);
        onsetView.setOnClickListener(view -> {
            Intent sleepOnsetIntent = new Intent(v.getContext(), SleepOnsetActivity.class);
            startActivity(sleepOnsetIntent);
        });

        //Do SQMood survey again
        if (!sharedPref.contains(survey_key)) {
            editor.putInt(survey_key, 0).apply();
        }
        int surveyDay = sharedPref.getInt(survey_key, 0);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        LinearLayout sqMoodVisitView = v.findViewById(R.id.SQMoodVisitView);
        sqMoodVisitView.setOnClickListener(view -> {
            Bundle temp = new Bundle();
            Intent surveyIntent = new Intent(v.getContext(), SQMoodSendingActivity.class);
            surveyIntent.putExtra(survey_name, 0);
            surveyIntent.putExtra("moodData", temp);
            startActivity(surveyIntent);
        });

        return v;
    }

    //Change "HH:mm" to milliseconds
    public long timeToSeconds(String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime time = LocalTime.parse(value, formatter);
        LocalDate currentDate = LocalDate.now();
        ZonedDateTime dateTime = ZonedDateTime.of(currentDate, time, ZoneId.systemDefault());

        return dateTime.toInstant().toEpochMilli();
    }

    //Change "HH:mm" to "hh:mm a"
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

    //Change "hh:mm a" to "HH:mm"
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
    public void setDateButtonText(String text, int isStartButton) {
    }

    @Override
    public void setTimeButtonText(String text, int isStartButton) {
        if (notifyButton != null) {
            notifyButton.setText(text);
            if (sharedPref != null && sharedPref.contains(NotifyKey)) {
                String buttonText = (String) notifyButton.getText();
                String shared_buttonText = changeTimeFormatSimple(buttonText);
                if (!shared_buttonText.equals("")) {
                    editor.putString(NotifyKey, shared_buttonText).apply();
                    Log.v("SettingFragment", "Notification time is set: "
                            + shared_buttonText);

                    //Request pushWorker newly
                    if (this.requested != null && context != null) {
                        long now = System.currentTimeMillis();
                        WorkManager.getInstance(context).cancelWorkById(requested.getId());
                        long targetTime = timeToSeconds(shared_buttonText);
                        long delay = targetTime - now;
                        if (delay < 0) {
                            delay += oneDay;
                        }
                        Log.v("SettingFragment", "Delay of the notification: " + delay);
                        OneTimeWorkRequest pushRequest = new OneTimeWorkRequest.Builder(PushWorker.class)
                                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                                .build();
                        WorkManager.getInstance(context).enqueueUniqueWork(RecommendName,
                                ExistingWorkPolicy.REPLACE, pushRequest);
                    }
                }
            }
        }
    }
}