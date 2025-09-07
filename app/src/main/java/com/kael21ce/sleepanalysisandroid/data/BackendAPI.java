package com.kael21ce.sleepanalysisandroid.data;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BackendAPI {
    private static final String TAG = "BackendAPI";

    // 일일 설문을 서버에 업로드
    public static void sendDailySurvey(Context context, int sleep_quality, int daily_alertness,
                                int mood_high, int mood_low,
                                int mood_anx, int mood_irr, int latency,
                                SurveyCallback callback) {
        // 설문 작성 시기
        long time = System.currentTimeMillis();

        // API 불러오기
        RetrofitAPI apiService = RetrofitClient.getClient(context).create(RetrofitAPI.class);

        // 설문 payload 생성
        DataMood payload = new DataMood(latency, daily_alertness, sleep_quality,
                mood_high, mood_low, mood_anx, mood_irr, time);

        // 네트워크 호출
        apiService.createMood(payload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "일일 설문 업로드 성공. Status: " + response.code());
                    callback.onSuccess();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "알 수 없는 에러";
                        Log.e(TAG, "서버 에러: " + response.code());
                        callback.onFailure("서버 에러: " + response.code());
                    } catch (IOException e) {
                        Log.e(TAG, "에러 메시지 파싱 실패");
                        callback.onFailure("에러 메시지 파싱 실패");
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "네트워크 에러: " + t.getMessage());
                callback.onFailure("네트워크 에러: " + t.getMessage());
            }
        });
    }

    // 각성도 설문을 서버에 업로드

    // 설문 전달 후를 진행하기 위한 callback
    public interface SurveyCallback {
        void onSuccess();
        void onFailure(String errorMsg);
    }
}
