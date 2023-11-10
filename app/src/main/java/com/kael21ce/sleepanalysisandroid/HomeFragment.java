package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.kael21ce.sleepanalysisandroid.data.Awareness;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    SimpleDateFormat sdfDateTime = new SimpleDateFormat( "hh:mm a", Locale.KOREA);
    SimpleDateFormat sdfDateTime2 = new SimpleDateFormat( "dd/MM/yyyy hh:mm a", Locale.KOREA);
    SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd", Locale.KOREA);
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.KOREA);
    String mainSleepStartString, mainSleepEndString, workOnsetString, workOffsetString, napSleepStartString, napSleepEndString, sleepOnsetString;
    private List<Awareness> awarenesses;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();

        TextView startTime = (TextView) v.findViewById(R.id.StartTimeHome);
        TextView endTime = (TextView) v.findViewById(R.id.EndTimeHome);

        ImageButton sleepButton = v.findViewById(R.id.sleepButtonHome);
        ImageButton napButton = v.findViewById(R.id.napButtonHome);
        ImageButton workButton = v.findViewById(R.id.workButtonHome);
        TextView sleepImportanceText = v.findViewById(R.id.sleepImprotanceHomeText);
        TextView sleepTypeText = v.findViewById(R.id.sleepTypeHomeText);
        TextView stateDescriptionText = v.findViewById(R.id.StateDescriptionHomeText);
        ClockView clockView = v.findViewById(R.id.sweepingClockHome);
        RecyclerView chartRecycler = v.findViewById(R.id.ChartRecyclerView);

        BarChart alertnessChart = v.findViewById(R.id.alertnessChart);

        //Initial button color setting
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));


        sleepButton.setOnClickListener(v1 -> sleepButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                clockView));
        napButton.setOnClickListener(v1 -> napButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                clockView));
        workButton.setOnClickListener(v1 -> workButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                clockView));
        clockView.setTypeOfInterval(1);
        mainSleepStartString = sdfTime.format(new Date(mainActivity.getMainSleepStart()));
        mainSleepEndString = sdfTime.format(new Date(mainActivity.getMainSleepEnd()));
        napSleepStartString = sdfTime.format(new Date(mainActivity.getNapSleepStart()));
        napSleepEndString = sdfTime.format(new Date(mainActivity.getNapSleepEnd()));
        workOnsetString = sdfTime.format(new Date(mainActivity.getWorkOnset()));
        workOffsetString = sdfTime.format(new Date(mainActivity.getWorkOffset()));
        sleepOnsetString = sdfDateTime.format(new Date(mainActivity.getSleepOnset()));

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        Log.v("sleep onset", sdfDateTime2.format(new Date(sharedPref.getLong("sleepOnset", System.currentTimeMillis()))));
        Log.v("work onset", sdfDateTime2.format(new Date(sharedPref.getLong("workOnset", System.currentTimeMillis()))));
        Log.v("work offset", sdfDateTime2.format(new Date(sharedPref.getLong("workOffset", System.currentTimeMillis()))));

        //Just Example
        clockView.setAngleFromTime(mainSleepStartString, mainSleepEndString);

        //we use connection because fragment and activity is connected and we don't reuse fragment for other activity
        startTime.setText(mainSleepStartString);
        endTime.setText(mainSleepEndString);

        //Graph showing alertness
        ArrayList<BarEntry> barEntries = new ArrayList<BarEntry>();
            //Add data to Entries: form-(x: time, y: alertness value)
            //time range: 0 ~ 72 (yesterday ~ tomorrow) / alertness range: -100 ~ 100
        //Just Example
        for (float i=0.25f; i < 0.25f*(48f * 4f + 1f); i += 0.25f) {
            barEntries.add(new BarEntry(5f + i, (float) (100*(0.7*Math.sin(i) + 0.3*Math.cos(i * 3)))));
        }
            //
        //Set the color of bar depending on the y-value
        ArrayList<Integer> barColors = new ArrayList<>();
        for (int i = 0; i < barEntries.size(); i++) {
            if (barEntries.get(i).getY() > 0f) {
                barColors.add(ResourcesCompat.getColor(getResources(), R.color.green_2, null));
            } else {
                barColors.add(ResourcesCompat.getColor(getResources(), R.color.red_2, null));
            }
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "Alertness");
        //Set the color of each bar
        barDataSet.setColors(barColors);
        barDataSet.setHighlightEnabled(false);
        //Set the width of each bar
        barDataSet.setDrawValues(false);
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.2f);
        alertnessChart.setData(barData);
        Legend alertnessLegend = alertnessChart.getLegend();
        alertnessLegend.setEnabled(false);
        alertnessChart.setTouchEnabled(true);
        //Showing window
        alertnessChart.setScaleEnabled(false);
        alertnessChart.setPinchZoom(true);
        alertnessChart.setVisibleXRangeMaximum(10f);
        //Customize the grid
        XAxis xAxis = alertnessChart.getXAxis();
        YAxis leftYAxis = alertnessChart.getAxisLeft();
        YAxis rightYAxis = alertnessChart.getAxisRight();
        leftYAxis.setDrawAxisLine(false);
        rightYAxis.setDrawAxisLine(false);
        xAxis.setGridColor(ResourcesCompat.getColor(getResources(), R.color.gray_4, null));
        xAxis.setValueFormatter(new XAxisValueFormatter());
        xAxis.setGranularity(20);
        xAxis.setLabelCount(barEntries.size() / 20, true);
        //Customize the description
        Description description = new Description();
        description.setText("");
        alertnessChart.setDescription(description);

        //Load LinearLayoutManager and BarAdapter for ChartRecyclerView
        LinearLayoutManager chartLinearLayoutManager =
                new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
        chartRecycler.setLayoutManager(chartLinearLayoutManager);
        BarAdapter barAdapter = new BarAdapter();

        awarenesses = mainActivity.getAwarenesses();
        long oneDayToMils = 1000*60*60*24;
        long curTime = System.currentTimeMillis();
        long curDay = curTime/oneDayToMils;
        for(Awareness awareness: awarenesses){
            Log.v("AWARENESS VALUE IN SCHEDULE", sdfDate.format(new Date(awareness.awarenessDay*oneDayToMils+oneDayToMils)));

            long hourGoodDuration = awareness.goodDuration/60;
            long minuteGoodDuration = awareness.goodDuration%60;
            long hourBadDuration = awareness.badDuration/60;
            long minuteBadDuration = awareness.badDuration%60;
            Log.v("AWARENESS VALUE IN SCHEDULE", hourGoodDuration + ":" + minuteGoodDuration);
            Log.v("AWARENESS VALUE IN SCHEDULE", hourBadDuration + ":" + minuteBadDuration);
            if(curDay-9 < awareness.awarenessDay){
                String date = sdfDate.format(new Date((awareness.awarenessDay+1)*oneDayToMils));
                String goodDuration = hourGoodDuration + ":" + minuteGoodDuration;
                String badDuration = hourBadDuration + ":" + minuteBadDuration;
                barAdapter.addItem(new Bar(date, convertToWeight(goodDuration), convertToWeight(badDuration)));
            }
        }

        //Set the bar items to BarAdapter (8 items)
        //Just Examples
        //You can convert the duration "HH:mm" to weight by using convertToWeight function in this fragment
