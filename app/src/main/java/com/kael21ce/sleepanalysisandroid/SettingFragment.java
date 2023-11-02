package com.kael21ce.sleepanalysisandroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kael21ce.sleepanalysisandroid.data.Sleep;

public class SettingFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);
        LinearLayout onsetView = v.findViewById(R.id.OnsetView);
        onsetView.setOnClickListener(view -> {
            SleepOnsetFragment sleepOnsetFragment = new SleepOnsetFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.SettingView, sleepOnsetFragment).commit();
        });
        return v;
    }
}