package com.kael21ce.sleepanalysisandroid;

import com.kael21ce.sleepanalysisandroid.data.DataMood;
import com.kael21ce.sleepanalysisandroid.data.DataSurvey;

import java.util.ArrayList;
import java.util.Date;

public class WorkType {
    int workType;
    String workStart;
    String workEnd;
    boolean isChosen;

    public int getWorkType() {
        return workType;
    }

    public void setWorkType(int workType) {
        this.workType = workType;
    }

    public String getWorkStart() {
        return workStart;
    }

    public void setWorkStart(String workStart) {
        this.workStart = workStart;
    }

    public String getWorkEnd() {
        return workEnd;
    }

    public void setWorkEnd(String workEnd) {
        this.workEnd = workEnd;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public WorkType(int workType, String workStart, String workEnd, boolean isChosen) {
        this.workType = workType;
        this.workStart = workStart;
        this.workEnd = workEnd;
        this.isChosen = isChosen;
    }
}
