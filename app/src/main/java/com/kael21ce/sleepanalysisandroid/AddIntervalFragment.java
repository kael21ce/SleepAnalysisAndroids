package com.kael21ce.sleepanalysisandroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class AddIntervalFragment extends Fragment {

    public ImageButton backButton;
    public IntervalFragment intervalFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_interval, container, false);

        //Return to IntervalFragment
        intervalFragment = new IntervalFragment();
        backButton = v.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction().replace(R.id.IntervalFrame, intervalFragment).commit();
            }
        });

        return  v;
    }
}