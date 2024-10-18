package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class CurrentMarker extends MarkerView {
    private Context context;
    private TextView tvContent;
    private LinearLayout markerLayout, lowerMarkerLayout, currentDotLayout, intervalLayout;
    private ImageView timeMarker, lowerTimeMarker;
    private TextView intervalTypeText;
    private String recommendedTime = "00:00";
    private String inputTime = "00:00";
    private float alertnessPhaseChange = 0f;
    private float sleepIntervalTimeFloat = 0f, workIntervalTimeFloat = 0f, lastIntervalTimeFloat = 0f;
    private Entry currentEntry;
    private BarChart barChart;
    private float currentAlertness = 0;
    private boolean isHardToSleep = false;

    public CurrentMarker(Context context, int layoutResource, BarChart barChart) {
        super(context, layoutResource);
        this.context = context;
        this.barChart = barChart;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        currentEntry = e;
        float xI = timeToX(this.inputTime);

        //Remove all views
        this.removeAllViews();

        //Inflate the layout
        View view;
        if (e.getX() > 23.9f && e.getX() <= 24.1f && highlight.getX() == 24f) {
            //current alertness
            view = LayoutInflater.from(context).inflate(R.layout.current_marker, this, true);
            markerLayout = view.findViewById(R.id.MarkerLayout);
            lowerMarkerLayout = view.findViewById(R.id.LowerMarkerLayout);
            currentDotLayout = view.findViewById(R.id.currentDotLayout);
            this.currentAlertness = e.getY();
            if (e.getY() >= 0) {
                tvContent = view.findViewById(R.id.currentAwareness);
                markerLayout.setVisibility(VISIBLE);
                lowerMarkerLayout.setVisibility(GONE);
                markerLayout.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.corner_8_green, null));
                tvContent.setTextColor(ResourcesCompat.getColor(getResources(), R.color.green_1, null));
                currentDotLayout.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.corner_8_green_stroke, null));
            } else {
                tvContent = view.findViewById(R.id.lowerCurrentAwareness);
                markerLayout.setVisibility(GONE);
                lowerMarkerLayout.setVisibility(VISIBLE);
                lowerMarkerLayout.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.corner_8_red, null));
                tvContent.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red_1, null));
                currentDotLayout.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.corner_8_red_stroke, null));
            }
            float currentAwareness = Math.round(e.getY()*10f)/10.0f;
            tvContent.setText("현재 각성도: " + currentAwareness);
        } else if (e.getX() > this.alertnessPhaseChange - 0.1f
                && e.getX() <= this.alertnessPhaseChange + 0.1f
                && highlight.getX() == this.alertnessPhaseChange) {
            view = LayoutInflater.from(context).inflate(R.layout.time_marker, this, true);
            timeMarker = view.findViewById(R.id.TimeMarker);
            lowerTimeMarker = view.findViewById(R.id.LowerTimeMarker);
            if (this.currentAlertness >= 0) {
                timeMarker.setVisibility(GONE);
                lowerTimeMarker.setVisibility(VISIBLE);
            } else {
                timeMarker.setVisibility(VISIBLE);
                lowerTimeMarker.setVisibility(GONE);
            }
        } else if (e.getX() > this.sleepIntervalTimeFloat - 0.1f && e.getX() <= this.sleepIntervalTimeFloat + 0.1f) {
            view = LayoutInflater.from(context).inflate(R.layout.interval_marker, this, true);
            intervalLayout = view.findViewById(R.id.IntervalLayout);
            intervalTypeText = view.findViewById(R.id.intervalTypeText);
            intervalLayout.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_black_alpha, null));
            intervalTypeText.setText("추천 수면 시간");
            if (isHardToSleep) {
                intervalLayout.setVisibility(GONE);
            } else {
                intervalLayout.setVisibility(VISIBLE);
            }
        } else if (e.getX() > this.workIntervalTimeFloat - 0.1f && e.getX() <= this.workIntervalTimeFloat + 0.1f) {
            view = LayoutInflater.from(context).inflate(R.layout.interval_marker, this, true);
            intervalLayout = view.findViewById(R.id.IntervalLayout);
            intervalTypeText = view.findViewById(R.id.intervalTypeText);
            intervalLayout.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_yellow_alpha, null));
            intervalTypeText.setText("근무 시간");
        } else if (e.getX() > this.lastIntervalTimeFloat - 0.1f && e.getX() <= this.lastIntervalTimeFloat + 0.1f) {
            view = LayoutInflater.from(context).inflate(R.layout.interval_marker, this, true);
            intervalLayout = view.findViewById(R.id.IntervalLayout);
            intervalTypeText = view.findViewById(R.id.intervalTypeText);
            intervalLayout.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_blue_alpha, null));
            intervalTypeText.setText("지난 수면 시간");
        }

        // this will perform necessary layouting
        super.refreshContent(e, highlight);
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        // Get the chart dimensions
        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int width = getWidth();
        int height = getHeight();

        float xR = timeToX(this.recommendedTime);

        // Adjust the y-coordinate
        float yValue = posY;
        float xValue = posX;
        IBarDataSet dataSet = barChart.getData().getDataSetByIndex(0);
        MPPointD point;
        if (currentEntry.getX() > 23.9f && currentEntry.getX() <= 24.1f) {
            yValue = currentEntry.getY();
            xValue = currentEntry.getX();
            point = barChart.getTransformer(dataSet.getAxisDependency()).getPixelForValues(xValue, yValue);
            posY = (float) point.y;
        }
        if (currentEntry.getX() > xR - 0.1f && currentEntry.getX() <= xR + 0.1f) {
            yValue = currentEntry.getY();
            xValue = currentEntry.getX();
            point = barChart.getTransformer(dataSet.getAxisDependency()).getPixelForValues(xValue, yValue);
            posY = (float) point.y;
        }

        posX -= width / 2.0f;

        //Make interval marker move to top of the chart
        boolean inSleep = false, inWork = false, inLast = false;
        if (currentEntry.getX() > this.sleepIntervalTimeFloat - 0.1f
                && currentEntry.getX() <= this.sleepIntervalTimeFloat + 0.1f) {
            inSleep = true;
        }
        if (currentEntry.getX() > this.workIntervalTimeFloat - 0.1f
                && currentEntry.getX() <= this.workIntervalTimeFloat + 0.1f) {
            inWork = true;
        }
        if (currentEntry.getX() > this.lastIntervalTimeFloat - 0.1f
                && currentEntry.getX() <= this.lastIntervalTimeFloat + 0.1f) {
            inLast = true;
        }

        //Current marker and time marker
        boolean inCurrent = false, inTime = false;
        if (currentEntry.getX() > 23.9f
                && currentEntry.getX() <= 24.1f) {
            inCurrent = true;
        }
        if (currentEntry.getX() > this.alertnessPhaseChange - 0.1f
                && currentEntry.getX() <= this.alertnessPhaseChange + 0.1f) {
            inTime = true;
        }

        if (inSleep || inWork || inLast) {
            offset.y = 0;
            canvas.translate(posX, offset.y);
            draw(canvas);
            canvas.translate(-posX, -offset.y);
        } else {
            int savedId = canvas.save();
            canvas.clipRect(barChart.getViewPortHandler().getContentRect());
            if (this.currentAlertness >= 0) {
                if (inCurrent) {
                    posY -= (height - 15f);
                } else if (inTime) {
                    posY -= 15f;
                }
            } else {
                if (inCurrent) {
                    posY -= 15f;
                } else if (inTime) {
                    posY -= (height - 15f);
                }
            }
            canvas.translate(posX, posY);
            draw(canvas);
            canvas.restoreToCount(savedId);
        }
    }


    //Set the recommended time: format should be "hh:mm"
    public void setRecommendedTime(String time) {
        this.recommendedTime = time;
    }
    //Set the input time: format should be "hh:mm"
    public void setInputTime(String time) {
        this.inputTime = time;
    }

    //Compute the x value of time
    public float timeToX(String time) {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentTime = sdf.format(date);
        int currentHour = Integer.parseInt(currentTime.substring(0, currentTime.indexOf(":")));
        int currentMinute = Integer.parseInt(currentTime.substring(currentTime.indexOf(":") + 1));
        int hour = Integer.parseInt(time.substring(0, currentTime.indexOf(":")));
        int minute = Integer.parseInt(time.substring(currentTime.indexOf(":") + 1));
        float currentFloat = currentHour + currentMinute / 60f;
        float timeFloat = hour + minute / 60f;
        if (currentFloat < timeFloat) {
            //Time is in today
            float delta = timeFloat - currentFloat;
            return 24f + delta;
        } else {
            //Time is in tomorrow
            float remainedTime = 24f - currentFloat;
            return 24f + remainedTime + timeFloat;
        }
    }

    public float pastTimeToX(String time) {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentTime = sdf.format(date);
        int currentHour = Integer.parseInt(currentTime.substring(0, currentTime.indexOf(":")));
        int currentMinute = Integer.parseInt(currentTime.substring(currentTime.indexOf(":") + 1));
        int hour = Integer.parseInt(time.substring(0, currentTime.indexOf(":")));
        int minute = Integer.parseInt(time.substring(currentTime.indexOf(":") + 1));
        float currentFloat = currentHour + currentMinute / 60f;
        float timeFloat = hour + minute / 60f;
        if (currentFloat > timeFloat) {
            //Time is in today
            float delta = currentFloat - timeFloat;
            return 24f - delta;
        } else {
            //Time is in yesterday
            return timeFloat - currentFloat;
        }
    }

    //Set the interval time float
    public void setIntervalFloat(float sleepF, float workF, float lastF) {
        this.sleepIntervalTimeFloat = sleepF;
        this.workIntervalTimeFloat = workF;
        this.lastIntervalTimeFloat = lastF;
    }

    //Set whether the recommendation is impossible
    public void setIsHardToSleep(boolean isHardToSleep) {
        this.isHardToSleep = isHardToSleep;
    }

    //Set alertnessPhaseChange
    public void setAlertnessPhaseChange(float value) {
        this.alertnessPhaseChange = value;
    }
}
