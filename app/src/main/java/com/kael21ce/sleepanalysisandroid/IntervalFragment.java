package com.kael21ce.sleepanalysisandroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class IntervalFragment extends Fragment {

    public ImageButton intervalPlusButton;
    public AddIntervalFragment addIntervalFragment;
    public RecyclerView intervalRecyclerView;
    public TextView AlertnessHighTimeText;
    public TextView AlertnessLowTimeText;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.KOREA);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_interval, container, false);

        intervalPlusButton = v.findViewById(R.id.intervalPlusButton);
        intervalRecyclerView = v.findViewById(R.id.IntervalRecyclerView);

        //Set layoutManager to recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(v.getContext(),
                LinearLayoutManager.VERTICAL, false);
        intervalRecyclerView.setLayoutManager(layoutManager);
        IntervalAdapter intervalAdapter = new IntervalAdapter(getParentFragmentManager());

        //get the bundle arguments
        Bundle bundle = this.getArguments();
        if(bundle == null){
            Log.v("bundle", "bundle failed to be fetched");
        }

        //update the awareness
        AlertnessHighTimeText = v.findViewById(R.id.AlertnessHighTimeText);
        AlertnessLowTimeText = v.findViewById(R.id.AlertnessLowTimeText);
        AlertnessHighTimeText.setText(String.valueOf(bundle.getLong("goodDuration")));
        AlertnessLowTimeText.setText(String.valueOf(bundle.getLong("badDuration")));

        //Add time interval to intervalRecyclerView (info about interval is get from ScheduleFragment!)
        int count = bundle.getInt("count");
        for(int i = 0; i < count; i ++){
            long sleepStart = bundle.getLong("sleepStart"+i);
            long sleepEnd = bundle.getLong("sleepEnd"+i);
            long duration = (sleepEnd - sleepStart)/1000;
            Log.v("DURATION", String.valueOf(sleepStart));
            Log.v("DURATION2", String.valueOf(sleepEnd));
//            intervalAdapter.addItem(new Interval(String.valueOf(duration), 2));
            intervalAdapter.addItem(new Interval(sdf.format(new Date(sleepStart)) + " " + sdf.format(new Date(sleepEnd)), 3));
        }
        //Just example
//        intervalAdapter.addItem(new Interval("1시간 0분", 2));
//        intervalAdapter.addItem(new Interval("5시간 0분", 3));
//        intervalAdapter.addItem(new Interval("8시간 58분", 2));
//        intervalAdapter.addItem(new Interval("0시간 15분", 1));

        //If there is no Item, not show IntervalListLayout
        if (intervalAdapter.getItemCount() == 0) {
            LinearLayout intervalListLayout = v.findViewById(R.id.IntervalListLayout);
            intervalListLayout.setVisibility(View.INVISIBLE);
        }
        intervalRecyclerView.setAdapter(intervalAdapter);

        //Add sleep interval if plus button is clicked
        addIntervalFragment = new AddIntervalFragment();
        addIntervalFragment.setArguments(bundle);
        intervalPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getParentFragmentManager().beginTransaction().replace(R.id.IntervalFrame, addIntervalFragment).commit();
            }
        });
        return v;
    }
}