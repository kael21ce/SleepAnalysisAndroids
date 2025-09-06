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
import androidx.room.Room;

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
import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.AuthInterceptor;
import com.kael21ce.sleepanalysisandroid.data.BlockStatusResponse;
import com.kael21ce.sleepanalysisandroid.data.DataMood;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;
import com.kael21ce.sleepanalysisandroid.data.RetrofitClient;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.kael21ce.sleepanalysisandroid.data.SleepDao;
import com.kael21ce.sleepanalysisandroid.data.SleepUploadPayload;
import com.kael21ce.sleepanalysisandroid.data.Sleep_struct;

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
    private AppDatabase db;
    private List<SleepUploadPayload> sleepUploadPayloadList; // 메서드끼리 공유하도록 전역 변수로 설정

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

        // 1) AppDatabase 불러오기 및 API 불러오기
        db = Room.databaseBuilder(this,
                AppDatabase.class, "sleep_wake").allowMainThreadQueries().build();
        // 수면 데이터 및 설문을 위한 API는 method 내에서 호출
        RetrofitAPI apiService = RetrofitClient.getClient(this).create(RetrofitAPI.class); // is_blocked를 위한 API

        // 2) 수면 데이터 동기화
        performFetchSleepsJSON(() -> {
            Log.d(TAG, "수면 데이터를 서버에서 읽기 완료: # - " + sleepUploadPayloadList.size());
            saveSleepToSleepDao(() -> {
                Log.d(TAG, "수면 데이터를 AppDatabase에 저장 완료");
                // 3) 설문 동기화
                // 4) is_blocked 불러오기
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
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
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

    // 서버 수면 JSON 동기화 및 Core data 적재
    // 실패한 것: sharedPreference에 저장된 값을 사용하도록 처리
    private void performFetchSleepsJSON(LoadSaveCallback callback) {
        final String type = "수면";
        String format = "iso_utc";
        sleepUploadPayloadList = new ArrayList<>();

        Retrofit retrofit = WaitingClient.getWaitingClient(this, true);
        WaitingService apiService = retrofit.create(WaitingService.class);
        apiService.fetchSleeps(format).enqueue(new Callback<List<SleepUploadPayload>>() {
            @Override
            public void onResponse(Call<List<SleepUploadPayload>> call, Response<List<SleepUploadPayload>> response) {
                if (response.code() == 204) {
                    NoContentLog(type);
                    callback.onSucceed();
                    return;
                }

                if (response.isSuccessful()) {
                    List<SleepUploadPayload> sleepData = response.body();
                    SuccessfulLoadingLog(type, sleepData.size());
                    sleepUploadPayloadList = sleepData;
                    callback.onSucceed();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "알 수 없는 에러";
                        FailureLoadingLog(type, response.code(), errorBody);
                        callback.onSucceed();
                    } catch (IOException e) {
                        ParsingFailureLog();
                        callback.onSucceed();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SleepUploadPayload>> call, Throwable t) {
                NetworkLog(t.getMessage());
                callback.onSucceed();
            }
        });
    }

    // 불러온 수면 데이터를 정리해 SleepDao에 업로드
    public long parseServerDate(String sleep) {
        // KST 기준 ISO 8601 형식 formatter
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.KOREA);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        // String을 Date로 변환 -> long(millisecond)으로 변환
        try {
            Date date = formatter.parse(sleep);
            assert date != null;
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void saveSleepToSleepDao(LoadSaveCallback callback) {
        // KST 기준 ISO 8601 형식 formatter
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.KOREA);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -14);
        Date twoWeeksAgo = calendar.getTime();

        /**List<SleepUploadPayload> -> List<Sleep_struct> -> Sleep_struct 순으로 접근
         *  Sleep_struct를 Sleep으로 변환, List<Sleep>을 생성
         */
        int payloadLength = sleepUploadPayloadList.size();
        List<Sleep> sleeps = new ArrayList<>();
        for (int i = 0; i < payloadLength; i++) {
            SleepUploadPayload payload = sleepUploadPayloadList.get(i);
            List<Sleep_struct> sleepList = payload.getSleep();
            Log.d(TAG, i + " 번째 payload의 수면 데이터 수: " + sleepList.size());
            for (Sleep_struct sleep : sleepList) {
                long sleepStart = parseServerDate(sleep.getSleepStart());
                long sleepEnd = parseServerDate(sleep.getSleepEnd());
                // Date parse error가 난 경우는 제외
                if (sleepStart < 0 || sleepEnd < 0) continue;

                // 현재로부터 2주 내에 있는 수면 데이터만 저장
                if (sleepStart >= twoWeeksAgo.getTime() && sleepEnd <= now.getTime()) continue;

                Sleep newSleep = new Sleep();
                newSleep.sleepStart = sleepStart;
                newSleep.sleepEnd = sleepEnd;
                sleeps.add(newSleep);
            }
        }

        // SleepDao에 한번에 List<Sleep>을 저장
        if (db != null) {
            SleepDao sleepDao = db.sleepDao();
            sleepDao.insertAll(sleeps);
        } else {
            Log.e(TAG, "데이터 저장 실패: DB가 초기화되지 않았습니다");
        }
        callback.onSucceed();
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

    /** 데이터를 서버에서 불러오거나 AppDatabase에 저장할 때의 callback
     * onFailure 함수를 통한 UI 작용은 WaitingActivity에서 진행 X - 데이터 로드 및 저장을 기다리기 위한 용도
     */
    private interface LoadSaveCallback {
        void onSucceed();
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

// List<Sleep_struct>만 담겼을 때를 위한 class
class PersonalResponse {
    @SerializedName("sleep")
    private List<Sleep_struct> sleep;

    public List<Sleep_struct> getSleep() {
        return sleep;
    }
}

// List<SleepUploadPayload>가 담겼을 때를 위한 class
class SleepJSONMulti {
    @SerializedName("results")
    private List<SleepUploadPayload> results;

    // Getter
    public List<SleepUploadPayload> getResults() { return  results; }
}

/** {"sleep": [...]}을 가져오는 것을 시도
 * 만약 서버 구조가 바뀌어 { "user": ..., "sleep": [...] }, { "results": [...] } 형태가 와도 SleepUploadPayload로 변경
 */
class SleepDataDeserializer implements JsonDeserializer<List<SleepUploadPayload>> {
    @Override
    public List<SleepUploadPayload> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Gson gson = new Gson();

        // 1. { "results": [...] } 형태인지 확인
        if (jsonObject.has("results")) {
            SleepJSONMulti multiResponse = gson.fromJson(json, SleepJSONMulti.class);
            return multiResponse.getResults();
        }

        // 2. { "user": ..., "sleep": [...] } 형태인지 확인
        if (jsonObject.has("sleep") && jsonObject.has("user")) {
            SleepUploadPayload singleResponse = gson.fromJson(json, SleepUploadPayload.class);
            return Collections.singletonList(singleResponse);
        }

        // 3. { "sleep": [...] } 형태인지 확인
        if (jsonObject.has("sleep")) {
            PersonalResponse personalResponse = gson.fromJson(json, PersonalResponse.class);
            SleepUploadPayload wrappedResponse = new SleepUploadPayload(personalResponse.getSleep());
            return Collections.singletonList(wrappedResponse);
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
                Type sleepListType = new TypeToken<List<SleepUploadPayload>>() {}.getType();
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
    Call<List<SleepUploadPayload>> fetchSleeps(@Query("fmt") String format);

    @GET("/sleepapp/daily_survey/")
    Call<DailySurveyResponse> fetchDailySurvey(
            @Query("from") String fromDate,
            @Query("to") String toDate
    );
}