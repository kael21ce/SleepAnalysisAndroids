package com.kael21ce.sleepanalysisandroid.data;

import com.google.gson.annotations.SerializedName;

public class DataMood {
    @SerializedName("latency")
    private int latency;
    @SerializedName("daily_alertness")
    private int daily_alertness;
    @SerializedName("sleep_quality")
    private int sleep_quality;
    @SerializedName("mood_high")
    private int mood_high;
    @SerializedName("mood_low")
    private int mood_low;
    @SerializedName("mood_anx")
    private int mood_anx;
    @SerializedName("mood_irr")
    private int mood_irr;
    @SerializedName("time")
    private long time;

    public int getSleep_quality() {
        return sleep_quality;
    }

    public int getDaily_alertness() {
        return daily_alertness;
    }

    public long getTime() {
        return time;
    }

    public int getLatency() {
        return latency;
    }

    public int getMood_high() {
        return mood_high;
    }

    public int getMood_low() {
        return mood_low;
    }

    public int getMood_anx() {
        return mood_anx;
    }

    public int getMood_irr() {
        return mood_irr;
    }

    public DataMood(int latency, int daily_alertness, int sleep_quality, int mood_high, int mood_low, int mood_anx, int mood_irr, long time){
        this.latency = latency;
        this.daily_alertness = daily_alertness;
        this.sleep_quality = sleep_quality;
        this.mood_high = mood_high;
        this.mood_low = mood_low;
        this.mood_anx = mood_anx;
        this.mood_irr = mood_irr;
        this.time = time;
    }

}
