package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kael21ce.sleepanalysisandroid.data.DataModal;
import com.kael21ce.sleepanalysisandroid.data.DataMood;
import com.kael21ce.sleepanalysisandroid.data.DataSurvey;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SurveyActivity extends AppCompatActivity {
    private int level = 5;
    private int level2 = 5;

    private int surveyLevel = 1;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private static final String survey_key = "SQMood";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        //Text
        TextView surveyTitle = findViewById(R.id.SurveyTitle);
        TextView surveyDescription = findViewById(R.id.SurveyDescription);
        //Back button
        ImageButton backSurveyButton = findViewById(R.id.surveyBackButton);
        Button endSurveyButton = findViewById(R.id.endSurveyButton);
        //Emoji
        ImageView awarenessEmoji = findViewById(R.id.AwarenessEmoji);
        TextView emojiDescription = findViewById(R.id.EmojiDescription);
        //Button
        Button seek1 = findViewById(R.id.seek1);
        Button seek2 = findViewById(R.id.seek2);
        Button seek3 = findViewById(R.id.seek3);
        Button seek4 = findViewById(R.id.seek4);
        Button seek5 = findViewById(R.id.seek5);
        Button seek6 = findViewById(R.id.seek6);
        Button seek7 = findViewById(R.id.seek7);
        Button seek8 = findViewById(R.id.seek8);
        Button seek9 = findViewById(R.id.seek9);
        Button seek10 = findViewById(R.id.seek10);
        ArrayList<Button> buttonArrayList = new ArrayList<>();
        buttonArrayList.add(seek1);
        buttonArrayList.add(seek2);
        buttonArrayList.add(seek3);
        buttonArrayList.add(seek4);
        buttonArrayList.add(seek5);
        buttonArrayList.add(seek6);
        buttonArrayList.add(seek7);
        buttonArrayList.add(seek8);
        buttonArrayList.add(seek9);
        buttonArrayList.add(seek10);

        //Click back button
        backSurveyButton.setOnClickListener(view -> {
            Intent nextIntent = new Intent(SurveyActivity.this, SplashActivity.class);
            nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextIntent);
        });

        //Initial setting
        setSeekColor(buttonArrayList, 5, awarenessEmoji, emojiDescription);

        //Seek Button
        seek1.setOnClickListener(view -> {
            setSeekColor(buttonArrayList, 1, awarenessEmoji, emojiDescription);
        });
        seek2.setOnClickListener(view -> {
            setSeekColor(buttonArrayList, 2, awarenessEmoji, emojiDescription);
        });
        seek3.setOnClickListener(view -> {
            setSeekColor(buttonArrayList, 3, awarenessEmoji, emojiDescription);
        });
        seek4.setOnClickListener(view -> {
            setSeekColor(buttonArrayList, 4, awarenessEmoji, emojiDescription);
        });
        seek5.setOnClickListener(view -> {
            setSeekColor(buttonArrayList, 5, awarenessEmoji, emojiDescription);
        });
        seek6.setOnClickListener(view -> {
            setSeekColor(buttonArrayList, 6, awarenessEmoji, emojiDescription);
        });
        seek7.setOnClickListener(view -> {
            setSeekColor(buttonArrayList, 7, awarenessEmoji, emojiDescription);
        });
        seek8.setOnClickListener(view -> {
            setSeekColor(buttonArrayList, 8, awarenessEmoji, emojiDescription);
        });
        seek9.setOnClickListener(view -> {
            setSeekColor(buttonArrayList, 9, awarenessEmoji, emojiDescription);
        });
        seek10.setOnClickListener(view -> {
            setSeekColor(buttonArrayList, 10, awarenessEmoji, emojiDescription);
        });

        //endSurveyButton
        Intent sentIntent = getIntent();
        if(sentIntent.getIntExtra("firstDone", 0) != 0){
//            level = sentIntent.getIntExtra("firstDone", 0);
            surveyLevel = 2;
            surveyTitle.setText("어제 하루 얼마나 개운하셨나요?");
            surveyDescription.setText("어제의 전반적인 개운한 정도를 평가해주세요");
        }
        Log.v("SURVEY LEVEL", String.valueOf(surveyLevel));
        if(surveyLevel == 1) {
            endSurveyButton.setOnClickListener(view -> {
                Intent nextIntent = new Intent(this, SplashActivity.class);
                nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                sendSurvey();
                startActivity(nextIntent);
            });
        }else{
            endSurveyButton.setOnClickListener(view -> {
                //Get the survey day
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                Intent endIntent = new Intent(SurveyActivity.this, MainActivity.class);
                endIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                Bundle moodData = sentIntent.getBundleExtra("moodData");
                sendMood(moodData.getInt("sleep_quality"), moodData.getInt("mood_high"), moodData.getInt("mood_low"), moodData.getInt("mood_anx"), moodData.getInt("mood_irr"));
                editor.putInt(survey_key, day).apply();
                //Need to add level to dataset
                startActivity(endIntent);
            });
        }
    }
    //Set the level, seek button color and emoji
    private void setSeekColor(ArrayList<Button> buttonArrayList, int position, ImageView emoji, TextView description) {
        int size = buttonArrayList.size();
        if (position > size) {
            return;
        } else {
            //Set the color
            for (int i = 0; i < size; i++) {
                if (i + 1 <= position) {
                    buttonArrayList.get(i).setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.black, null));
                } else {
                    buttonArrayList.get(i).setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.gray_3, null));
                }
            }
            //Set the level
            if(surveyLevel == 1) {
                level = position;
            }else{
                level2 = position;
            }
            //Set the emoji
            description.setText("개운한 정도: " + position + " / 10");
            if (position == 1) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
            } else if (2 == position || position == 3) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sad, null));
            } else if (4 ==position || position ==5) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.tired, null));
            } else if (6 == position || position == 7) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.smile, null));
            } else if (8 == position || position == 9) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.laugh, null));
            } else if (position == 10) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.kiss, null));
            } else {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
            }
        }
    }

    private void sendSurvey(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sleep-math.com/sleepapp/")
                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())
                // at last we are building our retrofit builder.
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        String username = sharedPref.getString("User_Name", "tester33");
        long time = System.currentTimeMillis();
        long sleep_onset = sharedPref.getLong("sleepOnset", time);
        long work_onset = sharedPref.getLong("workOnset", time);
        long work_offset = sharedPref.getLong("workOffset", time);

        DataSurvey survey = new DataSurvey(username, sleep_onset, work_onset, work_offset, getLevel(), time);
        Call<DataSurvey> call = retrofitAPI.createSurvey(survey);
        call.enqueue(new Callback<DataSurvey>() {
            @Override
            public void onResponse(Call<DataSurvey> call, Response<DataSurvey> response) {
                // this method is called when we get response from our api.
                Locale currentLocale = Locale.getDefault();
                String language = currentLocale.getLanguage();
                if(response.code() <= 300) {
                    if (language.equals("ko")) {
                        Toast.makeText(SurveyActivity.this, "데이터가 전송되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SurveyActivity.this, "Data added to API", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (language.equals("ko")) {
                        Toast.makeText(SurveyActivity.this, "데이터 전송에 실패했습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SurveyActivity.this, "Data sending failed", Toast.LENGTH_SHORT).show();
                    }
                    // we are getting response from our body
                    // and passing it to our modal class.
                    DataSurvey responseFromAPI = response.body();

                    // on below line we are getting our data from modal class and adding it to our string.
                    String responseString = "Response Code : " + response.code() + "\nName : " + "\n";
                    Log.v("RESPONSE", responseString);
                }
            }

            @Override
            public void onFailure(Call<DataSurvey> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
                Log.v("ERROR", "Error found is : " + t.getMessage());
            }
        });
    }

    private void sendMood(Integer sleep_quality, Integer mood_high, Integer mood_low, Integer mood_anx, Integer mood_irr) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sleep-math.com/sleepapp/")
                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                // at last we are building our retrofit builder.
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        String username = sharedPref.getString("User_Name", "tester33");
        long time = System.currentTimeMillis();

        DataMood mood = new DataMood(username,getLevel2(), sleep_quality, mood_high, mood_low, mood_anx, mood_irr, time);
        Call<DataMood> call = retrofitAPI.createMood(mood);
        call.enqueue(new Callback<DataMood>() {
            @Override
            public void onResponse(Call<DataMood> call, Response<DataMood> response) {
                // this method is called when we get response from our api.
                if (response.code() <= 300) {
                    Toast.makeText(SurveyActivity.this, "Data added to API", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SurveyActivity.this, "Data sending failed", Toast.LENGTH_SHORT).show();
                    // we are getting response from our body
                    // and passing it to our modal class.
                    DataMood responseFromAPI = response.body();

                    // on below line we are getting our data from modal class and adding it to our string.
                    String responseString = "Response Code : " + response.code() + "\nName : " + "\n";
                    Log.v("RESPONSE", responseString);
                }
            }

            @Override
            public void onFailure(Call<DataMood> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
                Log.v("ERROR", "Error found is : " + t.getMessage());
            }
        });
    }

    //Return level
    private int getLevel() {
        return this.level;
    }
    private int getLevel2() {return this.level2;}
}