package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class ClockView extends View {

    private float startAngle = 0;
    //Sweep clockwise
    private float sweepAngle = 90;
    private Paint arcPaint;
    private Paint circlePaint;
    //To distinguish the type of interval
    private int typeOfInterval = 1;

    public ClockView(Context context) {
        super(context);
    }

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        arcPaint = new Paint();
        arcPaint.setStyle(Paint.Style.FILL);
        //Color of the arc
        if (typeOfInterval == 1) {
            arcPaint.setColor(getResources().getColor(R.color.black, null));
        } else if (typeOfInterval == 2) {
            arcPaint.setColor(getResources().getColor(R.color.gray_4, null));
        } else if (typeOfInterval == 3) {
            arcPaint.setColor(getResources().getColor(R.color.yellow_1, null));
        } else {
            arcPaint.setColor(getResources().getColor(R.color.blue_1, null));
        }

        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(getResources().getColor(R.color.white, null));

        int width = getWidth();
        int height = getHeight();

        // Calculate the center of the view
        int centerX = width / 2;
        int centerY = height / 2;

        // Calculate the arc's bounding rectangle
        int radius = Math.min(centerX, centerY);
        int left = centerX - radius;
        int top = centerY - radius;
        int right = centerX + radius;
        int bottom = centerY + radius;

        //Draw the arc and circle on the canvas
        canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, true, arcPaint);
        double circleRadius = Math.min(width, height) / 2.8;
        float CircleRadius = (float) circleRadius;
        canvas.drawCircle(centerX, centerY, CircleRadius, circlePaint);
    }

    //Set the startAngle and sweepAngle
    public void setAngle(float startAngle, float sweepAngle) {
        this.startAngle = startAngle;
        this.sweepAngle = sweepAngle;
        invalidate();
    }

    //Set the type of interval displaying
    public void setTypeOfInterval(int type) {
        this.typeOfInterval = type;
        invalidate();
    }
}
