package com.kael21ce.sleepanalysisandroid.data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitAPI {
    @POST("android/")

        //on below line we are creating a method to post our data.
    Call<DataModal> createPost(@Body DataModal dataModal);

    // "user/"는 더 이상 존재하지 않는 라우트(SPA catch-all로 떨어짐). 실제 회원가입/로그인은
    // 각각 signup/, token/ 이다 (iOS AuthAPI와 동일).
    @POST("signup/")

    Call<DataUser> createSignupUser(@Body DataUser dataUser);

    @POST("token/")

    Call<TokenPair> login(@Body DataUser dataUser);

    @POST("token/refresh/")

    Call<AccessOnly> refreshToken(@Body RefreshRequest body);

    @POST("survey/")

    Call<DataSurvey> createSurvey(@Body DataSurvey dataSurvey);

    @POST("daily_survey/")

    Call<DataMood> createMood(@Body DataMood dataMood);

    // 로그인 직후 서버에 저장된 데이터를 다시 읽어와 로컬에 복원하기 위한 조회용 엔드포인트들
    // (iOS의 pullSleepsViaJSON / fetchDailySurvey / fetchSurveyRecords에 대응).
    @GET("android/")
    Call<SleepFetchResponse> getSleeps();

    @GET("daily_survey/")
    Call<DailySurveyResponse> getDailySurvey(@Query("from") String from, @Query("to") String to);

    @GET("survey/")
    Call<KssSurveyResponse> getSurvey(@Query("from") String from, @Query("to") String to, @Query("min_result") int minResult);
}
