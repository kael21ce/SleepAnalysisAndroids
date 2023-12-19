package com.kael21ce.sleepanalysisandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.kael21ce.sleepanalysisandroid.data.Awareness;
import com.kael21ce.sleepanalysisandroid.data.Sleep;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    SimpleDateFormat sdfDateTime = new SimpleDateFormat( "hh:mm a", Locale.KOREA);
    SimpleDateFormat sdfDateTime2 = new SimpleDateFormat( "dd/MM/yyyy hh:mm a", Locale.KOREA);
    SimpleDateFormat sdfDateTimeRecomm = new SimpleDateFormat("a hh:mm", Locale.KOREA);
    SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd", Locale.KOREA);
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.KOREA);
    long now, nineHours;
    String mainSleepStartString, mainSleepEndString, workOnsetString, workOffsetString, napSleepStartString, napSleepEndString, sleepOnsetString;
    private List<Awareness> awarenesses;

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        MainActivity mainActivity = (MainActivity)getActivity();

        //Survey Caption
        LinearLayout SurveyUpperView = v.findViewById(R.id.SurveyUpperView);
        TextView surveyDescription = v.findViewById(R.id.surveyDescription);
        ImageButton surveyUpperButton = v.findViewById(R.id.surveyUpperButton);

        //ClockView
        TextView startTime = (TextView) v.findViewById(R.id.StartTimeHome);
        TextView endTime = (TextView) v.findViewById(R.id.EndTimeHome);
        ImageButton sleepButton = v.findViewById(R.id.sleepButtonHome);
        ImageButton napButton = v.findViewById(R.id.napButtonHome);
        ImageButton workButton = v.findViewById(R.id.workButtonHome);
        TextView sleepImportanceText = v.findViewById(R.id.sleepImprotanceHomeText);
        TextView sleepTypeText = v.findViewById(R.id.sleepTypeHomeText);
        TextView stateDescriptionText = v.findViewById(R.id.StateDescriptionHomeText);
        TextView stateDescriptionSmallText = v.findViewById(R.id.StateDescriptionHomeSmallText);
        ImageView stateDescriptionImage = v.findViewById(R.id.StateDescriptionHomeImage);
        ClockView clockView = v.findViewById(R.id.sweepingClockHome);
        LinearLayout RecommendHomeView = v.findViewById(R.id.RecommendHomeView);

        //Alertness Graph
        BarChart alertnessChart = v.findViewById(R.id.alertnessChart);
        TextView AlertnessText = v.findViewById(R.id.AlertnessText);
        LinearLayout AlertnessHomeView = v.findViewById(R.id.AlertnessHomeView);

        //Weekly alertness chart
        RecyclerView chartRecycler = v.findViewById(R.id.ChartRecyclerView);
        TextView positiveTimeText = v.findViewById(R.id.positiveTimeText);
        TextView negativeTimeText = v.findViewById(R.id.negativeTimeText);
        ImageView positiveImage = v.findViewById(R.id.positiveImage);
        ImageView negativeImage = v.findViewById(R.id.negativeImage);
        TextView positiveNumberText = v.findViewById(R.id.positiveNumberText);
        TextView negativeNumberText = v.findViewById(R.id.negativeNumberText);
        LinearLayout ChartHomeView = v.findViewById(R.id.ChartHomeView);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        String user_name = sharedPref.getString("User_Name", "UserName");

        //No data
        LinearLayout homeNoDataView = v.findViewById(R.id.homeNoDataView);
        TextView homeNoDataDescription = v.findViewById(R.id.homeNoDataDescription);
        ImageButton toRecommendButton = v.findViewById(R.id.toRecommendButton);
        ImageView no_data = v.findViewById(R.id.no_data_home);
        Glide.with(v.getContext()).load(R.raw.no_data).into(no_data);

        homeNoDataDescription.setText(user_name + "님의 수면 추천 정보가 없어요");
        toRecommendButton.setOnClickListener(view -> {
            RecommendFragment recommendFragment = new RecommendFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, recommendFragment).commit();
            mainActivity.setBottomNaviItem(R.id.tabRecommend);
        });

        if (sharedPref.contains("sleepOnset") && sharedPref.contains("workOnset") && sharedPref.contains("workOffset")) {
            homeNoDataView.setVisibility(View.GONE);
            SurveyUpperView.setVisibility(View.VISIBLE);
            RecommendHomeView.setVisibility(View.VISIBLE);
            AlertnessHomeView.setVisibility(View.VISIBLE);
            ChartHomeView.setVisibility(View.VISIBLE);
        } else {
            homeNoDataView.setVisibility(View.VISIBLE);
            SurveyUpperView.setVisibility(View.GONE);
            RecommendHomeView.setVisibility(View.GONE);
            AlertnessHomeView.setVisibility(View.GONE);
            ChartHomeView.setVisibility(View.GONE);
        }

        nineHours = (1000*60*60*9);
        now = System.currentTimeMillis();

        //Survey description and button: move to SurveyActivity
        surveyDescription.setText(user_name + "님의 상태를 알려주세요");
        surveyUpperButton.setOnClickListener(view -> {
            Intent surveyIntent = new Intent(v.getContext(), SurveyActivity.class);
            startActivity(surveyIntent);
        });

        //Initial button color setting
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));


        sleepButton.setOnClickListener(v1 -> sleepButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        napButton.setOnClickListener(v1 -> napButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        workButton.setOnClickListener(v1 -> workButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        clockView.setTypeOfInterval(1);
        mainSleepStartString = sdfTime.format(new Date(mainActivity.getMainSleepStart()));
        mainSleepEndString = sdfTime.format(new Date(mainActivity.getMainSleepEnd()));
        napSleepStartString = sdfTime.format(new Date(mainActivity.getNapSleepStart()));
        napSleepEndString = sdfTime.format(new Date(mainActivity.getNapSleepEnd()));
        workOnsetString = sdfTime.format(new Date(mainActivity.getWorkOnset()));
        workOffsetString = sdfTime.format(new Date(mainActivity.getWorkOffset()));
        sleepOnsetString = sdfDateTime.format(new Date(mainActivity.getSleepOnset()));

        Log.v("sleep onset", sdfDateTime2.format(new Date(sharedPref.getLong("sleepOnset", now))));
        Log.v("work onset", sdfDateTime2.format(new Date(sharedPref.getLong("workOnset", now))));
        Log.v("work offset", sdfDateTime2.format(new Date(sharedPref.getLong("workOffset", now))));

        //Just Example
        clockView.setAngleFromTime(mainSleepStartString, mainSleepEndString);

        //we use connection because fragment and activity is connected and we don't reuse fragment for other activity
        //startTime.setText(mainSleepStartString);
        //endTime.setText(mainSleepEndString);
        startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepStart())));
        endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepEnd())));

        //Graph showing alertness
        //Change the alertnessDescription

        TextView alertnessDescription = v.findViewById(R.id.AlertnessDescription);
        TextView alertnessTitle = v.findViewById(R.id.AlertnessRecommend);
        CurrentMarker mv = new CurrentMarker(v.getContext(), R.layout.current_marker, alertnessChart);

        //BarDataSet 1: alertness
        ArrayList<BarEntry> barEntries = mainActivity.getBarEntries();
        Log.v("BarEntries", String.valueOf(barEntries.size()));
            //Add data to Entries: form-(x: time, y: alertness value)
            //time range: 0 ~ 48 (- 24 + current , current + 24) / alertness range: -100 ~ 100
        //Set the color of bar depending on the y-value
        ArrayList<Integer> barColors = new ArrayList<>();
        for (int i = 0; i < barEntries.size(); i++) {
            if (barEntries.get(i).getY() > 0f) {
                if (barEntries.get(i).getX() >= 23.92f && barEntries.get(i).getX() <= 24.08f) {
                    barColors.add(ResourcesCompat.getColor(getResources(), R.color.blue_1, null));
                } else {
                    barColors.add(ResourcesCompat.getColor(getResources(), R.color.green_2, null));
                }
            } else {
                if (barEntries.get(i).getX() >= 23.92f && barEntries.get(i).getX() <= 24.08f) {
                    barColors.add(ResourcesCompat.getColor(getResources(), R.color.blue_1, null));
                } else {
                    barColors.add(ResourcesCompat.getColor(getResources(), R.color.red_2, null));
                }
            }
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "Alertness");
        barDataSet.setColors(barColors);
        barDataSet.setHighlightEnabled(true);
        barDataSet.setDrawValues(false);
        barDataSet.setHighLightAlpha(0);

        //BarDataSet 2: Recommended sleep interval
        String recommendedOnset = sdfTime.format(new Date(mainActivity.getMainSleepStart()));
        String recommendedOffset = sdfTime.format(new Date(mainActivity.getMainSleepEnd()));
        float rOnsetF = mv.timeToX(recommendedOnset);
        float rOffsetF = mv.timeToX(recommendedOffset);
        float rMidF = (rOnsetF + rOffsetF) / 2f;
        ArrayList sleepIntervalEntries1 = new ArrayList<BarEntry>();
        ArrayList sleepIntervalEntries2 = new ArrayList<BarEntry>();
        int[] rBarColors = new int[barEntries.size()];
        for (int i = 0; i < barEntries.size(); i++) {
            if (barEntries.get(i).getX() >= rOnsetF && barEntries.get(i).getX() <= rOffsetF) {
                sleepIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), -90f));
                sleepIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 90f));
            } else {
                sleepIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), 0f));
                sleepIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 0f));
            }
            rBarColors[i] = ResourcesCompat.getColor(getResources(), R.color.black, null);
        }
        BarDataSet sleepSet1 = new BarDataSet(sleepIntervalEntries1, "Sleep_Interval_1");
        BarDataSet sleepSet2 = new BarDataSet(sleepIntervalEntries2, "Sleep_Interval_2");
        sleepSet1.setColors(rBarColors, 20);
        sleepSet2.setColors(rBarColors, 20);

        //BarDataSet 3: Work interval
        String workOnset = sdfTime.format(new Date(mainActivity.getWorkOnset()));
        String workOffset = sdfTime.format(new Date(mainActivity.getWorkOffset()));
        float wOnsetF = mv.timeToX(workOnset);
        float wOffsetF = mv.timeToX(workOffset);
        float wMidF = 0f;
        if (wOnsetF >= wOffsetF) {
            wMidF = (wOnsetF + 48f) / 2f;
        } else {
            wMidF = (wOnsetF + wOffsetF) / 2f;
        }
        ArrayList workIntervalEntries1 = new ArrayList<BarEntry>();
        ArrayList workIntervalEntries2 = new ArrayList<BarEntry>();
        int[] wBarColors = new int[barEntries.size()];
        for (int i = 0; i < barEntries.size(); i++) {
            if (barEntries.get(i).getX() >= wOnsetF) {
                if (wOnsetF >= wOffsetF) {
                    //work offset > current time + 24h
                    workIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), -90f));
                    workIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 90f));
                } else {
                    if (barEntries.get(i).getX() <= wOffsetF) {
                        workIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), -90f));
                        workIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 90f));
                    } else {
                        workIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), 0f));
                        workIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 0f));
                    }
                }
            } else {
                workIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), 0f));
                workIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 0f));
            }
            wBarColors[i] = ResourcesCompat.getColor(getResources(), R.color.yellow_1, null);
        }
        BarDataSet workSet1 = new BarDataSet(workIntervalEntries1, "Work_Interval_1");
        BarDataSet workSet2 = new BarDataSet(workIntervalEntries2, "Work_Interval_2");
        workSet1.setColors(wBarColors, 20);
        workSet2.setColors(wBarColors, 20);

        //Set the bar data
        BarData barData = new BarData(barDataSet, sleepSet1, sleepSet2, workSet1, workSet2);

        //BarDataSet 4: Last sleep interval
        List<Sleep> sleeps = mainActivity.getSleeps();
        float alertnessPhaseChange = 49f;
        float lMidF = 0f;
        if (sleeps.size() > 0) {
            Sleep lastSleep = sleeps.get(sleeps.size() - 1);
            String lastOnset = sdfTime.format(new Date(lastSleep.sleepStart));
            String lastOffset = sdfTime.format(new Date(lastSleep.sleepEnd));
            float lOnsetF = mv.pastTimeToX(lastOnset);
            float lOffsetF = mv.pastTimeToX(lastOffset);
            lMidF = (lOnsetF + lOffsetF) / 2f;
            Log.v("LastSleep", "Onset: " + lOnsetF + " / Offset: " + lOffsetF);
            ArrayList lastIntervalEntries1 = new ArrayList<BarEntry>();
            ArrayList lastIntervalEntries2 = new ArrayList<BarEntry>();
            int[] lBarColors = new int[barEntries.size()];
            for (int i = 0; i < barEntries.size(); i++) {
                if (barEntries.get(i).getX() >= lOnsetF && barEntries.get(i).getX() <= lOffsetF) {
                    lastIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), -90f));
                    lastIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 90f));
                } else {
                    lastIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), 0f));
                    lastIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 0f));
                }
                lBarColors[i] = ResourcesCompat.getColor(getResources(), R.color.blue_1, null);
                //Theoretically recommended sleep onset
                if (i < barEntries.size() - 1) {
                    if (barEntries.get(i).getY() >= 0 && barEntries.get(i + 1).getY() <= 0) {
                        if (barEntries.get(i).getX() >= lOffsetF) {
                            alertnessPhaseChange = barEntries.get(i).getX();
                        }
                    }
                }
            }
            BarDataSet lastSet1 = new BarDataSet(lastIntervalEntries1, "Last_Interval_1");
            BarDataSet lastSet2 = new BarDataSet(lastIntervalEntries2, "Last_Interval_2");
            lastSet1.setColors(lBarColors, 20);
            lastSet2.setColors(lBarColors, 20);

            barData.addDataSet(lastSet1);
            barData.addDataSet(lastSet2);
        }


        //Set the width of each bar
        barData.setBarWidth(0.2f);
        alertnessChart.setData(barData);

        //Legend
        Legend alertnessLegend = alertnessChart.getLegend();
        alertnessLegend.setEnabled(false);
        alertnessChart.setTouchEnabled(true);

        //Showing window
        alertnessChart.setScaleEnabled(false);
        alertnessChart.setPinchZoom(true);
        alertnessChart.setVisibleXRangeMaximum(10f);
        alertnessChart.setFitBars(true);

        //Customize the grid
        XAxis xAxis = alertnessChart.getXAxis();
        YAxis leftYAxis = alertnessChart.getAxisLeft();
        YAxis rightYAxis = alertnessChart.getAxisRight();
        rightYAxis.setEnabled(false);

        //x axis on the top
        XAxisValueFormatter xAxisValueFormatter = new XAxisValueFormatter();
        xAxis.setValueFormatter(xAxisValueFormatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(2f);
        xAxis.setLabelCount(barEntries.size(), true);
        Log.v("Size", String.valueOf(barEntries.size()));

        //y axis on the left
        leftYAxis.setDrawAxisLine(true);

        //Customize the description
        Description description = new Description();
        description.setText("");
        alertnessChart.setDescription(description);
        //Customize the grid lines
        customizeGridLine(alertnessChart);
        //Customize the font of labels
        customizeLabel(v.getContext(), alertnessChart);

        //Set the time of alertnessText
        String originString = sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepStart()));
        AlertnessText.setText("오늘의 권장 취침 시각은 " + originString + " 입니다");
        //Set MarkerView
        String inputOnset = sdfTime.format(new Date(mainActivity.getSleepOnset()));
        mv.setRecommendedTime(recommendedOnset);
        mv.setInputTime(inputOnset);
        mv.setIntervalFloat(rMidF, wMidF, lMidF);
        mv.setAlertnessPhaseChange(alertnessPhaseChange);
        mv.setChartView(alertnessChart);
        alertnessChart.setMarker(mv);
        //Set the Highlight
        //If the recommended onset and input onset exhibits little difference,
        //only display the recommended onset
        if (alertnessPhaseChange != 49f) {
            if (Math.abs(alertnessPhaseChange - mv.timeToX(inputOnset)) > 0.1) {
                Highlight[] highlights = new Highlight[] {
                        new Highlight(24f, 0, -1),
                        new Highlight(alertnessPhaseChange, 0, -1),
                        new Highlight(mv.timeToX(inputOnset), 0, -1),
                        new Highlight(rMidF, 0, -1),
                        new Highlight(wMidF, 0, -1),
                        new Highlight(lMidF, 0, -1)
                };
                alertnessChart.highlightValues(highlights);
            } else {
                Highlight[] highlights = new Highlight[] {
                        new Highlight(24f, 0, -1),
                        new Highlight(mv.timeToX(inputOnset), 0, -1),
                        new Highlight(rMidF, 0, -1),
                        new Highlight(wMidF, 0, -1),
                        new Highlight(lMidF, 0, -1)
                };
                alertnessChart.highlightValues(highlights);
            }
        } else {
            Highlight[] highlights = new Highlight[] {
                    new Highlight(24f, 0, -1),
                    new Highlight(mv.timeToX(inputOnset), 0, -1),
                    new Highlight(rMidF, 0, -1),
                    new Highlight(wMidF, 0, -1),
                    new Highlight(lMidF, 0, -1)
            };
            alertnessChart.highlightValues(highlights);
        }
        //Change the description depending on current alertness
        if (barEntries.size() > 0) {
            float currentAlertness = barEntries.get(288).getY();
            if (currentAlertness >= 0) {
                alertnessTitle.setText("집중하기 좋은 상태에요");
                alertnessDescription.setText(user_name + "님의 각성도가 높아요");
            } else {
                alertnessTitle.setText("잠시 바람 쐬는 건 어때요?");
                alertnessDescription.setText(user_name + "님의 각성도가 낮아요");
            }
        } else {
            alertnessTitle.setText("잠시 바람 쐬는 건 어때요?");
            alertnessDescription.setText(user_name + "님의 각성도가 낮아요");
        }

        alertnessChart.invalidate();

        //Weekly chart
        //Set the ChartDescription
        TextView chartDescription = v.findViewById(R.id.ChartDescription);
        chartDescription.setText("이번주 " + user_name + "님의 각성도에요");

        //Load LinearLayoutManager and BarAdapter for ChartRecyclerView
        LinearLayoutManager chartLinearLayoutManager =
                new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
        chartRecycler.setLayoutManager(chartLinearLayoutManager);
        BarAdapter barAdapter = new BarAdapter();

        awarenesses = mainActivity.getAwarenesses();
        List<Awareness> weeklyAwareness = new ArrayList<>();
        long oneDayToMils = 1000*60*60*24;
        long curTime = now;
        long curDay = (curTime)/oneDayToMils;
        int i = 0;
        for(Awareness awareness: awarenesses){
            Log.v("AWARENESS VALUE IN SCHEDULE", sdfDate.format(new Date(awareness.awarenessDay*oneDayToMils+oneDayToMils)));

            long hourGoodDuration = awareness.goodDuration/60;
            long minuteGoodDuration = awareness.goodDuration%60;
            long hourBadDuration = awareness.badDuration/60;
            long minuteBadDuration = awareness.badDuration%60;
            Log.v("AWARENESS VALUE IN SCHEDULE", hourGoodDuration + ":" + minuteGoodDuration);
            Log.v("AWARENESS VALUE IN SCHEDULE", hourBadDuration + ":" + minuteBadDuration);
            if(curDay-8 < awareness.awarenessDay){
                String date = sdfDate.format(new Date((awareness.awarenessDay)*oneDayToMils));
                String goodDuration = hourGoodDuration + ":" + minuteGoodDuration;
                String badDuration = hourBadDuration + ":" + minuteBadDuration;
                //Add to get ONLY weekly awareness
                weeklyAwareness.add(i, awareness);
                barAdapter.addItem(new Bar(date, convertToWeight(goodDuration), convertToWeight(badDuration)));
                i++;
            }
        }
        //When HomeFragment appears
        if (weeklyAwareness.size() == 8) {
            Awareness todayAwareness = weeklyAwareness.get(7);
            long todayHourGoodDuration = todayAwareness.goodDuration/60;
            long todayMinuteGoodDuration = todayAwareness.goodDuration%60;
            long todayHourBadDuration = todayAwareness.badDuration/60;
            long todayMinuteBadDuration = todayAwareness.badDuration%60;
            positiveTimeText.setText(todayHourGoodDuration + "시간 " + todayMinuteGoodDuration + "분");
            negativeTimeText.setText(todayHourBadDuration + "시간 " + todayMinuteBadDuration + "분");
            setDeltaAwareness(positiveNumberText, positiveImage, negativeNumberText, negativeImage,
                    7, weeklyAwareness);
        }

        //Load the clicked position in RecyclerView
        barAdapter.setOnBarClickListener(position -> {
            Log.d("HomeFragment", "Item clicked at position " + position);
            Awareness clickedAwareness = weeklyAwareness.get(position);
            long hourGoodDuration = clickedAwareness.goodDuration/60;
            long minuteGoodDuration = clickedAwareness.goodDuration%60;
            long hourBadDuration = clickedAwareness.badDuration/60;
            long minuteBadDuration = clickedAwareness.badDuration%60;
            //Time for positive
            positiveTimeText.setText(hourGoodDuration + "시간 " + minuteGoodDuration + "분");
            //Time for negative
            negativeTimeText.setText(hourBadDuration + "시간 " + minuteBadDuration + "분");
            //Number for increase and decrease

            setDeltaAwareness(positiveNumberText, positiveImage, negativeNumberText, negativeImage,
                    position, weeklyAwareness);
        });

        chartRecycler.setAdapter(barAdapter);

        return v;
    }

    public void sleepButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                                 ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                                 TextView sleepTypeText, TextView sleepImportanceText,
                                 TextView stateDescriptionText, TextView stateDescriptionSmallText,
                                 ImageView stateDescriptionImage, ClockView clockView)
    {
        stateDescriptionSmallText.setVisibility(View.VISIBLE);
        startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepStart())));
        endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepEnd())));
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
        stateDescriptionText.setText("잠을 자기 좋은 시간이에요");
        stateDescriptionSmallText.setText("기상시간을 지켜주세요");
        stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sleep, null));
        //Change the clock angle using setAngle and color using setTypeOfInterval
        clockView.setTypeOfInterval(1);
        //Just example
        if(!mainSleepStartString.equals(mainSleepEndString)) {
            clockView.setIsRecommended(true);
            clockView.setAngleFromTime(mainSleepStartString, mainSleepEndString);
        }else{
            clockView.setIsRecommended(false);
        }
    }

    public void napButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                               ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                               TextView sleepTypeText, TextView sleepImportanceText,
                               TextView stateDescriptionText, TextView stateDescriptionSmallText,
                               ImageView stateDescriptionImage, ClockView clockView)
    {
        stateDescriptionSmallText.setVisibility(View.VISIBLE);
        startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getNapSleepStart())));
        endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getNapSleepEnd())));
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
        stateDescriptionText.setText("낮잠을 주무세요");
        stateDescriptionSmallText.setText("맑은 정신을 유지할 수 있어요");
        stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sleep, null));
        clockView.setTypeOfInterval(2);
        //Just example
        if(!napSleepStartString.equals(napSleepEndString)) {
            clockView.setIsRecommended(true);
            clockView.setAngleFromTime(napSleepStartString, napSleepEndString);
        }else{
            clockView.setIsRecommended(false);
            stateDescriptionText.setText("낮잠이 필요하지 않아요");
            stateDescriptionSmallText.setText("낮잠 없이도 맑은 정신을 유지할 수 있어요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.laugh, null));
            startTime.setText("--:--");
            endTime.setText("--:--");
        }
    }

    public void workButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                                ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                                TextView sleepTypeText, TextView sleepImportanceText,
                                TextView stateDescriptionText, TextView stateDescriptionSmallText,
                                ImageView stateDescriptionImage, ClockView clockView)
    {
        stateDescriptionSmallText.setVisibility(View.GONE);
        startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getWorkOnset())));
        endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getWorkOffset())));
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
        stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.smile, null));
        clockView.setTypeOfInterval(3);
        //Just example
        clockView.setIsRecommended(true);
        clockView.setAngleFromTime(workOnsetString, workOffsetString);
    }

    //Customize the grid line
    private void customizeGridLine(BarChart barChart) {
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(true);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setGridColor(ResourcesCompat.getColor(getResources(), R.color.gray_4, null));
        xAxis.setAxisLineColor(ResourcesCompat.getColor(getResources(), R.color.gray_4, null));

        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.enableGridDashedLine(10f, 10f, 0f);
        yAxis.setAxisLineColor(ResourcesCompat.getColor(getResources(), R.color.gray_4, null));
        yAxis.setGridColor(ResourcesCompat.getColor(getResources(), R.color.gray_4, null));
        //Set the displaying range
        yAxis.setAxisMinimum(-100f);
        yAxis.setAxisMaximum(100f);
    }

    //Customize the label
    private void customizeLabel(Context context, BarChart barChart) {
        XAxis xAxis = barChart.getXAxis();
        YAxis yAxis = barChart.getAxisLeft();
        Typeface customTypeface = ResourcesCompat.getFont(context, R.font.pretendardbold);
        xAxis.setTypeface(customTypeface);
        xAxis.setTextColor(R.color.black);
        yAxis.setTypeface(customTypeface);
        yAxis.setTextColor(R.color.black);
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

    //Set the difference of awareness interval
    public void setDeltaAwareness(TextView positiveNumberText, ImageView positiveImage, TextView negativeNumberText,
                                   ImageView negativeImage, int position, List<Awareness> weeklyAwareness) {
        Awareness clickedAwareness = weeklyAwareness.get(position);
        if (position == 0) {
            positiveNumberText.setText("-");
            positiveNumberText.setTextColor(getResources().getColor(R.color.black, null));
            positiveImage.setVisibility(View.INVISIBLE);
            negativeNumberText.setText("-");
            positiveNumberText.setTextColor(getResources().getColor(R.color.black, null));
            negativeImage.setVisibility(View.INVISIBLE);
        } else {
            positiveImage.setVisibility(View.VISIBLE);
            negativeImage.setVisibility(View.VISIBLE);
            Awareness formerAwareness = weeklyAwareness.get(position - 1);
            long deltaGood = clickedAwareness.goodDuration - formerAwareness.goodDuration;
            long deltaBad = clickedAwareness.badDuration - formerAwareness.badDuration;
            long deltaGoodHour = Math.abs(deltaGood)/60;
            long deltaGoodMinute = Math.abs(deltaGood)%60;
            long deltaBadHour = Math.abs(deltaBad)/60;
            long deltaBadMinute = Math.abs(deltaBad)%60;
            String deltaGoodString = deltaGoodHour + "시간 " + deltaGoodMinute + "분";
            String deltaBadString = deltaBadHour + "시간 " + deltaBadMinute + "분";
            //Positive Number
            if (deltaGood > 0) {
                positiveNumberText.setText(deltaGoodString);
                positiveNumberText.setTextColor(getResources().getColor(R.color.green_1, null));
                positiveImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.arrow_up, null));
            } else {
                positiveNumberText.setText(deltaGoodString);
                positiveNumberText.setTextColor(getResources().getColor(R.color.red_1, null));
                positiveImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.arrow_down, null));
            }
            //Negative Number
            if (deltaBad > 0) {
                negativeNumberText.setText(deltaBadString);
                negativeNumberText.setTextColor(getResources().getColor(R.color.green_1, null));
                negativeImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.arrow_up, null));
            } else {
                negativeNumberText.setText(deltaBadString);
                negativeNumberText.setTextColor(getResources().getColor(R.color.red_1, null));
                negativeImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.arrow_down, null));
            }
        }
    }
}

