package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class CurrentMarker extends MarkerView {
    private TextView tvContent;
    private LinearLayout markerLayout;
    private LinearLayout dotLayout;
    public CurrentMarker(Context context, int layoutResource) {
        super(context, layoutResource);
        // find your layout components
        tvContent = (TextView) findViewById(R.id.currentAwareness);
        markerLayout = (LinearLayout) findViewById(R.id.MarkerLayout);

    }
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e.getX() > 23.9f && e.getX() <= 24.1f) {
            markerLayout.setVisibility(VISIBLE);
            tvContent.setVisibility(VISIBLE);
            float currentAwareness = Math.round(e.getY()*10f)/10.0f;
            tvContent.setText("현재 각성도: " + currentAwareness);
        } else {
            markerLayout.setVisibility(INVISIBLE);
            tvContent.setVisibility(INVISIBLE);
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
}
