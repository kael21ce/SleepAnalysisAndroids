package com.kael21ce.sleepanalysisandroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class IntervalFragment extends Fragment {

    public ImageButton intervalPlusButton;
    public AddIntervalFragment addIntervalFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_interval, container, false);

        intervalPlusButton = v.findViewById(R.id.intervalPlusButton);

        //Add sleep interval
        addIntervalFragment = new AddIntervalFragment();
        intervalPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction().replace(R.id.IntervalFrame, addIntervalFragment).commit();
            }
        });
        return v;
    }
}