//        barAdapter.addItem(new Bar("12/25", convertToWeight("12:00"), convertToWeight("3:00")));
//        barAdapter.addItem(new Bar("12/26", 124, 80));
//        barAdapter.addItem(new Bar("12/27", 50, 10));
//        barAdapter.addItem(new Bar("12/28", 20, 20));
//        barAdapter.addItem(new Bar("12/29", 80, 90));
//        barAdapter.addItem(new Bar("12/30", 10, 20));
//        barAdapter.addItem(new Bar("12/31", 60, 10));
//        barAdapter.addItem(new Bar("1/1", 80, 40));

        chartRecycler.setAdapter(barAdapter);

        return v;
    }

    public void sleepButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                                 ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                                 TextView sleepTypeText, TextView sleepImportanceText,
                                 TextView stateDescriptionText, ClockView clockView)
    {
        startTime.setText(sdfDateTime.format(new Date(mainActivity.getMainSleepStart())));
        endTime.setText(sdfDateTime.format(new Date(mainActivity.getMainSleepEnd())));
        //Change the color of buttons
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        napButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        workButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        //Change the content of displaying text
        sleepTypeText.setText("밤잠");
        sleepImportanceText.setText("중요");
        sleepImportanceText.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.important_caption, null));
        stateDescriptionText.setText("각성도가 낮아요!\n이 시간엔 꼭 주무세요");
        //Change the clock angle using setAngle and color using setTypeOfInterval
        clockView.setTypeOfInterval(1);
        //Just example
        clockView.setAngleFromTime(mainSleepStartString, mainSleepEndString);
    }

    public void napButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                               ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                               TextView sleepTypeText, TextView sleepImportanceText,
                               TextView stateDescriptionText, ClockView clockView)
    {
        startTime.setText(sdfDateTime.format(new Date(mainActivity.getNapSleepStart())));
        endTime.setText(sdfDateTime.format(new Date(mainActivity.getNapSleepEnd())));
        //Change the color of buttons
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        napButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        workButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        //Change the content of displaying text
        sleepTypeText.setText("낮잠");
        sleepImportanceText.setText("권장");
        sleepImportanceText.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.recommend_caption, null));
        stateDescriptionText.setText("이때 주무시면 덜 피곤할거에요");
        clockView.setTypeOfInterval(2);
        //Just example
        clockView.setAngleFromTime(napSleepStartString, napSleepEndString);
    }

    public void workButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                                ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                                TextView sleepTypeText, TextView sleepImportanceText,
                                TextView stateDescriptionText, ClockView clockView)
    {
        startTime.setText(sdfDateTime.format(new Date(mainActivity.getWorkOnset())));
        endTime.setText(sdfDateTime.format(new Date(mainActivity.getWorkOffset())));
        //Change the color of buttons
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        napButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        workButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        //Change the content of displaying text
        sleepTypeText.setText("활동");
        sleepImportanceText.setText("중요");
        sleepImportanceText.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.important_caption, null));
        stateDescriptionText.setText("가장 각성도가 높은 시간이에요");
        clockView.setTypeOfInterval(3);
        //Just example
        clockView.setAngleFromTime(workOnsetString, workOffsetString);
    }

    //Convert "HH:mm" to weight
    public int convertToWeight(String time) {
        if (time.length() > 5) {
            return 0;
        }
        int index = time.indexOf(":");
        if (index == -1) {
            return 0;
        }
        String hour = time.substring(0, index);
        String minute = time.substring(index + 1);
        int hourInt = Integer.parseInt(hour);
        int minuteInt = Integer.parseInt(minute);
        double totalTime = hourInt + (double) minuteInt / 60;

        return (int) ((124/24)*totalTime);
    }
}

