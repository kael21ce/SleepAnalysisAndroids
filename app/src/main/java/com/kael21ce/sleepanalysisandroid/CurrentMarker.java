package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class CurrentMarker extends MarkerView {
    private TextView tvContent;
    private LinearLayout markerLayout;
    private LinearLayout TimeMarkerLayout;
    private LinearLayout currentDotLayout, timeDotLayout;
    private TextView timeTypeText;
    private String recommendedTime = "00:00";
    private String inputTime = "00:00";

    public CurrentMarker(Context context, int layoutResource) {
        super(context, layoutResource);
        // find your layout components
        tvContent = (TextView) findViewById(R.id.currentAwareness);
        markerLayout = (LinearLayout) findViewById(R.id.MarkerLayout);
        TimeMarkerLayout = findViewById(R.id.TimeMarkerLayout);
        timeTypeText = findViewById(R.id.timeTypeText);
        currentDotLayout = findViewById(R.id.currentDotLayout);
        timeDotLayout = findViewById(R.id.timeDotLayout);
    }
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        float xR = timeToX(this.recommendedTime);
        float xI = timeToX(this.inputTime);
        if (e.getX() > 23.9f && e.getX() <= 24.1f) {
            //current alertness
            markerLayout.setVisibility(VISIBLE);
            tvContent.setVisibility(VISIBLE);
            currentDotLayout.setVisibility(VISIBLE);
            TimeMarkerLayout.setVisibility(GONE);
            timeTypeText.setVisibility(GONE);
            timeDotLayout.setVisibility(GONE);
            float currentAwareness = Math.round(e.getY()*10f)/10.0f;
            tvContent.setText("현재 각성도: " + currentAwareness);
        } else if (e.getX() > xR - 0.1f && e.getX() <= xR + 0.1f) {
            markerLayout.setVisibility(INVISIBLE);
            tvContent.setVisibility(INVISIBLE);
            currentDotLayout.setVisibility(INVISIBLE);
            TimeMarkerLayout.setVisibility(VISIBLE);
            timeTypeText.setVisibility(VISIBLE);
            timeDotLayout.setVisibility(VISIBLE);
            timeTypeText.setText("희망 취침 시각");
        } else if (e.getX() > xI - 0.1f && e.getX() <= xI + 0.1f) {
            markerLayout.setVisibility(INVISIBLE);
            tvContent.setVisibility(INVISIBLE);
            currentDotLayout.setVisibility(INVISIBLE);
            TimeMarkerLayout.setVisibility(VISIBLE);
            timeTypeText.setVisibility(VISIBLE);
            timeDotLayout.setVisibility(VISIBLE);
            timeTypeText.setText("권장 취침 시각");
        } else {
            markerLayout.setVisibility(GONE);
            tvContent.setVisibility(GONE);
            currentDotLayout.setVisibility(GONE);
            TimeMarkerLayout.setVisibility(GONE);
            timeTypeText.setVisibility(GONE);
            timeDotLayout.setVisibility(GONE);
        }
        // this will perform necessary layouting
        super.refreshContent(e, highlight);
    }
    private MPPointF mOffset;
    @Override
    public MPPointF getOffset() {
        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }
        return mOffset;
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
}
