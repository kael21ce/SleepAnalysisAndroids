package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.kael21ce.sleepanalysisandroid.data.DataSurvey;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SleepOnsetActivity extends AppCompatActivity implements ButtonTextUpdater{

    private Button sleepOnsetDateButton, sleepOnsetTimeButton, workOnsetDateButton, workOnsetTimeButton,
     workOffsetDateButton, workOffsetTimeButton, sleepSettingSubmitButton;
    public DatePickerDialog datePickerDialog;
    public TimePickerDialog timePickerDialog;
    private TabLayout workTypeTabSetting;
    long sleepOnset, workOnset, workOffset, sleepOnsetShow;
    long sleepOnsetEditTime, sleepOnsetShowEditTime, workOnsetEditTime, workOffsetEditTime;
    int workType;
    SimpleDateFormat sdf;
    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy.MM.dd");
    SimpleDateFormat sdfTime;
    long now, nineHours;
    private String languageSetting = Locale.getDefault().getLanguage();
    String sleepOnsetTimeText, workOnsetTimeText, workOffsetTimeText;

    @Override
    protected void onCreate(Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_sleep_onset);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton sleepOnsetBackButton = findViewById(R.id.SleepOnsetBackButton);
        //Click back button
        sleepOnsetBackButton.setOnClickListener(view -> {
            Intent nextIntent = new Intent(SleepOnsetActivity.this, SplashActivity.class);
            nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextIntent);
        });

        sleepOnsetDateButton = findViewById(R.id.sleepOnsetDateButton);
        sleepOnsetTimeButton = findViewById(R.id.sleepOnsetTimeButton);
        workOnsetDateButton = findViewById(R.id.workOnsetDateButton);
        workOnsetTimeButton = findViewById(R.id.workOnsetTimeButton);
        workOffsetDateButton = findViewById(R.id.workOffsetDateButton);
        workOffsetTimeButton = findViewById(R.id.workOffsetTimeButton);
        workTypeTabSetting = findViewById(R.id.workTypeTabSetting);
        sleepSettingSubmitButton = findViewById(R.id.sleepSettingSubmitButton);

        nineHours = (1000*60*60*9);
        now = System.currentTimeMillis();

        MainActivity mainActivity = new MainActivity();

        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        sleepOnset = sharedPref.getLong("sleepOnset", now);
        sleepOnsetShow = sharedPref.getLong("sleepOnsetShow", now);
        workOnset = sharedPref.getLong("workOnset", now);
        workOffset = sharedPref.getLong("workOffset", now);
        workType = sharedPref.getInt("workType", 0);

