package com.kael21ce.sleepanalysisandroid;

import android.content.res.Resources;
import android.graphics.Color;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.kael21ce.sleepanalysisandroid.data.Awareness;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//Change the color of day if sleep is reported
public class ReportedDecorator implements DayViewDecorator {
    private final Calendar calendar = Calendar.getInstance();
    private android.content.res.Resources resources;
    private Map<Long, List<Sleep>> sleepsData;
    SimpleDateFormat sdfDateTime = new SimpleDateFormat( "yyyy/MM/dd", Locale.KOREA);
    long nineHours = 1000*9*60*60;
    long oneDayToMils = 1000*60*60*24;

    public ReportedDecorator() {
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
            long dayInMillis = date.getTime();
            long calendarDay = (dayInMillis + nineHours) / oneDayToMils;
            //Check
            if (sleepsData.get(calendarDay) == null) {
                return true;
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
            view.addSpan(new ForegroundColorSpan(resources.getColor(R.color.gray_2, null)));
        }
    }

    //Set list of awareness to check whether reported
    public void setSleepsData(Map<Long, List<Sleep>> sleepsData) {
        this.sleepsData = sleepsData;
    }
    public void setResources(Resources resources) {
        this.resources = resources;
    }
}
