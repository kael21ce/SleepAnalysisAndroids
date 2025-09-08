package com.kael21ce.sleepanalysisandroid;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineBackgroundSpan;
import android.util.Log;

import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//Change the color of day if sleep is reported
public class BlankDecorator implements DayViewDecorator {
    private final Calendar calendar = Calendar.getInstance();
    private Resources resources;
    private Map<Long, List<Sleep>> sleepsData;
    private long todayDate = -1;
    private long minDate = -1;
    SimpleDateFormat sdfDateTime = new SimpleDateFormat( "yyyy/MM/dd", Locale.KOREA);
    long nineHours = 1000*9*60*60;
    long oneDayToMils = 1000*60*60*24;

    public BlankDecorator() {
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        if (sleepsData != null) {
            int year = day.getYear();
            int month = day.getMonth();
            int dayOfMonth = day.getDay();
            String myDate = String.valueOf(year)+'/'+ month +'/'+ dayOfMonth;
            Date date = null;
            try {
                date = sdfDateTime.parse(myDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            assert date != null;

            ZoneId zone = ZoneId.systemDefault();
            ZonedDateTime zoneNow = ZonedDateTime.now(zone);
            ZoneOffset offset = zoneNow.getOffset();
            nineHours = (1000L * offset.getTotalSeconds());

            long dayInMillis = date.getTime();
            long calendarDay = (dayInMillis + nineHours) / oneDayToMils;
            //Check
            if (sleepsData.get(calendarDay) == null) {
                // 수면 시작일로부터 수면 기록이 비어있을 경우
                return minDate != -1 && minDate < calendarDay && calendarDay <= todayDate;
            } else {
                return false;
            }

        } else {
            return true;
        }
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (resources != null) {
            view.addSpan(new CustomDotSpan(resources.getColor(R.color.red_1, null)));
        }
    }

    //Set list of awareness to check whether reported
    public void setSleepsData(Map<Long, List<Sleep>> sleepsData) {
        this.sleepsData = sleepsData;
    }
    public void setResources(Resources resources) {
        this.resources = resources;
    }
    public void setTodayDate(long todayDate) { this.todayDate = todayDate; }
    public void setMinDate(long minDate) { this.minDate = minDate; }
}

class CustomDotSpan implements LineBackgroundSpan {

    private final float radius;
    private final int color;

    // 점의 크기 (radius)와 색상을 설정
    public CustomDotSpan(int color) {
        this.radius = 8;
        this.color = color;
    }

    public CustomDotSpan(float radius, int color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void drawBackground(
            Canvas canvas, Paint paint,
            int left, int right, int top, int baseline, int bottom,
            CharSequence charSequence,
            int start, int end, int lineNumber
    ) {
        int oldColor = paint.getColor();
        if (color != 0) {
            paint.setColor(color);
        }
        // 날짜 텍스트 아래에 점을 그리기
        canvas.drawCircle((left + right) / 2, bottom + radius, radius, paint);
        paint.setColor(oldColor);
    }
}
