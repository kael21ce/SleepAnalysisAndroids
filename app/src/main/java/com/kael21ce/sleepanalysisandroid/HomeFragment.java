package com.kael21ce.sleepanalysisandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.kael21ce.sleepanalysisandroid.data.Awareness;
import com.kael21ce.sleepanalysisandroid.data.Sleep;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
    private List<Awareness> awarenesses, sleepAwarenesses;

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

        //Weekly sleep chart
        RecyclerView sleepChartRecycler = v.findViewById(R.id.SleepChartRecyclerView);
        TextView sleepPositiveTimeText = v.findViewById(R.id.sleepPositiveTimeText);
        TextView sleepNegativeTimeText = v.findViewById(R.id.sleepNegativeTimeText);
        ImageView sleepPositiveImage = v.findViewById(R.id.sleepPositiveImage);
        ImageView sleepNegativeImage = v.findViewById(R.id.sleepNegativeImage);
        TextView sleepPositiveNumberText = v.findViewById(R.id.sleepPositiveNumberText);
        TextView sleepNegativeNumberText = v.findViewById(R.id.sleepNegativeNumberText);
        LinearLayout SleepChartHomeView = v.findViewById(R.id.SleepChartHomeView);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String user_name = sharedPref.getString("User_Name", "UserName");

        //No onset, offset schedule data
        LinearLayout homeNoDataView = v.findViewById(R.id.homeNoDataView);
        ImageButton toRecommendButton = v.findViewById(R.id.toRecommendButton);
        TextView homeNoDataDescription = v.findViewById(R.id.homeNoDataDescription);
        ImageView no_data = v.findViewById(R.id.no_data_home);
        Glide.with(v.getContext()).load(R.raw.no_data).into(no_data);

        toRecommendButton.setOnClickListener(view -> {
            RecommendFragment recommendFragment = new RecommendFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, recommendFragment).commit();
            mainActivity.setBottomNaviItem(R.id.tabRecommend);
        });

        //No sleep data
        LinearLayout alertnessNoDataView = v.findViewById(R.id.alertnessNoDataView);
        ImageButton toScheduleButton = v.findViewById(R.id.alertnessToScheduleButton);
        ImageView no_data_alertness = v.findViewById(R.id.no_data_alertness);
        Glide.with(v.getContext()).load(R.raw.no_data).into(no_data_alertness);

        toScheduleButton.setOnClickListener(view -> {
            ScheduleFragment scheduleFragment = new ScheduleFragment();
            getParentFragmentManager().beginTransaction().replace(R.id.mainFrame, scheduleFragment).commit();
            mainActivity.setBottomNaviItem(R.id.tabSchedule);
        });

        //Check whether recommendation is hidden
        if (!sharedPref.contains("isHidden")) {
            editor.putBoolean("isHidden", true).apply();
        }
        boolean isHidden = false;

        List<Sleep> sleeps = mainActivity.getSleeps();

        if (sharedPref.contains("sleepOnset") && sharedPref.contains("workOnset") && sharedPref.contains("workOffset")) {
            if (sleeps != null && sleeps.size() > 0) {
                if (!isHidden) {
                    homeNoDataView.setVisibility(View.GONE);
                    SurveyUpperView.setVisibility(View.VISIBLE);
                    alertnessNoDataView.setVisibility(View.GONE);
                    RecommendHomeView.setVisibility(View.VISIBLE);
                    AlertnessHomeView.setVisibility(View.VISIBLE);
                    ChartHomeView.setVisibility(View.VISIBLE);
                    SleepChartHomeView.setVisibility(View.VISIBLE);
                } else {
                    homeNoDataView.setVisibility(View.GONE);
                    SurveyUpperView.setVisibility(View.VISIBLE);
                    alertnessNoDataView.setVisibility(View.GONE);
                    RecommendHomeView.setVisibility(View.GONE);
                    AlertnessHomeView.setVisibility(View.GONE);
                    ChartHomeView.setVisibility(View.GONE);
                    SleepChartHomeView.setVisibility(View.GONE);
                }
            } else {
                homeNoDataView.setVisibility(View.VISIBLE);
                alertnessNoDataView.setVisibility(View.VISIBLE);
                RecommendHomeView.setVisibility(View.GONE);
                AlertnessHomeView.setVisibility(View.GONE);
                ChartHomeView.setVisibility(View.GONE);
                SleepChartHomeView.setVisibility(View.GONE);

                //Change text in homeNoDataView
                homeNoDataDescription.setText("수면 기록을 추가해보세요");
            }
        } else {
            homeNoDataView.setVisibility(View.VISIBLE);
            SurveyUpperView.setVisibility(View.VISIBLE);
            alertnessNoDataView.setVisibility(View.GONE);
            RecommendHomeView.setVisibility(View.GONE);
            AlertnessHomeView.setVisibility(View.GONE);
            ChartHomeView.setVisibility(View.GONE);
            SleepChartHomeView.setVisibility(View.GONE);
        }

        nineHours = (1000*60*60*9);
        now = System.currentTimeMillis();

        //Survey description and button: move to SurveyActivity
        surveyDescription.setText(user_name + "님의 현재 상태를 알려주세요");
        SurveyUpperView.setOnClickListener(view -> {
            Intent surveyIntent = new Intent(v.getContext(), SurveyActivity.class);
            surveyIntent.putExtra("firstDone", 0);
            startActivity(surveyIntent);
        });
        surveyUpperButton.setOnClickListener(view -> {
            Intent surveyIntent = new Intent(v.getContext(), SurveyActivity.class);
            surveyIntent.putExtra("firstDone", 0);
            startActivity(surveyIntent);
        });

        //Move to SleepOnsetActivity
        ImageButton clockOnsetButton = v.findViewById(R.id.ClockOnsetButton);
        clockOnsetButton.setOnClickListener(view -> {
            Intent onsetIntent = new Intent(v.getContext(), SleepOnsetActivity.class);
            startActivity(onsetIntent);
        });

        Intent infoIntent = new Intent(v.getContext(), InfoActivity.class);
        //Clock information
        ImageButton clockInfoButton = v.findViewById(R.id.ClockInfoButton);
        clockInfoButton.setOnClickListener(view -> {
            infoIntent.putExtra("Location",0);
            startActivity(infoIntent);
        });

        //Initial button color setting
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

        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));

        //Make the state scrollable horizontally
        stateDescriptionText.setMovementMethod(new ScrollingMovementMethod());
        stateDescriptionText.setHorizontallyScrolling(true);
        stateDescriptionText.setSelected(true);

        sleepButton.setOnClickListener(v1 -> sleepButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        napButton.setOnClickListener(v1 -> napButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        workButton.setOnClickListener(v1 -> workButtonClick(v1, mainActivity, startTime, endTime,
                sleepButton, napButton, workButton, sleepTypeText, sleepImportanceText, stateDescriptionText,
                stateDescriptionSmallText, stateDescriptionImage, clockView));
        sleepButton.performClick();

        clockView.setAngleFromTime(mainSleepStartString, mainSleepEndString);

        //we use connection because fragment and activity is connected and we don't reuse fragment for other activity
        //startTime.setText(mainSleepStartString);
        //endTime.setText(mainSleepEndString);
        if (mainActivity.getMainSleepStart() == mainActivity.getMainSleepEnd()) {
            startTime.setText("--:--");
            endTime.setText("--:--");
        } else {
            startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepStart())));
            endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepEnd())));
        }

        Button buttonSendData = v.findViewById(R.id.sendDataButton);

        buttonSendData.setOnClickListener(view -> {
            mainActivity.sendV0(user_name);
        });

        //Graph showing alertness
        //Alertness information
        ImageButton alertnessInfoButton = v.findViewById(R.id.AlertnessInfoButton);
        alertnessInfoButton.setOnClickListener(view -> {
            infoIntent.putExtra("Location",1);
            startActivity(infoIntent);
        });

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
                sleepIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), -100f));
                sleepIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 100f));
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
                    workIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), -100f));
                    workIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 100f));
                } else {
                    if (barEntries.get(i).getX() <= wOffsetF) {
                        workIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), -100f));
                        workIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 100f));
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
        sleeps = mainActivity.getSleeps();
        float alertnessPhaseChange = 49f;
        boolean calculateMore = true;
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
                    lastIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), -100f));
                    lastIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 100f));
                } else {
                    lastIntervalEntries1.add(new BarEntry(barEntries.get(i).getX(), 0f));
                    lastIntervalEntries2.add(new BarEntry(barEntries.get(i).getX(), 0f));
                }
                lBarColors[i] = ResourcesCompat.getColor(getResources(), R.color.blue_1, null);
                //Theoretically recommended sleep onset
                if (i < barEntries.size() - 1) {
                    if (barEntries.get(i).getY() >= 0 && barEntries.get(i + 1).getY() <= 0) {
                        if (barEntries.get(i).getX() >= lOffsetF && calculateMore) {
                            alertnessPhaseChange = barEntries.get(i).getX();
                            calculateMore = false;
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
        xAxis.setLabelCount(12, true);
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

        //Move the view to current alertness which is shown in the center of the graph
        alertnessChart.moveViewToX(19f);

        //Set MarkerView
        String inputOnset = sdfTime.format(new Date(mainActivity.getSleepOnset()));
        boolean isHardToSleep;
        if (mainActivity.getMainSleepStart() == mainActivity.getMainSleepEnd()) {
            isHardToSleep = true;
        } else {
            isHardToSleep = false;
        }
        mv.setRecommendedTime(recommendedOnset);
        mv.setInputTime(inputOnset);
        mv.setIntervalFloat(rMidF, wMidF, lMidF);
        mv.setAlertnessPhaseChange(alertnessPhaseChange);
        mv.setIsHardToSleep(isHardToSleep);
        mv.setChartView(alertnessChart);
        alertnessChart.setMarker(mv);
        //Set the Highlight
        //If the recommended onset and input onset exhibits little difference,
        //only display the recommended onset
        AlertnessText.setMovementMethod(new ScrollingMovementMethod());
        AlertnessText.setHorizontallyScrolling(true);
        AlertnessText.setSelected(true);
        if (alertnessPhaseChange != 49f) {
            Highlight[] highlights = new Highlight[] {
                    new Highlight(24f, 0, -1),
                    new Highlight(alertnessPhaseChange, 0, -1),
                    new Highlight(rMidF, 0, -1),
                    new Highlight(wMidF, 0, -1),
                    new Highlight(lMidF, 0, -1)
            };
            alertnessChart.highlightValues(highlights);
            String originString = floatToTime(alertnessPhaseChange);
            AlertnessText.setText(originString + " 이전에는 잠자리에 들기 어려울 수 있어요");
        } else {
            Highlight[] highlights = new Highlight[] {
                    new Highlight(24f, 0, -1),
                    new Highlight(rMidF, 0, -1),
                    new Highlight(wMidF, 0, -1),
                    new Highlight(lMidF, 0, -1)
            };
            alertnessChart.highlightValues(highlights);
            //Set the time of alertnessText
            String originString = sdfDateTimeRecomm.format(new Date(mainActivity.getMainSleepStart()));
            AlertnessText.setText(originString + " 이전에는 잠자리에 들기 어려울 수 있어요");
        }
        //Change the description depending on current alertness
        if (barEntries.size() > 0) {
            float currentAlertness = barEntries.get((int) barEntries.size()/2).getY();
            if (currentAlertness >= 0) {
                alertnessTitle.setText("집중하기 좋은 상태에요");
                alertnessDescription.setText(user_name + "님의 각성도가 높아요");
            } else {
                alertnessTitle.setText("잠시 낮잠을 자는 건 어때요?");
                alertnessDescription.setText(user_name + "님의 각성도가 낮아요");
            }
        } else {
            alertnessTitle.setText("잠시 바람 쐬는 건 어때요?");
            alertnessDescription.setText(user_name + "님의 각성도가 낮아요");
        }

        alertnessChart.invalidate();

        //Weekly alertness chart
        //Alertness chart information
        ImageButton chartInfoButton = v.findViewById(R.id.ChartInfoButton);
        chartInfoButton.setOnClickListener(view -> {
            infoIntent.putExtra("Location",2);
            startActivity(infoIntent);
        });
        //Set the ChartDescription
        TextView chartDescription = v.findViewById(R.id.ChartDescription);
        chartDescription.setText(user_name + "님의 일주일 활동 시간을 요약했어요");

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
        Collections.reverse(weeklyAwareness);
        //When HomeFragment appears
        if (weeklyAwareness.size() >= 8) {
            Awareness todayAwareness = weeklyAwareness.get(0);
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
            int size = weeklyAwareness.size();
            Awareness clickedAwareness = weeklyAwareness.get(size - position - 1);
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

        //Show the layout simply or not
        TextView summaryText = v.findViewById(R.id.SummaryText);
        LinearLayout chartTextLayout = v.findViewById(R.id.ChartTextLayout);
        if (!sharedPref.contains("isAlertnessSimply")) {
            editor.putBoolean("isAlertnessSimply", true);
        }
        boolean isAlertnessSimply = sharedPref.getBoolean("isAlertnessSimply", true);
        if (isAlertnessSimply) {
            chartTextLayout.setVisibility(View.GONE);
            summaryText.setText("자세히 보기");
        } else {
            chartTextLayout.setVisibility(View.VISIBLE);
            summaryText.setText("간단히 보기");
        }
        summaryText.setOnClickListener(view -> {
            Log.v("HomeFragment", "clicked");
            boolean isSimple = sharedPref.getBoolean("isAlertnessSimply", true);
            if (isSimple) {
                chartTextLayout.setVisibility(View.VISIBLE);
                editor.putBoolean("isAlertnessSimply", false).apply();
                summaryText.setText("간단히 보기");
            } else {
                chartTextLayout.setVisibility(View.GONE);
                editor.putBoolean("isAlertnessSimply", true).apply();
                summaryText.setText("자세히 보기");
            }
        });

        //Weekly sleep duration chart
        //Alertness chart information
        ImageButton sleepChartInfoButton = v.findViewById(R.id.SleepChartInfoButton);
        sleepChartInfoButton.setOnClickListener(view -> {
            infoIntent.putExtra("Location",3);
            startActivity(infoIntent);
        });
        //Set the SleepChartDescription
        TextView sleepChartDescription = v.findViewById(R.id.SleepChartDescription);
        sleepChartDescription.setText(user_name + "님의 일주일의 수면 시간을 요약했어요");

        //Load LinearLayoutManager and BarAdapter for SleepChartRecyclerView
        LinearLayoutManager sleepChartLinearLayoutManager =
                new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
        sleepChartRecycler.setLayoutManager(sleepChartLinearLayoutManager);
        BarAdapter sleepBarAdapter = new BarAdapter();

        List<Awareness> weeklyDuration = new ArrayList<>();

        sleepAwarenesses = mainActivity.getSleepAwarenesses();
        int idx = 0;
        for(Awareness awareness: sleepAwarenesses){
            Log.v("SLEEP AWARENESS VALUE IN SCHEDULE", sdfDate.format(new Date(awareness.awarenessDay*oneDayToMils+oneDayToMils)));

            long hourGoodDuration = awareness.goodDuration/60;
            long minuteGoodDuration = awareness.goodDuration%60;
            long hourBadDuration = awareness.badDuration/60;
            long minuteBadDuration = awareness.badDuration%60;
            Log.v("SLEEP AWARENESS VALUE IN SCHEDULE", hourGoodDuration + ":" + minuteGoodDuration);
            Log.v("SLEEP AWARENESS VALUE IN SCHEDULE", hourBadDuration + ":" + minuteBadDuration);
            if(curDay-8 < awareness.awarenessDay){
                String date = sdfDate.format(new Date((awareness.awarenessDay)*oneDayToMils));
                String goodDuration = hourGoodDuration + ":" + minuteGoodDuration;
                String badDuration = hourBadDuration + ":" + minuteBadDuration;
                //Add to get ONLY weekly awareness
                weeklyDuration.add(idx, awareness);
                sleepBarAdapter.addItem(new Bar(date, convertToWeight(goodDuration), convertToWeight(badDuration)));
                i++;
            }
        }
        Collections.reverse(weeklyDuration);
        //When HomeFragment appears
        Log.v("Weekly duration", String.valueOf(weeklyDuration.size()));
        if (weeklyDuration.size() >= 8) {
            Awareness todayAwareness = weeklyDuration.get(7);
            long todayHourGoodDuration = todayAwareness.goodDuration/60;
            long todayMinuteGoodDuration = todayAwareness.goodDuration%60;
            long todayHourBadDuration = todayAwareness.badDuration/60;
            long todayMinuteBadDuration = todayAwareness.badDuration%60;
            sleepPositiveTimeText.setText(todayHourGoodDuration + "시간 " + todayMinuteGoodDuration + "분");
            sleepNegativeTimeText.setText(todayHourBadDuration + "시간 " + todayMinuteBadDuration + "분");
            setDeltaAwareness(sleepPositiveNumberText, sleepPositiveImage, sleepNegativeNumberText, sleepNegativeImage,
                    7, weeklyDuration);
        }

        //Load the clicked position in RecyclerView
        sleepBarAdapter.setOnBarClickListener(position -> {
            Log.d("HomeFragment", "Item clicked at position " + position);
            Awareness clickedAwareness = weeklyDuration.get(position);
            long hourGoodDuration = clickedAwareness.goodDuration/60;
            long minuteGoodDuration = clickedAwareness.goodDuration%60;
            long hourBadDuration = clickedAwareness.badDuration/60;
            long minuteBadDuration = clickedAwareness.badDuration%60;
            //Time for positive
            sleepPositiveTimeText.setText(hourGoodDuration + "시간 " + minuteGoodDuration + "분");
            //Time for negative
            sleepNegativeTimeText.setText(hourBadDuration + "시간 " + minuteBadDuration + "분");
            //Number for increase and decrease

            setDeltaAwareness(sleepPositiveNumberText, sleepPositiveImage, sleepNegativeNumberText, sleepNegativeImage,
                    position, weeklyDuration);
        });

        sleepChartRecycler.setAdapter(sleepBarAdapter);

        //Show the layout simply or not
        TextView sleepSummaryText = v.findViewById(R.id.SleepSummaryText);
        LinearLayout sleepChartTextLayout = v.findViewById(R.id.SleepChartTextLayout);
        if (!sharedPref.contains("isSleepSimply")) {
            editor.putBoolean("isSleepSimply", true);
        }
        boolean isSleepSimply = sharedPref.getBoolean("isSleepSimply", true);
        if (isSleepSimply) {
            sleepChartTextLayout.setVisibility(View.GONE);
            sleepSummaryText.setText("자세히 보기");
        } else {
            sleepChartTextLayout.setVisibility(View.VISIBLE);
            sleepSummaryText.setText("간단히 보기");
        }
        sleepSummaryText.setOnClickListener(view -> {
            boolean isSimple = sharedPref.getBoolean("isSleepSimply", true);
            if (isSimple) {
                sleepChartTextLayout.setVisibility(View.VISIBLE);
                editor.putBoolean("isSleepSimply", false).apply();
                sleepSummaryText.setText("간단히 보기");
            } else {
                sleepChartTextLayout.setVisibility(View.GONE);
                editor.putBoolean("isSleepSimply", true).apply();
                sleepSummaryText.setText("자세히 보기");
            }
        });

        return v;
    }

    public void sleepButtonClick(View v, MainActivity mainActivity, TextView startTime, TextView endTime,
                                 ImageButton sleepButton, ImageButton napButton, ImageButton workButton,
                                 TextView sleepTypeText, TextView sleepImportanceText,
                                 TextView stateDescriptionText, TextView stateDescriptionSmallText,
                                 ImageView stateDescriptionImage, ClockView clockView)
    {
        stateDescriptionSmallText.setVisibility(View.VISIBLE);
        long sleepStart = mainActivity.getMainSleepStart();
        long sleepEnd = mainActivity.getMainSleepEnd();
        boolean isearly = mainActivity.getIsEarlySleep();
        boolean isenough = mainActivity.getIsEnoughSleep();

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
        if (sleepStart == sleepEnd) {
            startTime.setText("--:--");
            endTime.setText("--:--");
            if (!napSleepStartString.equals(napSleepEndString)) {
                stateDescriptionText.setText("밤잠을 자기 어려운 일정이에요");
                stateDescriptionSmallText.setText("대신 낮잠을 충분히 자야해요");
                stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sad, null));
            } else {
                if (isenough) {
                    stateDescriptionText.setText("근무 전까지 잠이 필요하지 않아요");
                    stateDescriptionSmallText.setText("잠을 자지 않아도 맑은 정신을 유지할 수 있어요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.smile, null));
                } else {
                    stateDescriptionText.setText("밤잠을 자기 어려운 일정이에요");
                    stateDescriptionSmallText.setText("근무 중에 피곤할 수 있어요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sad, null));
                }
            }
        } else {
            startTime.setText(sdfDateTimeRecomm.format(new Date(sleepStart)));
            endTime.setText(sdfDateTimeRecomm.format(new Date(sleepEnd)));
            if (isenough) {
                stateDescriptionText.setText("오늘의 추천 밤잠 일정이에요");
                stateDescriptionSmallText.setText("추천보다 너무 늦지 않게 일어나주세요");
                stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sleep, null));
            } else {
                if (isearly) {
                    stateDescriptionText.setText("근무 전까지 최대한 많이 자야해요");
                    stateDescriptionSmallText.setText("내일 근무 시간에 피곤할 수 있어요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
                } else {
                    stateDescriptionText.setText("근무 전까지 최대한 많이 자야해요");
                    stateDescriptionSmallText.setText("가능하면 더 일찍 자러 들어가 보세요");
                    stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
                }
            }
        }
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
        long napStart = mainActivity.getNapSleepStart();
        long napEnd = mainActivity.getNapSleepEnd();
        startTime.setText(sdfDateTimeRecomm.format(new Date(napStart)));
        endTime.setText(sdfDateTimeRecomm.format(new Date(napEnd)));
        boolean isearly = mainActivity.getIsEarlySleep();
        boolean isenough = mainActivity.getIsEnoughSleep();

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
        clockView.setTypeOfInterval(2);
        if(!napSleepStartString.equals(napSleepEndString)) {
            clockView.setIsRecommended(true);
            clockView.setAngleFromTime(napSleepStartString, napSleepEndString);
            if (isenough) {
                stateDescriptionText.setText("오늘의 추천 낮잠 일정이에요");
                stateDescriptionSmallText.setText("맑은 정신을 위해선 낮잠이 필요해요");
                stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sleep, null));
            } else {
                stateDescriptionText.setText("근무 전까지 최대한 많이 자야해요");
                stateDescriptionSmallText.setText("근무 중에 피곤할 수 있어요");
                stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
            }
        }else{
            clockView.setIsRecommended(false);
            if (isenough) {
                stateDescriptionText.setText("근무 전까지 잠이 필요하지 않아요");
                stateDescriptionSmallText.setText("낮잠 없이도 맑은 정신을 유지할 수 있어요");
                stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.smile, null));
            } else {
                stateDescriptionText.setText("낮잠을 자기 어려운 일정이에요");
                stateDescriptionSmallText.setText("근무 중에 피곤할 수 있어요");
                stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
            }
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
        startTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getWorkOnset())));
        endTime.setText(sdfDateTimeRecomm.format(new Date(mainActivity.getWorkOffset())));
        boolean isearly = mainActivity.getIsEarlySleep();
        boolean isenough = mainActivity.getIsEnoughSleep();
        //Change the color of buttons
        sleepButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        napButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8, null));
        workButton.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.corner_8_clicked, null));
        //Change the content of displaying text
        sleepTypeText.setText("근무");
        sleepImportanceText.setText("중요");
        sleepImportanceText.setBackground(ResourcesCompat
                .getDrawable(getResources(), R.drawable.important_caption, null));
        if (isenough) {
            stateDescriptionText.setText("예정된 근무 시간이에요");
            stateDescriptionSmallText.setText("근무 동안 맑은 정신을 유지할 수 있어요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.smile, null));
        } else {
            stateDescriptionText.setText("예정된 근무 시간이에요");
            stateDescriptionSmallText.setText("근무 동안 피곤할 수 있어요");
            stateDescriptionImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
        }
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
        yAxis.setAxisMaximum(110f);
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

    //Convert x float to "a hh:mm"
    public String floatToTime(float value) {
        if (value <= 48f) {
            float currentValue = 24f;
            float delta = value - currentValue;
            long target = (long) (now + delta*60*60*1000);
            Date date = new Date(target);
            return sdfDateTimeRecomm.format(date);
        } else {
            return "";
        }
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