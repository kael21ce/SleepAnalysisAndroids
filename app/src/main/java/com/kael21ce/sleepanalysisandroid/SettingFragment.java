package com.kael21ce.sleepanalysisandroid;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.Sleep;

public class SettingFragment extends Fragment {

    Boolean isFolded = true;
    SleepOnsetFragment sleepOnsetFragment = new SleepOnsetFragment();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();

        LinearLayout onsetView = v.findViewById(R.id.OnsetView);
        ImageView onsetButton = v.findViewById(R.id.OnsetButton);

        //Get the information about whether infoButton is clicked
        if (getArguments() != null) {
            String reply = getArguments().getString("isClicked");
            if (reply.equals("Yes")) {
                isFolded = false;
                getChildFragmentManager().beginTransaction().replace(R.id.SettingView, sleepOnsetFragment).commit();
                onsetView.setBackground(AppCompatResources.getDrawable(v.getContext(), R.color.gray_1));
                ObjectAnimator.ofFloat(onsetButton, View.ROTATION, 0f, 90f).setDuration(100).start();
            }
        }
        onsetView.setOnClickListener(view -> {
            if (isFolded) {
                isFolded = false;
                getChildFragmentManager().beginTransaction().replace(R.id.SettingView, sleepOnsetFragment).commit();
                onsetView.setBackground(AppCompatResources.getDrawable(v.getContext(), R.color.gray_1));
                ObjectAnimator.ofFloat(onsetButton, View.ROTATION, 0f, 90f).setDuration(100).start();
            } else {
                isFolded = true;
                getChildFragmentManager().beginTransaction().remove(sleepOnsetFragment).commit();
                onsetView.setBackgroundResource(R.drawable.setting_stroke);
                ObjectAnimator.ofFloat(onsetButton, View.ROTATION, 90f, 0f).setDuration(100).start();
            }
        });
        /*
        //Test for time setting
        Button timeSettingButton = v.findViewById(R.id.timeSettingButton);
        timeSettingButton.setOnClickListener(view -> {
            WhenSleepFragment whenSleepFragment = new WhenSleepFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, whenSleepFragment).commit();
            mainActivity.setGoneBottomNavi();
        });

         */
        return v;
    }
}