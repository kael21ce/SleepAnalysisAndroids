package com.kael21ce.sleepanalysisandroid;

import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kael21ce.sleepanalysisandroid.data.Sleep;

public class SettingFragment extends Fragment {

    Boolean isFolded = true;
    SleepOnsetFragment sleepOnsetFragment = new SleepOnsetFragment();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);

        LinearLayout onsetView = v.findViewById(R.id.OnsetView);
        ImageView onsetButton = v.findViewById(R.id.OnsetButton);
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
        return v;
    }
}