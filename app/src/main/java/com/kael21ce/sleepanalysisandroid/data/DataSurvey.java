package com.kael21ce.sleepanalysisandroid.data;

import com.google.gson.annotations.SerializedName;

public class DataSurvey {
    @SerializedName("sleep_onset")
    private long sleep_onset;
    @SerializedName("work_onset")
    private long work_onset;
    @SerializedName("work_offset")
    private long work_offset;
    @SerializedName("survey_result")
    private long survey_result;
    @SerializedName("time")
    private long time;

    public long getTime() {
        return time;
    }

    public long getSurvey_result() {
        return survey_result;
    }

    public DataSurvey(long sleep_onset, long work_onset, long work_offset, long survey_result, long time){
        this.sleep_onset = sleep_onset;
        this.work_onset = work_onset;
        this.work_offset = work_offset;
        this.survey_result = survey_result;
        this.time = time;
    }
}
