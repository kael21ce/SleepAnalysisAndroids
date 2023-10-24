package com.kael21ce.sleepanalysisandroid;

public class Interval {
    String interval;
    Integer isNap;

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
