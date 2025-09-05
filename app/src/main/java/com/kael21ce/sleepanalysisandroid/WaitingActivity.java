package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.kael21ce.sleepanalysisandroid.data.AuthInterceptor;
import com.kael21ce.sleepanalysisandroid.data.BlockStatusResponse;
import com.kael21ce.sleepanalysisandroid.data.DataMood;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;
import com.kael21ce.sleepanalysisandroid.data.RetrofitClient;
import com.kael21ce.sleepanalysisandroid.data.Sleep;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class WaitingActivity extends AppCompatActivity {

    private final Handler dotHandler = new Handler();
    private int dotCount = 0;
    private final int MAX_DOTS = 3;
    private TextView waitingText;
    private static final String TAG = "WaitingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_waiting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.WaitingViewLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Action bar 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Description에 user name 추가
        TextView waitingDescription = findViewById(R.id.WaitingDescription);
        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        String user_name = sharedPref.getString("User_Name", "로딩 오류");
        String user_based_text = user_name + waitingDescription.getText().toString();
        waitingDescription.setText(user_based_text);

        // WaitingText 뒤에 dot 추가
        waitingText = findViewById(R.id.WaitingText);
        updateDots();

        // GIF 이미지 설정
        ImageView waitingImage = findViewById(R.id.WaitingImage);
        Glide.with(this).load(R.raw.loading).into(waitingImage);

        // 사전 수면 동기화
        long currentTime = System.currentTimeMillis();
        long sleepOnset = sharedPref.getLong("sleepOnset", currentTime);
        long workOnset = sharedPref.getLong("workOnset", currentTime);
        long workOffset = sharedPref.getLong("workOffset", currentTime);
        long sleepOnsetShow = sharedPref.getLong("sleepOnsetShow", currentTime);

        Long[] updatedDates = updateOnsetDate(currentTime, sleepOnset, sleepOnsetShow, workOnset, workOffset);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("sleepOnset", updatedDates[0]);
        editor.putLong("sleepOnsetShow", updatedDates[1]);
        editor.putLong("workOnset", updatedDates[2]);
        editor.putLong("workOffset", updatedDates[3]);
        editor.apply();
        Log.v("SplashActivity", "Onset: " + updatedDates[0] + " / Onset Show: " + updatedDates[1] +
                " / Work onset: " + updatedDates[2] + " / Work offset: " + updatedDates[3]);

        // 1) 수면 데이터 동기화

        // 2) 설문 동기화

        // 3) is_blocked 불러오기
        RetrofitAPI apiService = RetrofitClient.getClient(this).create(RetrofitAPI.class);
        BlockStatusResponse.checkBlockStatus(apiService, this, sharedPref, new BlockStatusResponse.BlockReadCallback() {
            @Override
            public void onBlockRead() {
                Log.d(TAG, "is_blocked 읽기 완료");
                Intent finishIntent = new Intent(WaitingActivity.this, FinishActivity.class);
                startActivity(finishIntent);
                finish();
            }

            @Override
            public void onFailRead() {
                Log.d(TAG, "is_blocked 읽기 실패");
            }
        });
    }

    // WaitingText 뒤에 점을 주기적으로 업데이트
    private void updateDots() {
        dotHandler.postDelayed(() -> {
            dotCount++;
            if (dotCount > MAX_DOTS) {
                waitingText.setText("잠시만 기다려주세요");
            } else {
                waitingText.setText("잠시만 기다려주세요"
                        + new String(new char[dotCount]).replace("\0", "."));
            }
            updateDots();
        },500);
    }

    // 사전 동기화
    public static Long[] updateOnsetDate(long currentTime, long sleepOnset, long sleepOnsetShow, long workOnset, long workOffset) {
        long oneDayToMils = 1000*60*60*24;
        long tenMinToMils = 1000*60*10;
        long oneHourToMils = 1000*60*60;

        // Keep sleepOnsetShow before workOnset minus 1 day
        while (sleepOnsetShow < workOnset - oneDayToMils) {
            sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            sleepOnset = sleepOnsetShow;
        }

        // Ensure workOnset is after sleepOnset
        while (workOnset < sleepOnset) {
            workOnset = workOnset + oneDayToMils;
        }

        // Ensure workOffset is after workOnset
        while (workOffset < workOnset) {
            workOffset = workOffset + oneDayToMils;
        }

        // Adjust sleepOnset if currentTime is within sleepOnset and workOnset
        if (sleepOnset < currentTime && currentTime < workOnset) {
            while (sleepOnset < currentTime) {
                sleepOnset = currentTime + tenMinToMils;
            }
        }

        // Ensure sleepOnsetShow is not before currentTime
        if (workOnset - oneHourToMils <= sleepOnset) {
            while (sleepOnsetShow < currentTime) {
                sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            }
            sleepOnset = sleepOnsetShow;
        }

        // Repeat the adjustments for sleepOnsetShow, workOnset, and workOffset
        while (sleepOnsetShow < workOnset - oneDayToMils) {
            sleepOnsetShow = sleepOnsetShow + oneDayToMils;
            sleepOnset = sleepOnsetShow;
        }
        while (workOnset < sleepOnset) {
            workOnset = workOnset + oneDayToMils;
        }
        while (workOffset < workOnset) {
            workOffset = workOffset + oneDayToMils;
        }

        // Update work if it is ended
        while (currentTime > workOffset) {
            workOnset = workOnset + oneDayToMils;
            workOffset = workOffset + oneDayToMils;
        }

        return new Long[]{sleepOnset, sleepOnsetShow, workOnset, workOffset};
    }

    // 서버 수면 JSON 동기화 및 Core data 적재
    // 실패한 것: sharedPreference에 저장된 값을 사용하도록 처리
    private List<SleepJSONSingle> performFetchSleepsJSON() {
        final String type = "수면";
        String format = "iso_utc";
        final List<SleepJSONSingle>[] sleepList = new List[]{new ArrayList<>()};

        Retrofit retrofit = WaitingClient.getWaitingClient(this, true);
        WaitingService apiService = retrofit.create(WaitingService.class);
        apiService.fetchSleeps(format).enqueue(new Callback<List<SleepJSONSingle>>() {
            @Override
            public void onResponse(Call<List<SleepJSONSingle>> call, Response<List<SleepJSONSingle>> response) {
                if (response.code() == 204) {
                    NoContentLog(type);
                    return;
                }

                if (response.isSuccessful()) {
                    List<SleepJSONSingle> sleepData = response.body();
                    SuccessfulLoadingLog(type, sleepData.size());
                    sleepList[0] = sleepData;
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "알 수 없는 에러";
                        FailureLoadingLog(type, response.code(), errorBody);
                    } catch (IOException e) {
                        ParsingFailureLog();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SleepJSONSingle>> call, Throwable t) {
                NetworkLog(t.getMessage());
            }
        });
        return sleepList[0];
    }

    // 설문 데이터 다운로드: 7일과 그 이상 기간을 가져오는 method를 따로 제작
    private List<DataMood> performFetchDailySurvey(boolean isLonger) {
        final List<DataMood>[] surveyList = new List[]{new ArrayList<>()};
        final String type = "일일 설문";

        // 1. 날짜 계산 (현재, 7일 전)
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        if (isLonger) {
            calendar.add(Calendar.DAY_OF_YEAR, -49); // 7주
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, -7); // 7일
        }
        Date fromDate = calendar.getTime();

        // 2. ISO8601 UTC formatter 생성: YYYY-MM-DDTHH:MM:SSZ
        SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String toDateStr = isoFormatter.format(now);
        String fromDateStr = isoFormatter.format(fromDate);

        // 3. API 호출
        Retrofit retrofit = WaitingClient.getWaitingClient(this, false);
        WaitingService apiService = retrofit.create(WaitingService.class);
        apiService.fetchDailySurvey(fromDateStr, toDateStr).enqueue(new Callback<DailySurveyResponse>() {
            @Override
            public void onResponse(Call<DailySurveyResponse> call, Response<DailySurveyResponse> response) {
                if (response.code() == 204) {
                    NoContentLog(type);
                    return;
                }

                if (response.isSuccessful()) {
                    List<DataMood> incoming = response.body().getResults();
                    if (incoming == null || incoming.isEmpty()) {
                        Log.d(TAG, "결과 리스트가 비어있습니다.");
                        return;
                    }

                    SuccessfulLoadingLog(type, incoming.size());
                    surveyList[0] = incoming;
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "알 수 없는 에러";
                        FailureLoadingLog(type, response.code(), errorBody);
                    } catch (IOException e) {
                        ParsingFailureLog();
                    }
                }
            }

            @Override
            public void onFailure(Call<DailySurveyResponse> call, Throwable t) {
                NetworkLog(t.getMessage());
            }
        });
        return surveyList[0];
    }


    // 로깅 유틸
    private void NoContentLog(String type) {
        Log.d(TAG, type + " 데이터 없음 (204 No Content)");
    }

    private void SuccessfulLoadingLog(String type, int number) {
        Log.d(TAG, type + " 데이터 로딩 성공: # - " + number);
    }

    private void FailureLoadingLog(String type, int code, String errorBody) {
        Log.e(TAG, type + " 데이터 로딩 실패: " + code + " - " + errorBody);
    }

    private void ParsingFailureLog() {
        Log.e(TAG, "에러 메시지 파싱 실패");
    }

    private void NetworkLog(String message) {
        Log.e(TAG, "네트워크 오류: " + message);
    }
}