//        String sleepOnsetString = sdf.format(new Date(sleepOnset));
//        String workOnsetString = sdf.format(new Date(workOnset));
//        String workOffsetString = sdf.format(new Date(workOffset));

        if (languageSetting.equals("ko")) {
            sdf = new SimpleDateFormat("yyyy.MM.dd a h:mm", Locale.KOREA);
            sdfTime = new SimpleDateFormat("a h:mm", Locale.KOREA);
        } else {
            sdf = new SimpleDateFormat("yyyy.MM.dd h:mm a");
            sdfTime = new SimpleDateFormat("h:mm a");
        }

        TimeZone timeZone = TimeZone.getDefault();
        sdf.setTimeZone(timeZone);
        sdfTime.setTimeZone(timeZone);
        sdfDate.setTimeZone(timeZone);

        sleepOnsetDateButton.setText(sdfDate.format(new Date(sleepOnsetShow)));
        sleepOnsetTimeButton.setText(sdfTime.format(new Date(sleepOnsetShow)));
        workOnsetDateButton.setText(sdfDate.format(new Date(workOnset)));
        workOnsetTimeButton.setText(sdfTime.format(new Date(workOnset)));
        workOffsetDateButton.setText(sdfDate.format(new Date(workOffset)));
        workOffsetTimeButton.setText(sdfTime.format(new Date(workOffset)));

        SleepOnsetActivity sleepOnsetActivity = this;
        sleepOnsetDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(this, sleepOnsetActivity);
            datePickerDialog.setData(0);
            datePickerDialog.setDatePicker((String) sleepOnsetDateButton.getText());
            datePickerDialog.show();
        });
        sleepOnsetTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(this, sleepOnsetActivity);
            timePickerDialog.setData(0);
            timePickerDialog.setTimePicker((String) sleepOnsetTimeButton.getText());
            timePickerDialog.show();
        });
        workOnsetDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(this, sleepOnsetActivity);
            datePickerDialog.setData(1);
            datePickerDialog.setDatePicker((String) workOnsetDateButton.getText());
            datePickerDialog.show();
        });
        workOnsetTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(this, sleepOnsetActivity);
            timePickerDialog.setData(1);
            timePickerDialog.setTimePicker((String) workOnsetTimeButton.getText());
            timePickerDialog.show();
        });
        workOffsetDateButton.setOnClickListener(view -> {
            datePickerDialog = new DatePickerDialog(this, sleepOnsetActivity);
            datePickerDialog.setData(2);
            datePickerDialog.setDatePicker((String) workOffsetDateButton.getText());
            datePickerDialog.show();
        });
        workOffsetTimeButton.setOnClickListener(view -> {
            timePickerDialog = new TimePickerDialog(this, sleepOnsetActivity);
            timePickerDialog.setData(2);
            timePickerDialog.setTimePicker((String) workOffsetTimeButton.getText());
            timePickerDialog.show();
        });

        final int[] selectedType = new int[1];
        selectedType[0] = workType;
        workTypeTabSetting.addTab(workTypeTabSetting.newTab().setText("휴무"));
        workTypeTabSetting.addTab(workTypeTabSetting.newTab().setText("아침"));
        workTypeTabSetting.addTab(workTypeTabSetting.newTab().setText("저녁"));
        workTypeTabSetting.addTab(workTypeTabSetting.newTab().setText("야간"));
        int initialTab = Math.abs(workType);
        Objects.requireNonNull(workTypeTabSetting.getTabAt(initialTab)).select();
        workTypeTabSetting.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        selectedType[0] = 0;
                        Log.v("SleepOnsetActivity", "Selected Work type: " + selectedType[0]);
                        break;
                    case 1:
                        selectedType[0] = -1;
                        Log.v("SleepOnsetActivity", "Selected Work type: " + selectedType[0]);
                        break;
                    case 2:
                        selectedType[0] = -2;
                        Log.v("SleepOnsetActivity", "Selected Work type: " + selectedType[0]);
                        break;
                    case 3:
                        selectedType[0] = -3;
                        Log.v("SleepOnsetActivity", "Selected Work type: " + selectedType[0]);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        sleepSettingSubmitButton.setOnClickListener(view -> {
            String sleepOnsetDate = (String) sleepOnsetDateButton.getText();
            String sleepOnsetTime = (String) sleepOnsetTimeButton.getText();
            String sleepOnsetSDF = sleepOnsetDate + ' ' + sleepOnsetTime;
            String workOnsetDate = (String) workOnsetDateButton.getText();
            String workOnsetTime = (String) workOnsetTimeButton.getText();
            String workOnsetSDF = workOnsetDate + ' ' + workOnsetTime;
            String workOffsetDate = (String) workOffsetDateButton.getText();
            String workOffsetTime = (String) workOffsetTimeButton.getText();
            String workOffsetSDF = workOffsetDate + ' ' + workOffsetTime;
            Log.v("SLEEP ONSET SDF", sleepOnsetSDF);
            Log.v("WORK ONSET SDF", workOnsetSDF);
            Log.v("WORK OFFSET SDF", workOffsetSDF);

            Date sleepOnsetEdit = new Date(sleepOnset);
            Date workOnsetEdit = new Date(workOnset);
            Date workOffsetEdit = new Date(workOffset);
            try {
                sleepOnsetEdit = sdf.parse(sleepOnsetSDF);
                workOnsetEdit = sdf.parse(workOnsetSDF);
                workOffsetEdit = sdf.parse(workOffsetSDF);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            assert sleepOnsetEdit != null;
            assert workOnsetEdit != null;
            assert workOffsetEdit != null;

            Long[] updateDates = updateOnsetDate(now, sleepOnsetEdit.getTime(), sleepOnsetEdit.getTime(),
                    workOnsetEdit.getTime(), workOffsetEdit.getTime());
            sleepOnsetEditTime = updateDates[0];
            sleepOnsetShowEditTime = updateDates[1];
            workOnsetEditTime = updateDates[2];
            workOffsetEditTime = updateDates[3];

            if(isValid(sleepOnsetEditTime, workOnsetEditTime, workOffsetEditTime)) {
                if (Math.abs(selectedType[0]) < 4) {
                    mainActivity.setSleepOnset(sleepOnsetEditTime);
                    editor.putLong("sleepOnsetShow", sleepOnsetShowEditTime).apply();
                    mainActivity.setWorkOnset(workOnsetEditTime);
                    mainActivity.setWorkOffset(workOffsetEditTime);
                    editor.putInt("workType", selectedType[0]).apply();
                    sendSurvey(sleepOnsetEditTime, workOnsetEditTime, workOffsetEditTime, selectedType[0]);

                    Log.v("SleepOnsetActivity", "Onset: " + sleepOnsetEditTime + " / Onset Show: " + sleepOnsetShowEditTime +
                            " / Work onset: " + workOnsetEditTime + " / Work offset: " + workOffsetEditTime);

                    mainActivity.finish();
                    startActivity(new Intent(this, SplashActivity.class));
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(true);
                    if (languageSetting.equals("ko")) {
                        builder.setTitle("경고");
                        builder.setMessage("근무 종류를 선택해주세요.");

                        builder.setNegativeButton("OK", (dialogInterface, i) -> dialogInterface.cancel());
                    } else {
                        builder.setTitle("ERROR");
                        builder.setMessage("Choose the work type");

                        builder.setNegativeButton("OK", (dialogInterface, i) -> dialogInterface.cancel());
                    }

                    AlertDialog alert = builder.create();
                    alert.setOnShowListener(arg0 -> {
                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black, null));
                    });
                    alert.show();
                }
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                if (languageSetting.equals("ko")) {
                    builder.setTitle("경고");
                    builder.setMessage("잘못된 입력값입니다.");

                    builder.setNegativeButton("OK", (dialogInterface, i) -> dialogInterface.cancel());
                } else {
                    builder.setTitle("ERROR");
                    builder.setMessage("INVALID INPUT");

                    builder.setNegativeButton("OK", (dialogInterface, i) -> dialogInterface.cancel());
                }

                AlertDialog alert = builder.create();
                alert.setOnShowListener(arg0 -> {
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black, null));
                });
                alert.show();
            }
        });
    }

    public boolean isValid(long sleepOnset1, long workOnset1, long workOffset1){
        if(sleepOnset1 <= workOnset1 && workOnset1 <= workOffset1){
            if(workOnset1 - sleepOnset1 <= 1000*60*60*24 && workOffset1 - workOnset1 <= 1000*60*60*24){
                return true;
            }
        }
        return false;
    }

    //Change the text of Button
    public void setDateButtonText(String text, int isStartButton) {
        if (isStartButton==0) {
            if (sleepOnsetDateButton != null) {
                sleepOnsetDateButton.setText(text);
            }
        } else if(isStartButton == 1) {
            if (workOnsetDateButton != null) {
                workOnsetDateButton.setText(text);
            }
        } else{
            if (workOffsetDateButton != null) {
                workOffsetDateButton.setText(text);
            }
        }
    }

    public void setTimeButtonText(String text, int isStartButton) {
        if (isStartButton==0) {
            if (sleepOnsetTimeButton != null) {
                sleepOnsetTimeButton.setText(text);
            }
        } else if(isStartButton == 1) {
            if (workOnsetTimeButton != null) {
                workOnsetTimeButton.setText(text);
            }
        } else{
            if (workOffsetTimeButton != null) {
                workOffsetTimeButton.setText(text);
            }
        }
    }

    public String getDateButtonText(int isStartButton) {
        String nullString = "2024.01.01";
        if (isStartButton==0) {
            if (sleepOnsetDateButton != null) {
                return (String) sleepOnsetDateButton.getText();
            } else {
                return nullString;
            }
        } else if(isStartButton == 1) {
            if (workOnsetDateButton != null) {
                return (String) workOnsetDateButton.getText();
            } else {
                return nullString;
            }
        } else {
            if (workOffsetDateButton != null) {
                return (String) workOffsetDateButton.getText();
            } else {
                return nullString;
            }
        }
    }

    //Send info to server about changing schedule
    private void sendSurvey(long sleep_onset, long work_onset, long work_offset, int work_type){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sleep-math.com/sleepapp/")
                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())
                // at last we are building our retrofit builder.
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString("User_Email", "tester33");
        long time = System.currentTimeMillis();

        DataSurvey survey = new DataSurvey(userEmail, sleep_onset, work_onset, work_offset, work_type, time);
        Call<DataSurvey> call = retrofitAPI.createSurvey(survey);
        call.enqueue(new Callback<DataSurvey>() {
            @Override
            public void onResponse(Call<DataSurvey> call, Response<DataSurvey> response) {
                // this method is called when we get response from our api.
                Locale currentLocale = Locale.getDefault();
                String language = currentLocale.getLanguage();
                if(response.code() <= 300) {
                    if (language.equals("ko")) {
                        Toast.makeText(SleepOnsetActivity.this, "데이터가 전송되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SleepOnsetActivity.this, "Data added to API", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (language.equals("ko")) {
                        Toast.makeText(SleepOnsetActivity.this, "데이터 전송에 실패했습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SleepOnsetActivity.this, "Data sending failed", Toast.LENGTH_SHORT).show();
                    }
                    // we are getting response from our body
                    // and passing it to our modal class.
                    DataSurvey responseFromAPI = response.body();

                    // on below line we are getting our data from modal class and adding it to our string.
                    String responseString = "Response Code : " + response.code() + "\nName : " + "\n";
                    Log.v("RESPONSE", responseString);
                }
            }

            @Override
            public void onFailure(Call<DataSurvey> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
                Log.v("ERROR", "Error found is : " + t.getMessage());
            }
        });
    }

    public static Long[] updateOnsetDate(long currentTime, long sleepOnset, long sleepOnsetShow, long workOnset, long workOffset) {
        long oneDayToMils = 1000*60*60*24;
        long tenMinToMils = 1000*60*10;
        long oneHourToMils = 1000*60*60;

        // Keep sleepOnsetShow before workOnset minus 1 day
        while (sleepOnsetShow < workOnset - oneDayToMils) {
            sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            sleepOnset = sleepOnsetShow;
        }

        // Ensure workOnset is after sleepOnset
        while (workOnset < sleepOnset) {
            workOnset = workOnset + oneDayToMils;
        }

        // Ensure workOffset is after workOnset
        while (workOffset < workOnset) {
            workOffset = workOffset + oneDayToMils;
        }

        // Adjust sleepOnset if currentTime is within sleepOnset and workOnset
        if (sleepOnset <= currentTime && currentTime <= workOnset) {
            while (sleepOnset < currentTime) {
                sleepOnset = currentTime + tenMinToMils;
            }
        }

        // Ensure sleepOnsetShow is not before currentTime
        if (workOnset - oneHourToMils <= sleepOnset) {
            while (sleepOnsetShow < currentTime) {
                sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            }
            sleepOnset = sleepOnsetShow;
        }

        // Repeat the adjustments for sleepOnsetShow, workOnset, and workOffset
        while (sleepOnsetShow < workOnset - oneDayToMils) {
            sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            sleepOnset = sleepOnsetShow;
        }
        while (workOnset < sleepOnset) {
            workOnset = workOnset + oneDayToMils;
        }
        while (workOffset < workOnset) {
            workOffset = workOffset + oneDayToMils;
        }

        // Update work if it is ended
        while (currentTime > workOffset) {
            workOnset = workOnset + oneDayToMils;
            workOffset = workOffset + oneDayToMils;
        }

        return new Long[]{sleepOnset, sleepOnsetShow, workOnset, workOffset};
    }
}