package com.kael21ce.sleepanalysisandroid.data;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// iOS의 AuthorizedClient에 대응하는 공용 클라이언트. 저장된 access 토큰을 모든 요청에
// 자동으로 Authorization 헤더로 붙이고, 401을 받으면 refresh 토큰으로 재발급 후 그 요청만
// 한 번 재시도한다. 로그인 전(access 없음)에는 헤더 없이 그냥 나간다.
public final class ApiClient {
    private static final String BASE_URL = "https://www.sleep-math.com/sleepapp/";
    private static volatile Retrofit retrofit;

    private ApiClient() {}

    public static RetrofitAPI api(Context context) {
        return retrofit(context).create(RetrofitAPI.class);
    }

    private static Retrofit retrofit(Context context) {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    Context appContext = context.getApplicationContext();
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .addInterceptor(authInterceptor(appContext))
                            .authenticator(refreshAuthenticator(appContext))
                            .build();
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();
                }
            }
        }
        return retrofit;
    }

    private static Interceptor authInterceptor(Context context) {
        return chain -> {
            Request original = chain.request();
            String access = TokenStorage.getAccess(context);
            if (access == null || access.isEmpty()) {
                return chain.proceed(original);
            }
            Request authorized = original.newBuilder()
                    .header("Authorization", "Bearer " + access)
                    .build();
            return chain.proceed(authorized);
        };
    }

    private static Authenticator refreshAuthenticator(Context context) {
        return (Route route, Response response) -> {
            // 이미 한 번 재시도했다면 포기 (무한 루프 방지)
            if (responseCount(response) >= 2) return null;

            String refresh = TokenStorage.getRefresh(context);
            if (refresh == null || refresh.isEmpty()) return null;

            String newAccess = refreshAccessTokenSync(refresh);
            if (newAccess == null) return null;

            TokenStorage.saveAccess(context, newAccess);
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccess)
                    .build();
        };
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    // Authenticator는 백그라운드 스레드에서 동기 호출을 요구하므로 execute()로 처리
    private static String refreshAccessTokenSync(String refreshToken) {
        try {
            OkHttpClient plain = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .build();
            Retrofit plainRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(plain)
                    .build();
            RetrofitAPI plainApi = plainRetrofit.create(RetrofitAPI.class);
            retrofit2.Response<AccessOnly> resp = plainApi.refreshToken(new RefreshRequest(refreshToken)).execute();
            if (resp.isSuccessful() && resp.body() != null) {
                return resp.body().getAccess();
            }
        } catch (IOException ignored) {
        }
        return null;
    }
}