class PersonalResponse {
    @SerializedName("sleep")
    private List<Sleep> sleep;

    public List<Sleep> getSleep() {
        return sleep;
    }
}

class SleepJSONSingle {
    @SerializedName("user")
    private String user;
    @SerializedName("sleep")
    private List<Sleep> sleep;

    // Getter
    public List<Sleep> getSleep() { return sleep; }

    public static SleepJSONSingle fromSleepList(List<Sleep> sleepList) {
        SleepJSONSingle instance = new SleepJSONSingle();
        instance.sleep = sleepList;

        return instance;
    }
}

class SleepJSONMulti {
    @SerializedName("results")
    private List<SleepJSONSingle> results;

    // Getter
    public List<SleepJSONSingle> getResults() { return  results; }
}

// JSON 구조를 분석해 List<SleepJSONSingle>로 변환해주는 JSONDeserializer 정의
class SleepDataDeserializer implements JsonDeserializer<List<SleepJSONSingle>> {
    @Override
    public List<SleepJSONSingle> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Gson gson = new Gson();

        // 1. { "sleep": [...] } 형태인지 확인
        if (jsonObject.has("sleep")) {
            PersonalResponse personalResponse = gson.fromJson(json, PersonalResponse.class);
            SleepJSONSingle wrappedResponse = SleepJSONSingle.fromSleepList(personalResponse.getSleep());
            return Collections.singletonList(wrappedResponse);
        }


