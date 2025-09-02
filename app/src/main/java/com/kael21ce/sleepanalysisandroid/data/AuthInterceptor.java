package com.kael21ce.sleepanalysisandroid.data;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.kael21ce.sleepanalysisandroid.BeginRegisterActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

public class AuthInterceptor implements Interceptor {
    private final TokenStorage tokenStorage;
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.tokenStorage = TokenStorage.getInstance(this.context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String accessToken = tokenStorage.getAccessToken();

        // 1. 액세스 토큰이 있다면 헤더에 추가하기
        Request.Builder builder = originalRequest.newBuilder();
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        Request requestWithHeader = builder.build();

        // 2. 요청 실행하기
        Response response = chain.proceed(requestWithHeader);

        // 3. 401 응답 받으면 refresh 시도하기
        if (response.code() == 401) {
            synchronized (this) {
                String newAccessToken = refreshToken();

                if (newAccessToken != null) {
                    response.close();

                    Request newRequest = requestWithHeader.newBuilder()
                            .header("Authorization", "Bearer " + newAccessToken)
                            .build();
                    return chain.proceed(newRequest);
                } else {
                    // refresh 실패하면 로그아웃 처리
                    logoutAndNavigateToLogin();
                }
            }
        }
        return response;
    }

    // 동기적으로 토큰을 refresh
    private String refreshToken() {
        String refreshToken = tokenStorage.getRefreshToken();
        if (refreshToken == null) {
            return null;
        }

        RetrofitAPI apiService = RetrofitClient.getClient().create(RetrofitAPI.class);
        Call<AccessOnly> call = apiService.refresh(new RefreshPayload(refreshToken));

        try {
            retrofit2.Response<AccessOnly> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                String newAccessToken = response.body().getAccessToken();
                tokenStorage.updateAccessToken(newAccessToken);
                return newAccessToken;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void logoutAndNavigateToLogin() {
        tokenStorage.clearTokens();
        Intent intent = new Intent(context, BeginRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("LogOut", true);
        context.startActivity(intent);
    }
}
