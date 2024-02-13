package com.kael21ce.sleepanalysisandroid.data;

public class DataMood {

    private String user;
    private int survey_result2;
    private int sleep_quality;
    private int mood_high;
    private int mood_low;
    private int mood_anx;
    private int mood_irr;
    private long time;

    public DataMood(String user, int survey_result2, int sleep_quality, int mood_high, int mood_low, int mood_anx, int mood_irr, long time){
        this.user = user;
        this.survey_result2 = survey_result2;
        this.sleep_quality = sleep_quality;
        this.mood_high = mood_high;
        this.mood_low = mood_low;
        this.mood_anx = mood_anx;
        this.mood_irr = mood_irr;
        this.time = time;
    }

}
