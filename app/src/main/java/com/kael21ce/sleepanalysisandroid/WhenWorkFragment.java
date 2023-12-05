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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


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
        whenSleepDescription.setText(user_name + "님의 집중 시작 시간을 알려주세요");

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
                mainActivity.setVisibleBottomNavi();
                mainActivity.setBottomNaviItem(R.id.tabRecommend);
                int index = sleepOnsetTime.indexOf(":");
                String currentHr = inputSdfTime.format(now);
                String sleepHr = sleepOnsetTime.substring(0, index);
                String sleepMin = sleepOnsetTime.substring(index + 1);
                //Move to RecommendFragment
                //If sleep time is tomorrow
                if (Integer.parseInt(currentHr.substring(0, currentHr.indexOf(":"))) > Integer.parseInt(sleepHr)) {
                    sleepOnsetDate = sdfDate.format(now + oneDay);
                } else if (Integer.parseInt(currentHr.substring(0, currentHr.indexOf(":"))) == Integer.parseInt(sleepHr)) {
                    if (Integer.parseInt(currentHr.substring(currentHr.indexOf(":") + 1)) >= Integer.parseInt(sleepMin)) {
                        sleepOnsetDate = sdfDate.format(now + oneDay);
                    } else {
                        sleepOnsetDate = sdfDate.format(now);
                    }
                } else {
                    sleepOnsetDate = sdfDate.format(now);
                }
                //Set the work onset and work offset date
                if (sleepOnsetDate.equals(sdfDate.format(now))) {
                    if (Integer.parseInt(sleepOnsetTime.substring(0, index)) > Integer.parseInt(onHour1 + onHour2)) {
                        workOnsetDate = sdfDate.format(now + oneDay);
                        workOffsetDate = workOnsetDate;
                    } else if (Integer.parseInt(onHour1 + onHour2) > Integer.parseInt(offHour1 + offHour2)) {
                        workOnsetDate = sdfDate.format(now);
                        workOffsetDate = sdfDate.format(now + oneDay);
                    } else {
                        workOnsetDate = sleepOnsetDate;
                        workOffsetDate = sleepOnsetDate;
                    }
                } else {
                    if (Integer.parseInt(sleepOnsetTime.substring(0, index)) > Integer.parseInt(offHour1 + offHour2)) {
                        if (Integer.parseInt(offHour1 + offHour2) < Integer.parseInt(onHour1 + onHour2)) {
                            workOnsetDate = sleepOnsetDate;
                            workOffsetDate = sdfDate.format(now + oneDay*2);
                        } else {
                            workOnsetDate = sdfDate.format(now + oneDay*2);
                            workOffsetDate = sdfDate.format(now + oneDay*2);
                        }
                    } else {
                        workOnsetDate = sleepOnsetDate;
                        workOffsetDate = sleepOnsetDate;
                    }
                }
                //Set sleepOnset Time
                try {
                    Date sleepOnsetInput = inputSdfTime.parse(sleepOnsetTime);
                    sleepOnsetOutput = sdfTime.format(sleepOnsetInput);
                    Date workOnsetInput = inputSdfTime.parse(workOnsetTime);
                    workOnsetOutput = sdfTime.format(workOnsetInput);
                    Date workOffsetInput = inputSdfTime.parse(workOffsetTime);
                    workOffsetOutput = sdfTime.format(workOffsetInput);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String sleepOnsetSDF = sleepOnsetDate + ' ' + sleepOnsetOutput;
                String workOnsetSDF = workOnsetDate + ' ' + workOnsetOutput;
                String workOffsetSDF = workOffsetDate + ' ' + workOffsetOutput;

                Date sleepOnset = null;
                Date workOnset = null;
                Date workOffset = null;
                try {
                    sleepOnset = sdf.parse(sleepOnsetSDF);
                    workOnset = sdf.parse(workOnsetSDF);
                    workOffset = sdf.parse(workOffsetSDF);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                assert sleepOnset != null;
                assert workOnset != null;
                assert workOffset != null;

                if(isValid(sleepOnset.getTime(), workOnset.getTime(), workOffset.getTime())) {
                    mainActivity.setSleepOnset(sleepOnset.getTime());
                    mainActivity.setWorkOnset(workOnset.getTime());
                    mainActivity.setWorkOffset(workOffset.getTime());

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
}