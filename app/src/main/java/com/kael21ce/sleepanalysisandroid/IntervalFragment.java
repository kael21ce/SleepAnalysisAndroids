package com.kael21ce.sleepanalysisandroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;


public class IntervalFragment extends Fragment {

    public ImageButton intervalPlusButton;
    public AddIntervalFragment addIntervalFragment;
    public RecyclerView intervalRecyclerView;

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
        IntervalAdapter intervalAdapter = new IntervalAdapter();

        //Add time interval to intervalRecyclerView (info about interval is get from ScheduleFragment!)
        //Just example
        intervalAdapter.addItem(new Interval("1시간 0분", 2));
        intervalAdapter.addItem(new Interval("5시간 0분", 3));
        intervalAdapter.addItem(new Interval("8시간 58분", 2));
        intervalAdapter.addItem(new Interval("0시간 15분", 1));
        //
        intervalRecyclerView.setAdapter(intervalAdapter);

        //Add sleep interval if plus button is clicked
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