package com.kael21ce.sleepanalysisandroid.data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitAPI {
    @POST("android/")

        //on below line we are creating a method to post our data.
    Call<DataModal> createPost(@Body DataModal dataModal);

    @POST("user/")

    Call<DataUser> createUser(@Body DataUser dataUser);

    @POST("survey/")

    Call<DataSurvey> createSurvey(@Body DataSurvey dataSurvey);

    @POST("survey/sleep_quality/")

    Call<DataMood> createMood(@Body DataMood dataMood);
}
