package com.kael21ce.sleepanalysisandroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    SimpleDateFormat sdfDateTime = new SimpleDateFormat( "hh:mm a", Locale.KOREA);
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();
        Log.v("Created home", "HOMMMEE");

        TextView startTime = (TextView) v.findViewById(R.id.StartTimeHome);
        TextView endTime = (TextView) v.findViewById(R.id.EndTimeHome);

        ImageButton sleepButton = v.findViewById(R.id.sleepButtonHome);
        ImageButton napButton = v.findViewById(R.id.napButtonHome);
        ImageButton workButton = v.findViewById(R.id.workButtonHome);
        sleepButton.setOnClickListener(v1 -> sleepButtonClick(v1, mainActivity, startTime, endTime));
        napButton.setOnClickListener(v1 -> napButtonClick(v1, mainActivity, startTime, endTime));
        workButton.setOnClickListener(v1 -> workButtonClick(v1, mainActivity, startTime, endTime));

        //we use connection because fragment and activity is connected and we don't reuse fragment for other activity
        startTime.setText(sdfDateTime.format(new Date(mainActivity.getMainSleepStart())));
        endTime.setText(sdfDateTime.format(new Date(mainActivity.getMainSleepEnd())));

        return v;
    }

    public void sleepButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime)
    {
        startTime.setText(sdfDateTime.format(new Date(mainActivity.getMainSleepStart())));
        endTime.setText(sdfDateTime.format(new Date(mainActivity.getMainSleepStart())));
    }
    public void napButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime)
    {
        startTime.setText(sdfDateTime.format(new Date(mainActivity.getNapSleepStart())));
        endTime.setText(sdfDateTime.format(new Date(mainActivity.getNapSleepEnd())));
    }

    public void workButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime)
    {
        startTime.setText(sdfDateTime.format(new Date(mainActivity.getWorkOnset())));
        endTime.setText(sdfDateTime.format(new Date(mainActivity.getWorkOffset())));
    }
}