package com.kael21ce.sleepanalysisandroid.data;

import com.google.gson.annotations.SerializedName;

public class AccessOnly {
    @SerializedName("access")
    private String accessToken;

    // Getter
    public String getAccessToken() {
        return accessToken;
    }
}
