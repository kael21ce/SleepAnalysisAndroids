package com.kael21ce.sleepanalysisandroid;

import com.kael21ce.sleepanalysisandroid.data.DataMood;
import com.kael21ce.sleepanalysisandroid.data.DataSurvey;

import java.util.ArrayList;
import java.util.Date;

public class Records {
    Date recordDate;
    boolean isAlertness;
    ArrayList<DataSurvey> dataSurvey;
    ArrayList<DataMood> dataMood;

    public boolean isAlertness() {
        return isAlertness;
    }

    public void setAlertness(boolean alertness) {
        isAlertness = alertness;
    }

    public ArrayList<DataSurvey> getDataSurvey() {
        return dataSurvey;
    }

    public void setDataSurvey(ArrayList<DataSurvey> dataSurvey) {
        this.dataSurvey = dataSurvey;
    }

    public ArrayList<DataMood> getDataMood() {
        return dataMood;
    }

    public void setDataMood(ArrayList<DataMood> dataMood) {
        this.dataMood = dataMood;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    public Records(Date date, boolean isAlertness, ArrayList<DataSurvey> dataSurvey, ArrayList<DataMood> dataMood) {
        this.recordDate = date;
        this.isAlertness = isAlertness;
        this.dataSurvey = dataSurvey;
        this.dataMood = dataMood;
    }
}
