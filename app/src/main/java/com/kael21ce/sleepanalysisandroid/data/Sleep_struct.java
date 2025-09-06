package com.kael21ce.sleepanalysisandroid.data;

import com.google.gson.annotations.SerializedName;

// 서버로 데이터 전송을 위한 class
public class Sleep_struct {
    @SerializedName("sleepStart")
    private String sleepStart;

    @SerializedName("sleepEnd")
    private String sleepEnd;

    public Sleep_struct(String sleepStart, String sleepEnd) {
        this.sleepStart = sleepStart;
        this.sleepEnd = sleepEnd;
    }

    // Getter
    public String getSleepStart() {
        return sleepStart;
    }

    public String getSleepEnd() {
        return sleepEnd;
    }
}
