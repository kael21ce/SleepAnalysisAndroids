package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.kael21ce.sleepanalysisandroid.data.DataSurvey;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class WhenWorkFragment extends Fragment {
    String onHour1, onHour2, onMinute1, onMinute2, offHour1, offHour2, offMinute1, offMinute2;
    String sleepOnsetTime, workOnsetTime, workOffsetTime, sleepOnsetDate, workOnsetDate, workOffsetDate;
    String sleepOnsetOutput, workOnsetOutput, workOffsetOutput;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm aaa");
    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy.MM.dd");
    SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm aaa");
    SimpleDateFormat inputSdfTime = new SimpleDateFormat("HH:mm");
    long now = System.currentTimeMillis();
    long oneDay = (1000*60*60*24);
    long sleepOnsetResult, sleepOnsetShowResult, workOnsetResult, workOffsetResult;
    long sleepOnsetType, workOnsetType, workOffsetType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_when_work, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();

        //Hide action bar
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        //Set the user name
        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        String user_name = sharedPref.getString("User_Name", "UserName");
        TextView whenSleepDescription = v.findViewById(R.id.whenWorkDescription);

        //Back to RecommendFragment
        ImageButton sleepBackButton = v.findViewById(R.id.workBackButton);
        sleepBackButton.setOnClickListener(view -> {
            WhenSleepFragment whenSleepFragment = new WhenSleepFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, whenSleepFragment).commit();
        });

        //Get the work onset
        EditText whenWorkOnHour1 = v.findViewById(R.id.whenWorkOnHour1);
        EditText whenWorkOnHour2 = v.findViewById(R.id.whenWorkOnHour2);
        EditText whenWorkOnMinute1 = v.findViewById(R.id.whenWorkOnMinute1);
        EditText whenWorkOnMinute2 = v.findViewById(R.id.whenWorkOnMinute2);
        //Get the work offset
        EditText whenWorkOffHour1 = v.findViewById(R.id.whenWorkOffHour1);
        EditText whenWorkOffHour2 = v.findViewById(R.id.whenWorkOffHour2);
        EditText whenWorkOffMinute1 = v.findViewById(R.id.whenWorkOffMinute1);
        EditText whenWorkOffMinute2 = v.findViewById(R.id.whenWorkOffMinute2);
        Button whenWorkButton = v.findViewById(R.id.whenWorkButton);
        //Initial setting
        whenWorkButton.setEnabled(false);
        whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));

        //Check validity
        whenWorkOnHour1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenWorkOnHour1.getText() == null || whenWorkOnHour1.getText().toString().isEmpty()) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (Integer.parseInt(whenWorkOnHour1.getText().toString()) >= 3) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (Integer.parseInt(whenWorkOnHour1.getText().toString()) == 2 && onHour2 != null) {
                    if (Integer.parseInt(onHour2) >= 4) {
                        whenWorkButton.setEnabled(false);
                        whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                    }
                } else {
                    whenWorkButton.setEnabled(true);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    onHour1 = whenWorkOnHour1.getText().toString();
                }
                whenWorkOnHour2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        whenWorkOnHour2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenWorkOnHour2.getText() == null || whenWorkOnHour2.getText().toString().isEmpty()) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (onHour1 != null) {
                    if (Integer.parseInt(whenWorkOnHour2.getText().toString()) >= 4 && Integer.parseInt(onHour1) == 2) {
                        whenWorkButton.setEnabled(false);
                        whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                    } else {
                        whenWorkButton.setEnabled(true);
                        whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                        onHour2 = whenWorkOnHour2.getText().toString();
                    }
                } else {
                    whenWorkButton.setEnabled(true);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    onHour2 = whenWorkOnHour2.getText().toString();
                }
                whenWorkOnMinute1.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        whenWorkOnMinute1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenWorkOnMinute1.getText() == null || whenWorkOnMinute1.getText().toString().isEmpty()) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (Integer.parseInt(whenWorkOnMinute1.getText().toString()) >= 6) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else {
                    whenWorkButton.setEnabled(true);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    onMinute1 = whenWorkOnMinute1.getText().toString();
                }
                whenWorkOnMinute2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        whenWorkOnMinute2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenWorkOnMinute2.getText() == null || whenWorkOnMinute2.getText().toString().isEmpty()) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else {
                    whenWorkButton.setEnabled(true);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    onMinute2 = whenWorkOnMinute2.getText().toString();
                }
                whenWorkOffHour1.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        whenWorkOffHour1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenWorkOffHour1.getText() == null || whenWorkOffHour1.getText().toString().isEmpty()) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (Integer.parseInt(whenWorkOffHour1.getText().toString()) >= 3) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (Integer.parseInt(whenWorkOnHour1.getText().toString()) == 2 && offHour2 != null) {
                    if (Integer.parseInt(offHour2) >= 4) {
                        whenWorkButton.setEnabled(false);
                        whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                    }
                } else {
                    whenWorkButton.setEnabled(true);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    offHour1 = whenWorkOffHour1.getText().toString();
                }
                whenWorkOffHour2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        whenWorkOffHour2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenWorkOffHour2.getText() == null || whenWorkOffHour2.getText().toString().isEmpty()) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (offHour1 != null) {
                    if (Integer.parseInt(whenWorkOffHour2.getText().toString()) >= 4 && Integer.parseInt(offHour1) == 2) {
                        whenWorkButton.setEnabled(false);
                        whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                    } else {
                        whenWorkButton.setEnabled(true);
                        whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                        offHour2 = whenWorkOffHour2.getText().toString();
                    }
                } else {
                    whenWorkButton.setEnabled(true);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    offHour2 = whenWorkOffHour2.getText().toString();
                }
                whenWorkOffMinute1.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        whenWorkOffMinute1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenWorkOffMinute1.getText() == null || whenWorkOffMinute1.getText().toString().isEmpty()) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (Integer.parseInt(whenWorkOffMinute1.getText().toString()) >= 6) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else {
                    whenWorkButton.setEnabled(true);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    offMinute1 = whenWorkOffMinute1.getText().toString();
                }
                whenWorkOffMinute2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        whenWorkOffMinute2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenWorkOffMinute2.getText() == null || whenWorkOffMinute2.getText().toString().isEmpty()) {
                    whenWorkButton.setEnabled(false);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else {
                    whenWorkButton.setEnabled(true);
                    whenWorkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    offMinute2 = whenWorkOffMinute2.getText().toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Work Type Picker
        final int[] selectedType = new int[1];
        selectedType[0] = 0;
        TabLayout workTypeTab = v.findViewById(R.id.workTypeTab);
        workTypeTab.addTab(workTypeTab.newTab().setText("휴무"));
        workTypeTab.addTab(workTypeTab.newTab().setText("아침"));
        workTypeTab.addTab(workTypeTab.newTab().setText("저녁"));
        workTypeTab.addTab(workTypeTab.newTab().setText("야간"));

        workTypeTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        selectedType[0] = 0;
                        Log.v("WhenWorkFragment", "Selected Work type: " + selectedType[0]);
                        break;
                    case 1:
                        selectedType[0] = -1;
                        Log.v("WhenWorkFragment", "Selected Work type: " + selectedType[0]);
                        break;
                    case 2:
                        selectedType[0] = -2;
                        Log.v("WhenWorkFragment", "Selected Work type: " + selectedType[0]);
                        break;
                    case 3:
                        selectedType[0] = -3;
                        Log.v("WhenWorkFragment", "Selected Work type: " + selectedType[0]);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        Bundle sleepBundle = getArguments();
        if (sleepBundle != null) {
            sleepOnsetTime = sleepBundle.getString("SleepOnset");
            Log.d("SleepOnset", sleepOnsetTime);
        }

        //Save work onset and offset to database
        whenWorkButton.setOnClickListener(view -> {
            workOnsetTime = onHour1 + onHour2 + ":" + onMinute1 + onMinute2;
            workOffsetTime = offHour1 + offHour2 + ":" + offMinute1 + offMinute2;
            Log.v("WhenWorkFragment", "SleepOnset: " + sleepOnsetTime + " /  WorkOnset: " + workOnsetTime);
            if (sleepOnsetTime.equals(workOnsetTime)) {
                Toast.makeText(v.getContext(), "취침 시간과 집중 시작 시간은 일치하면 안됩니다!",Toast.LENGTH_SHORT).show();
            } else {
                Date sleepOnsetInput, workOnsetInput, workOffsetInput;
                Calendar sleepOnsetCal, workOnsetCal, workOffsetCal;
                try {
                    sleepOnsetInput = inputSdfTime.parse(sleepOnsetTime);


                    workOnsetInput = inputSdfTime.parse(workOnsetTime);


                    workOffsetInput = inputSdfTime.parse(workOffsetTime);
                } catch (ParseException e) {
                    sleepOnsetInput = new Date();
                    workOnsetInput = new Date();
                    workOffsetInput = new Date();
                }
                sleepOnsetCal = Calendar.getInstance();
                Calendar timeCal = Calendar.getInstance();
                timeCal.setTime(sleepOnsetInput);
                sleepOnsetCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                sleepOnsetCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                sleepOnsetCal.set(Calendar.SECOND, 0);
                sleepOnsetCal.set(Calendar.MILLISECOND, 0);
                sleepOnsetType = sleepOnsetCal.getTimeInMillis();

                workOnsetCal = Calendar.getInstance();
                timeCal = Calendar.getInstance();
                timeCal.setTime(workOnsetInput);
                workOnsetCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                workOnsetCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                workOnsetCal.set(Calendar.SECOND, 0);
                workOnsetCal.set(Calendar.MILLISECOND, 0);
                workOnsetType = workOnsetCal.getTimeInMillis();

                timeCal = Calendar.getInstance();
                timeCal.setTime(workOffsetInput);
                workOffsetCal = Calendar.getInstance();
                workOffsetCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                workOffsetCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                workOffsetCal.set(Calendar.SECOND, 0);
                workOffsetCal.set(Calendar.MILLISECOND, 0);
                workOffsetType = workOffsetCal.getTimeInMillis();

                if (sleepOnsetType <= now) {
                    sleepOnsetType = sleepOnsetType + oneDay;
                }

                Long[] updateDates = updateOnsetDate(now, sleepOnsetType, sleepOnsetType,
                        workOnsetType, workOffsetType);
                sleepOnsetResult = updateDates[0];
                sleepOnsetShowResult = updateDates[1];
                workOnsetResult = updateDates[2];
                workOffsetResult = updateDates[3];

                if(isValid(sleepOnsetResult, workOnsetResult, workOffsetResult)) {
                    mainActivity.setSleepOnset(sleepOnsetResult);
                    mainActivity.setWorkOnset(workOnsetResult);
                    mainActivity.setWorkOffset(workOffsetResult);
                    sharedPref.edit().putInt("workType", selectedType[0]).apply();
                    sharedPref.edit().putLong("sleepOnsetShow", sleepOnsetShowResult).apply();

                    Log.v("SplashActivity", "Onset: " + sleepOnsetResult + " / Onset Show: " + sleepOnsetShowResult +
                            " / Work onset: " + workOnsetResult + " / Work offset: " + workOffsetResult);

                    sendSurvey(sleepOnsetResult, workOnsetResult, workOffsetResult, selectedType[0]);

                    mainActivity.finish();
                    startActivity(new Intent(mainActivity, SplashActivity.class));
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle("ERROR");
                    builder.setMessage("INVALID INPUT");

                    builder.setNegativeButton("OK", (dialogInterface, i) -> dialogInterface.cancel());

                    AlertDialog alert = builder.create();
                    alert.setOnShowListener(arg0 -> {
                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
                    });
                    alert.show();
                }
            }

        });
        return v;
    }

    public boolean isValid(long sleepOnset1, long workOnset1, long workOffset1) {
        if(sleepOnset1 <= workOnset1 && workOnset1 <= workOffset1){
            if(workOnset1 - sleepOnset1 <= 1000*60*60*24 && workOffset1 - workOnset1 <= 1000*60*60*24){
                return true;
            }
        }
        return false;
    }

    //Check whether today is the weekend
    public boolean isWeekend() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    private void sendSurvey(long sleep_onset, long work_onset, long work_offset, int work_type){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sleep-math.com/sleepapp/")
                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())
                // at last we are building our retrofit builder.
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
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
//                        Toast.makeText(getActivity(), "데이터가 전송되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(getActivity(), "Data added to API", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (language.equals("ko")) {
//                        Toast.makeText(getActivity(), "데이터 전송에 실패했습니다", Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(getActivity(), "Data sending failed", Toast.LENGTH_SHORT).show();
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