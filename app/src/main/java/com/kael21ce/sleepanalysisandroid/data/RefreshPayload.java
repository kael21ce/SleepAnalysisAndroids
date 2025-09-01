package com.kael21ce.sleepanalysisandroid.data;

import com.google.gson.annotations.SerializedName;

public class RefreshPayload {
    private String refresh;

    // Getter
    public RefreshPayload(String refresh) {
        this.refresh = refresh;
    }
}
