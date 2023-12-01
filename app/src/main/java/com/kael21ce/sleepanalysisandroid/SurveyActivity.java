package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SurveyActivity extends AppCompatActivity {
    private int level = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Back button
        ImageButton backSurveyButton = findViewById(R.id.surveyBackButton);
        TextView nextText = findViewById(R.id.nextText);
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
            finish();
        });

        //Click nextText
        nextText.setOnClickListener(view -> {
            Intent nextIntent = new Intent(SurveyActivity.this, MainActivity.class);
            nextIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(nextIntent);
        });

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
        endSurveyButton.setOnClickListener(view -> {
            Intent endIntent = new Intent(SurveyActivity.this, MainActivity.class);
            endIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            //Need to add level to dataseet
            startActivity(endIntent);
        });
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
            level = position;
            //Set the emoji
            if (position == 1) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
                description.setText("매우 졸림");
            } else if (2 == position || position == 3) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.sad, null));
                description.setText("졸림");
            } else if (4 ==position || position ==5) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.tired, null));
                description.setText("평소와 같음");
            } else if (6 == position || position == 7) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.smile, null));
                description.setText("조금 개운함");
            } else if (8 == position || position == 9) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.laugh, null));
                description.setText("개운함");
            } else if (position == 10) {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.kiss, null));
                description.setText("매우 개운함");
            } else {
                emoji.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.puke, null));
                description.setText("매우 졸림");
            }
        }
    }

    //Return level
    private int getLevel() {
        return this.level;
    }
}