//Axis value formatter for x-axis in alertnessChart
class XAxisValueFormatter extends ValueFormatter {
    /*Range
    Each hour is represented by float and integer
    Yesterday: 0 ~ 24
    Today: 24 ~ 48
    Tomorrow: 48 ~ 72
    The range of time for showing alertness is [(today current time) - 24, (today current time) + 24]
    */
    @Override
    public String getFormattedValue(float value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/d");
        int valueInt = (int) value;
        int quotient = valueInt / 24;
        int remainder = valueInt % 24;
        int remainderDisplaying = valueInt % 3;
        float valueF = (float) valueInt;
        Log.v("Ticks", String.valueOf(value));
        if (value < valueInt + 0.6) {
            if (remainderDisplaying == 1) {
                if (remainder < 12) {
                    return remainder + " AM";
                } else {
                    return remainder + " PM";
                }
            }
            if (remainder == 0) {
                if (quotient == 0) {
                    LocalDate yesterday = LocalDate.now().minusDays(1);
                    return yesterday.format(formatter);
                } else if (quotient == 1) {
                    LocalDate today = LocalDate.now();
                    return today.format(formatter);
                } else {
                    LocalDate tomorrow = LocalDate.now().plusDays(1);
                    return tomorrow.format(formatter);
                }
            }
            return "";
        } else {
            return "";
        }
    }
}