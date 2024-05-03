package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Get the location where the info button is clicked -> Image & back button
        Intent locationIntent = getIntent();
        int location = locationIntent.getIntExtra("Location",0);
        ImageView guidelineImage = findViewById(R.id.guidelineImage);
        //Back button
        ImageButton infoBackButton = findViewById(R.id.infoBackButton);
        infoBackButton.setOnClickListener(view->{
            finish();
        });
        MainActivity mainActivity = new MainActivity();

        if (location == 0) {
            guidelineImage.setImageDrawable(getDrawable(R.drawable.guideline0));
        } else if (location == 1) {
            guidelineImage.setImageDrawable(getDrawable(R.drawable.guideline1));
        } else if (location == 2) {
            guidelineImage.setImageDrawable(getDrawable(R.drawable.guideline2));
        } else if (location == 3) {
            guidelineImage.setImageDrawable(getDrawable(R.drawable.guideline3));
        } else if (location == 4) {
            guidelineImage.setImageDrawable(getDrawable(R.drawable.guideline4));
        } else {
            guidelineImage.setImageDrawable(getDrawable(R.drawable.guideline5));
        }


    }
}