        // 2. { "user": ..., "sleep": [...] } 형태인지 확인
        if (jsonObject.has("sleep") && jsonObject.has("user")) {
            SleepJSONSingle singleResponse = gson.fromJson(json, SleepJSONSingle.class);
            return Collections.singletonList(singleResponse);
        }

        // 3. { "results": [...] } 형태인지 확인
        if (jsonObject.has("results")) {
            SleepJSONMulti multiResponse = gson.fromJson(json, SleepJSONMulti.class);
            return multiResponse.getResults();
        }

        throw new JsonParseException("Unsupported sleep data format: " + json);
    }
}

class WaitingClient {
    private static final String BASE_URL = "https://www.sleep-math.com";
    private static Retrofit retrofit = null;

    public static Retrofit getWaitingClient(Context context, boolean isSleepJSON) {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            // Sleep 데이터를 불러올 때만 SleepDataDeserializer 사용
            if (isSleepJSON) {
                Type sleepListType = new TypeToken<List<SleepJSONSingle>>() {}.getType();
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(sleepListType, new SleepDataDeserializer())
                        .create();
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            } else {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
        }

        return retrofit;
    }
}

// {"results": [...]} 구조를 위한 클래스
class DailySurveyResponse {
    @SerializedName("results")
    private List<DataMood> results;

    // Getter
    public List<DataMood> getResults() { return results; }
}

// WaitingActivity 내에서 작동하는 API
interface WaitingService {
    @GET("/sleepapp/android/")
    Call<List<SleepJSONSingle>> fetchSleeps(@Query("fmt") String format);

    @GET("/sleepapp/daily_survey/")
    Call<DailySurveyResponse> fetchDailySurvey(
            @Query("from") String fromDate,
            @Query("to") String toDate
    );
}