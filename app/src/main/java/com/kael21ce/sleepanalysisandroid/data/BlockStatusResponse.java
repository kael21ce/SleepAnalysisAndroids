package com.kael21ce.sleepanalysisandroid.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.annotations.SerializedName;
import com.kael21ce.sleepanalysisandroid.BeginRegisterActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class BlockStatusResponse {
    private final static String TAG = "BlockStatusResponse";
    @SerializedName("is_blocked")
    private boolean isBlocked;

    public boolean isBlocked() {
        return isBlocked;
    }

    // is_blocked 서버로부터 확인
    public static void performFetchBlockStatus(RetrofitAPI apiService, BlockStatusCallback callback) {
        apiService.fetchBlockStatus().enqueue(new Callback<BlockStatusResponse>() {
            @Override
            public void onResponse(Call<BlockStatusResponse> call, Response<BlockStatusResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().isBlocked());
                } else {
                    int statusCode = response.code();
                    if (statusCode == 401 || statusCode == 403) {
                        callback.onAuthFailure();
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "알 수 없는 에러";
                            callback.onOtherFailure("HTTP " + statusCode + ": " + errorBody);
                        } catch (IOException e) {
                            callback.onOtherFailure("에러 메시지 파싱 실패");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<BlockStatusResponse> call, Throwable t) {
                NetworkLog(t.getMessage());
                callback.onAuthFailure();
            }
        });
    }

    // Callback으로 isHidden 업데이트 및 로그아웃 진행
    public static void checkBlockStatus(RetrofitAPI apiService, Context context, SharedPreferences sharedPref, BlockReadCallback callback) {
        performFetchBlockStatus(apiService, new BlockStatusCallback() {
            @Override
            public void onSuccess(boolean isBlocked) {
                Log.d(TAG, "is_blocked: " + isBlocked);
                sharedPref.edit().putBoolean("isHidden", isBlocked).apply();

                // 작업 완료 callback 보내기
                callback.onBlockRead();
            }

            @Override
            public void onAuthFailure() {
                Log.d(TAG, "인증 실패. 로그인 화면으로 이동");
                Toast.makeText(context, "인증이 실패하여 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                Intent logOutIntent = new Intent(context, BeginRegisterActivity.class);
                logOutIntent.putExtra("LogOut", true);
                logOutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(logOutIntent);
                callback.onFailRead();
            }

            @Override
            public void onOtherFailure(String errorMessage) {
                Log.e(TAG, errorMessage);

                // 작업 완료 callback 보내기
                callback.onBlockRead();
            }
        });
    }

    public interface BlockReadCallback {
        void onBlockRead();
        void onFailRead();
    }

    private static void NetworkLog(String message) {
        Log.e(TAG, "네트워크 오류: " + message);
    }
}

interface BlockStatusCallback {
    // 성공 시 is_blocked 전달
    void onSuccess(boolean isBlocked);

    // 인증 실패 또는 네트워크 오류 시 호출
    void onAuthFailure();

    // 예외 처리
    void onOtherFailure(String errorMessage);
}