//Axis value formatter for x-axis in alertnessChart
class XAxisValueFormatter extends ValueFormatter {
    /*Range
    Each hour is represented by float and integer
    The range of time for showing alertness is [(today current time) - 24, (today current time) + 24]
    */
    //To make displayed tick label not overlap
    String displayedDate = "";
    String displayedTime = "";
    @Override
    public String getFormattedValue(float value) {
        String timeLabel;
        String dateLabel;
        String label;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/d");
        LocalTime current = LocalTime.now();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        int currentHour = current.getHour();
        int currentMinute = current.getMinute();
        float valueReal = value;
        float valueCentered = valueReal + currentHour + currentMinute / 60f;
        float currentCenter = 24f;
        Log.v("Ticks", currentCenter + " compared to " + value);
        int valueR = Math.round(valueCentered);
        int quotient = valueR / 24;
        int remainder = valueR % 24;
        //Calculate time
        if (remainder < 12) {
            timeLabel = remainder + " AM";
        } else if (remainder == 12) {
            timeLabel = remainder + " PM";
        } else {
            timeLabel = remainder - 12 + " PM";
        }
        //Calculate date
        if (valueCentered < 23f) {
            dateLabel = yesterday.format(formatter);
        } else if (valueCentered >= 23f && valueCentered < 47f) {
            dateLabel = today.format(formatter);
        } else {
            dateLabel = tomorrow.format(formatter);
        }
        //Set the displaying of label
        label = dateLabel + " " + timeLabel;
        if (!timeLabel.equals(displayedTime)) {
            //Only show multiples of 3
            if (remainder % 3 == 0) {
                //If date is not same as before
                if (!dateLabel.equals(displayedDate)) {
                    displayedTime = timeLabel;
                    displayedDate = dateLabel;
                    return label;
                } else {
                    displayedTime = timeLabel;
                    return label;
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
}