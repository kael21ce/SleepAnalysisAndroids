package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ClockView extends View {

    private float startAngle = 45;
    //Sweep clockwise
    private float sweepAngle = 270;
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

        //Calculate the position of small circles in the sides of interval
        double smallCircleRadius = Math.min(width, height) / 14;
        double beta = Math.asin(smallCircleRadius / (radius - smallCircleRadius));
        double betaDeg = beta * 180 / Math.PI;
        float startAngleRad = (float) (startAngle * Math.PI / 180);
        float sweepedAngleRad = (float) ((startAngle + sweepAngle) * Math.PI / 180);
        float positionX1 = (float) (centerX + (radius - smallCircleRadius) * Math.cos(-startAngleRad - beta));
        float positionY1 = (float) (centerY - (radius - smallCircleRadius) * Math.sin(-startAngleRad - beta));
        float positionX2 = (float) (centerX + (radius - smallCircleRadius) * Math.cos(-sweepedAngleRad + beta));
        float positionY2 = (float) (centerY - (radius - smallCircleRadius) * Math.sin(-sweepedAngleRad + beta));
        //Draw the arc on the canvas
        canvas.drawArc(left, top, right, bottom, startAngle + Math.abs((float) betaDeg),
                sweepAngle - 2 * Math.abs((float) betaDeg), true, arcPaint);
        //Draw small circles
        canvas.drawCircle(positionX1, positionY1, (float) smallCircleRadius, arcPaint);
        canvas.drawCircle(positionX2, positionY2, (float) smallCircleRadius, arcPaint);
        //Draw circle on the canvas
        double circleRadius = Math.min(width, height) / 2.8;
        float CircleRadius = (float) circleRadius;
        canvas.drawCircle(centerX, centerY, CircleRadius, circlePaint);
    }

    //Set the start Time and end Time
    public void setAngleFromTime(String startTime, String endTime) {
        float startAngle = convertTimeToAngle(startTime);
        this.startAngle = startAngle;
        float endAngle = convertTimeToAngle(endTime);
        if (startAngle > endAngle) {
            this.sweepAngle = 360 + endAngle - startAngle;
        } else {
            this.sweepAngle = endAngle - startAngle;
        }
        invalidate();
    }

    //Set the type of interval displaying
    public void setTypeOfInterval(int type) {
        this.typeOfInterval = type;
        invalidate();
    }

    //Change time to degree angle (e.g. 13:40 -> 115)
    public Float convertTimeToAngle(String time) {
        //Should be in format of "hh:mm"
        List<Integer> HrMin = convertTimeFormat(time);
        if (HrMin != null) {
            int intHour = HrMin.get(0);
            int intMinute = HrMin.get(1);
            if (intHour > 24 || intMinute > 60) {
                return (float) 0;
            }

            return (float) (15 * (intHour - 6) + 0.25 * intMinute);
        } else {
            return (float) 0;
        }
    }

    public List<Integer> convertTimeFormat(String time) {
        //Should be in format of "hh:mm"
        List<Integer> result = new ArrayList<Integer>();
        if (time.length() > 5) {
            return result;
        } else {
            int index = time.indexOf(":");
            if (index == -1) {
                return result;
            }
            String hour = time.substring(0, index);
            String minute = time.substring(index + 1);
            int intHour = Integer.parseInt(hour);
            int intMinute = Integer.parseInt(minute);
            result.add(0, intHour);
            result.add(1, intMinute);

            return result;
        }
    }
}

