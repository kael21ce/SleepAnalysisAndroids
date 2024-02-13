package com.kael21ce.sleepanalysisandroid.data;

public class DataSurvey {

    private String user;
    private long sleep_onset;
    private long work_onset;
    private long work_offset;
    private long survey_result;
    private long time;

    public DataSurvey(String user, long sleep_onset, long work_onset, long work_offset, long survey_result, long time){
        this.user = user;
        this.sleep_onset = sleep_onset;
        this.work_onset = work_onset;
        this.work_offset = work_offset;
        this.survey_result = survey_result;
        this.time = time;
    }
}
