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

import com.kael21ce.sleepanalysisandroid.data.DataSurvey;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SleepOnsetActivity extends AppCompatActivity implements ButtonTextUpdater{

    private Button sleepOnsetDateButton;
    private Button sleepOnsetTimeButton;
    private Button workOnsetDateButton;
    private Button workOnsetTimeButton;
    private Button workOffsetDateButton;
    private Button workOffsetTimeButton;
    private Button sleepSettingSubmitButton;
    public DatePickerDialog datePickerDialog;
    public TimePickerDialog timePickerDialog;
    long sleepOnset, workOnset, workOffset;
    SimpleDateFormat sdf;
    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy.MM.dd");
    SimpleDateFormat sdfTime;
    long now, nineHours;
    private String languageSetting = Locale.getDefault().getLanguage();

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
        sleepSettingSubmitButton = findViewById(R.id.sleepSettingSubmitButton);

        nineHours = (1000*60*60*9);
        now = System.currentTimeMillis();

        MainActivity mainActivity = new MainActivity();

        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        sleepOnset = sharedPref.getLong("sleepOnset", now);
        workOnset = sharedPref.getLong("workOnset", now);
        workOffset = sharedPref.getLong("workOffset", now);

//        String sleepOnsetString = sdf.format(new Date(sleepOnset));
//        String workOnsetString = sdf.format(new Date(workOnset));
//        String workOffsetString = sdf.format(new Date(workOffset));

        if (languageSetting.equals("ko")) {
            sdf = new SimpleDateFormat("yyyy.MM.dd a hh:mm", Locale.KOREA);
            sdfTime = new SimpleDateFormat("a hh:mm", Locale.KOREA);
        } else {
            sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm a");
            sdfTime = new SimpleDateFormat("hh:mm a");
        }

        sleepOnsetDateButton.setText(sdfDate.format(new Date(sleepOnset)));
        sleepOnsetTimeButton.setText(sdfTime.format(new Date(sleepOnset)));
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

            if(isValid(sleepOnsetEdit.getTime(), workOnsetEdit.getTime(), workOffsetEdit.getTime())) {
                mainActivity.setSleepOnset(sleepOnsetEdit.getTime());
                mainActivity.setWorkOnset(workOnsetEdit.getTime());
                mainActivity.setWorkOffset(workOffsetEdit.getTime());
                sendSurvey(sleepOnsetEdit.getTime(), workOnsetEdit.getTime(), workOffsetEdit.getTime());

                mainActivity.finish();
                startActivity(new Intent(this, SplashActivity.class));
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
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
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
    private void sendSurvey(long sleep_onset, long work_onset, long work_offset){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sleep-math.com/sleepapp/")
                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())
                // at last we are building our retrofit builder.
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        String username = sharedPref.getString("User_Name", "tester33");
        long time = System.currentTimeMillis();

        DataSurvey survey = new DataSurvey(username, sleep_onset, work_onset, work_offset, -1, time);
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
}