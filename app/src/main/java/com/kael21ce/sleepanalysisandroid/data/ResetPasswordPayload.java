package com.kael21ce.sleepanalysisandroid.data;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordPayload {
    @SerializedName("new_password")
    private final String newPassword;

    @SerializedName("email")
    private String email;

    @SerializedName("current_password")
    private String currentPassword;

    public ResetPasswordPayload(String newPassword) {
        this.newPassword = newPassword;
    }

    public ResetPasswordPayload setEmail(String email) {
        this.email = email;
        return this;
    }

    public ResetPasswordPayload setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
        return this;
    }
}
