package com.kael21ce.sleepanalysisandroid.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// 업로드를 위한 클래스
public class SleepUploadPayload {
    @SerializedName("sleep")
    private List<Sleep_struct> sleep;

    public SleepUploadPayload(List<Sleep_struct> sleep) {
        this.sleep = sleep;
    }

    // Getter
    public List<Sleep_struct> getSleep() {
        return sleep;
    }
}