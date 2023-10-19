package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.kael21ce.sleepanalysisandroid.data.SleepDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecommendFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy"+ "HH:mm", Locale.KOREA);
//        long sleepOnset = AppDatabase.sleepOnset;
//        String test = sdfDateTime.format(new Date(sleepOnset));
//        Log.v("tag_test", test);
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        long sleepOnset = sharedPref.getLong("sleepOnset", System.currentTimeMillis());
        String test = sdfDateTime.format(new Date(sleepOnset));
        Log.v("tag_test", test);

        AppDatabase db = Room.databaseBuilder(getActivity(),
                AppDatabase.class, "sleep_wake").allowMainThreadQueries().build();
        SleepDao sleepDao = db.sleepDao();
        List<Sleep> sleeps = sleepDao.getAll();
        for(Sleep sleep: sleeps){
            String sleepStart = sdfDateTime.format(new Date(sleep.sleepStart));
            String sleepEnd = sdfDateTime.format(new Date(sleep.sleepEnd));
            Log.v("theSleepR", sleepStart);
            Log.v("theSleepR2", sleepEnd);
        }


        View v = inflater.inflate(R.layout.fragment_recommend, container, false);
        return v;
    }
}