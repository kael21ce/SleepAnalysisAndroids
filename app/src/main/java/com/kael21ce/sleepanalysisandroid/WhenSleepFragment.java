package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class WhenSleepFragment extends Fragment {
    String hour1, hour2, minute1, minute2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_when_sleep, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();

        //Set the user name
        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        String user_name = sharedPref.getString("User_Name", "UserName");
        TextView whenSleepDescription = v.findViewById(R.id.whenSleepDescription);
        whenSleepDescription.setText(user_name + "님이 희망하시는 취침시간을 알려주세요");

        //Back to RecommendFragment
        ImageButton sleepBackButton = v.findViewById(R.id.sleepBackButton);
        sleepBackButton.setOnClickListener(view -> {
            mainActivity.setVisibleBottomNavi();
            mainActivity.setBottomNaviItem(R.id.tabRecommend);
            RecommendFragment recommendFragment = new RecommendFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, recommendFragment).commit();
        });

        //Get the sleep onset
        EditText whenSleepHour1 = v.findViewById(R.id.whenSleepHour1);
        EditText whenSleepHour2 = v.findViewById(R.id.whenSleepHour2);
        EditText whenSleepMinute1 = v.findViewById(R.id.whenSleepMinute1);
        EditText whenSleepMinute2 = v.findViewById(R.id.whenSleepMinute2);
        Button whenSleepButton = v.findViewById(R.id.whenSleepButton);
        //Initial setting
        whenSleepButton.setEnabled(false);
        whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));

        //Check validity of onset time
        whenSleepHour1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenSleepHour1.getText() == null || whenSleepHour1.getText().toString().isEmpty()) {
                    whenSleepButton.setEnabled(false);
                    whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (Integer.parseInt(whenSleepHour1.getText().toString()) >= 3) {
                    whenSleepButton.setEnabled(false);
                    whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (Integer.parseInt(whenSleepHour1.getText().toString()) == 2 && hour2 != null) {
                    if (Integer.parseInt(hour2) >= 4) {
                        whenSleepButton.setEnabled(false);
                        whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                    }
                } else {
                    whenSleepButton.setEnabled(true);
                    whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    hour1 = whenSleepHour1.getText().toString();
                }
                whenSleepHour2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        whenSleepHour2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenSleepHour2.getText() == null || whenSleepHour2.getText().toString().isEmpty()) {
                    whenSleepButton.setEnabled(false);
                    whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (Integer.parseInt(whenSleepHour2.getText().toString()) >= 4 && hour1 != null) {
                    if (Integer.parseInt(hour1) == 2) {
                        whenSleepButton.setEnabled(false);
                        whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                    }
                } else {
                    whenSleepButton.setEnabled(true);
                    whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    hour2 = whenSleepHour2.getText().toString();
                }
                whenSleepMinute1.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        whenSleepMinute1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenSleepMinute1.getText() == null || whenSleepMinute1.getText().toString().isEmpty()) {
                    whenSleepButton.setEnabled(false);
                    whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else if (Integer.parseInt(whenSleepMinute1.getText().toString()) >= 6) {
                    whenSleepButton.setEnabled(false);
                    whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else {
                    whenSleepButton.setEnabled(true);
                    whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    minute1 = whenSleepMinute1.getText().toString();
                }
                whenSleepMinute2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        whenSleepMinute2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (whenSleepMinute2.getText() == null || whenSleepMinute2.getText().toString().isEmpty()) {
                    whenSleepButton.setEnabled(false);
                    whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
                } else {
                    whenSleepButton.setEnabled(true);
                    whenSleepButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                    minute2 = whenSleepMinute2.getText().toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //Send sleep onset to whenActivityFragment
        whenSleepButton.setOnClickListener(view -> {
            Bundle onSetBundle = new Bundle();
            onSetBundle.putString("SleepOnset", hour1 + hour2 + ":" + minute1 + minute2);
            //Move to whenActivityFragment

        });
        return v;
    }
}