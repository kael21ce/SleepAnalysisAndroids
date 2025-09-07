package com.kael21ce.sleepanalysisandroid.data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RetrofitAPI {
    @POST("/sleepapp/survey/")
    Call<Void> createSurvey(@Body DataSurvey dataSurvey);

    @POST("/sleepapp/daily_survey/")
    Call<Void> createMood(@Body DataMood dataMood);

    @POST("/sleepapp/signup/")
    Call<Void> signup(@Body DataUser dataUser);

    @POST("/sleepapp/token/")
    Call<TokenPair> login(@Body DataUser dataUser);

    @POST("/sleepapp/token/refresh/")
    Call<AccessOnly> refresh(@Body RefreshPayload refreshPayload);

    @POST("/sleepapp/user/reset_password/")
    Call<Void> resetPassword(@Body ResetPasswordPayload payload);

    @GET("/sleepapp/user/blocked/")
    Call<BlockStatusResponse> fetchBlockStatus();
}
