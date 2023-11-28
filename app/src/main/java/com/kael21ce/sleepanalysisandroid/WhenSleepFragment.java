package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class WhenSleepFragment extends Fragment {

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

        return v;
    }
}