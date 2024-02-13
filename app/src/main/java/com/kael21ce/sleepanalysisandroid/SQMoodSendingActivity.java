package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kael21ce.sleepanalysisandroid.data.DataMood;
import com.kael21ce.sleepanalysisandroid.data.DataSurvey;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SQMoodSendingActivity extends AppCompatActivity {

    int surveyType = 0;
    int position = 1;
    boolean isChosen = false;
    private static final String name = "SurveyType";

    Bundle moodData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqmood_sending);

        TextView sQMoodTitle = findViewById(R.id.sQMoodTitle);
        TextView sQDescription = findViewById(R.id.sQMoodDescription);
        LinearLayout firstButton = findViewById(R.id.firstButton);
        TextView firstContent = findViewById(R.id.firstContent);
        LinearLayout secondButton = findViewById(R.id.secondButton);
        TextView secondContent = findViewById(R.id.secondContent);
        LinearLayout thirdButton = findViewById(R.id.thirdButton);
        TextView thirdContent = findViewById(R.id.thirdContent);
        LinearLayout fourthButton = findViewById(R.id.fourthButton);
        TextView fourthContent = findViewById(R.id.fourthContent);
        LinearLayout fifthButton = findViewById(R.id.fifthButton);
        TextView fifthContent = findViewById(R.id.fifthContent);
        Button sQMoodButton = findViewById(R.id.SQMoodButton);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Button click
        firstButton.setOnClickListener(view -> {
            position = 4;
            isChosen = true;
            firstButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_black_stroke, null));
            firstContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.black, null));
            secondButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            secondContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            thirdButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            thirdContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            fourthButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            fourthContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            fifthButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            fifthContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            sQMoodButton.setEnabled(true);
            sQMoodButton.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                    R.color.blue_1, null));
        });

        secondButton.setOnClickListener(view -> {
            position = 3;
            isChosen = true;
            firstButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            firstContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            secondButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_black_stroke, null));
            secondContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.black, null));
            thirdButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            thirdContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            fourthButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            fourthContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            fifthButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            fifthContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            sQMoodButton.setEnabled(true);
            sQMoodButton.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                    R.color.blue_1, null));
        });

        thirdButton.setOnClickListener(view -> {
            position = 2;
            isChosen = true;
            firstButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            firstContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            secondButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            secondContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            thirdButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_black_stroke, null));
            thirdContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.black, null));
            fourthButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            fourthContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            fifthButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            fifthContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            sQMoodButton.setEnabled(true);
            sQMoodButton.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                    R.color.blue_1, null));
        });

        fourthButton.setOnClickListener(view -> {
            position = 1;
            isChosen = true;
            firstButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            firstContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            secondButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            secondContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            thirdButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            thirdContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            fourthButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_black_stroke, null));
            fourthContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.black, null));
            fifthButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            fifthContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            sQMoodButton.setEnabled(true);
            sQMoodButton.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                    R.color.blue_1, null));
        });

        fifthButton.setOnClickListener(view -> {
            position = 0;
            isChosen = true;
            firstButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            firstContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            secondButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            secondContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            thirdButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            thirdContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            fourthButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_gray_stroke, null));
            fourthContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.gray_4, null));
            fifthButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.corner_8_black_stroke, null));
            fifthContent.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.black, null));
            sQMoodButton.setEnabled(true);
            sQMoodButton.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                    R.color.blue_1, null));
        });

        if (!isChosen) {
            sQMoodButton.setEnabled(false);
            sQMoodButton.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                    R.color.blue_2, null));
        } else {
            sQMoodButton.setEnabled(true);
            sQMoodButton.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                    R.color.blue_1, null));
        }

        //Get the sent survey type
        Intent sentIntent = getIntent();
        this.surveyType = sentIntent.getIntExtra(name, 0);
        this.moodData = sentIntent.getBundleExtra("moodData");

        if (surveyType == 0) {
            sQMoodTitle.setText("얼마나 우울한 감정이 들었나요?");
            sQDescription.setText("오늘 느낀 가장 우울한 정도를 입력해주세요");
            fifthButton.setVisibility(View.INVISIBLE);
            firstContent.setText("매우 심하다");
            secondContent.setText("심하다");
            thirdContent.setText("가볍다");
            fourthContent.setText("없다");
            sQMoodButton.setOnClickListener(view -> {
                moodData.putInt("mood_high", position);
                Intent nextIntent = new Intent(this, SQMoodSendingActivity.class);
                nextIntent.putExtra(name, 1);
                nextIntent.putExtra("moodData", moodData);
                startActivity(nextIntent);
                //Save the survey result

            });
        } else if (surveyType == 1) {
            sQMoodTitle.setText("얼마나 들뜨는 감정이 들었나요?");
            sQDescription.setText("오늘 느낀 가장 들뜬 정도를 입력해주세요");
            fifthButton.setVisibility(View.INVISIBLE);
            firstContent.setText("매우 심하다");
            secondContent.setText("심하다");
            thirdContent.setText("가볍다");
            fourthContent.setText("없다");
            sQMoodButton.setOnClickListener(view -> {
                moodData.putInt("mood_low", position);
                Intent nextIntent = new Intent(this, SQMoodSendingActivity.class);
                nextIntent.putExtra(name, 2);
                nextIntent.putExtra("moodData", moodData);
                startActivity(nextIntent);
                //Save the survey result
            });
        } else if (surveyType == 2) {
            sQMoodTitle.setText("얼마나 불안한 감정이 들었나요?");
            sQDescription.setText("오늘 느낀 가장 불안한 정도를 입력해주세요");
            fifthButton.setVisibility(View.INVISIBLE);
            firstContent.setText("매우 심하다");
            secondContent.setText("심하다");
            thirdContent.setText("가볍다");
            fourthContent.setText("없다");
            sQMoodButton.setOnClickListener(view -> {
                moodData.putInt("mood_anx", position);
                Intent nextIntent = new Intent(this, SQMoodSendingActivity.class);
                nextIntent.putExtra(name, 3);
                nextIntent.putExtra("moodData", moodData);
                startActivity(nextIntent);
                //Save the survey result
            });
        } else if (surveyType == 3) {
            sQMoodTitle.setText("얼마나 짜증나는 감정이 들었나요?");
            sQDescription.setText("오늘 느낀 가장 짜증나는 정도를 입력해주세요");
            fifthButton.setVisibility(View.INVISIBLE);
            firstContent.setText("매우 심하다");
            secondContent.setText("심하다");
            thirdContent.setText("가볍다");
            fourthContent.setText("없다");
            sQMoodButton.setOnClickListener(view -> {
                moodData.putInt("mood_irr", position);
                Intent nextIntent = new Intent(this, SQMoodSendingActivity.class);
                nextIntent.putExtra(name, 4);
                nextIntent.putExtra("moodData", moodData);
                startActivity(nextIntent);
                //Save the survey result
            });
        } else {
            sQMoodTitle.setText("어제 밤은 어떠셨나요?");
            sQDescription.setText("어제 밤 주무신 수면의 질을 평가해주세요");
            fifthButton.setVisibility(View.VISIBLE);
            firstContent.setText("매우 좋음");
            secondContent.setText("좋음");
            thirdContent.setText("보통");
            fourthContent.setText("좋지 않음");
            fifthContent.setText("매우 좋지 않음");
            sQMoodButton.setOnClickListener(view -> {
                moodData.putInt("sleep_quality", position);
                Intent endIntent = new Intent(this, SurveyActivity.class);
//                endIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                endIntent.putExtra("firstDone", 1);
                endIntent.putExtra("moodData", moodData);
                startActivity(endIntent);
                //Save the survey result
//                sendMood(position+1, moodData.getInt("mood_high"), moodData.getInt("mood_low"), moodData.getInt("mood_anx"), moodData.getInt("mood_irr"));

            });
        }

    }
}