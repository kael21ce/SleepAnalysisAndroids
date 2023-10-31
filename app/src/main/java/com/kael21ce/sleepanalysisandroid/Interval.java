package com.kael21ce.sleepanalysisandroid;

public class Interval {
    String interval;
    Integer isNap;
    public static final int Nap_Type = 1;
    public static final int Activity_Type = 2;
    public static final int Sleep_Type = 3;

    public Interval(String interval, Integer isNap) {
        this.interval = interval;
        this.isNap = isNap;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public Integer getIsNap() {
        return isNap;
    }

    public void setIsNap(Integer isNap) {
        this.isNap = isNap;
    }
}
