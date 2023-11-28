package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();


        //Get intent from CheckActivity
        Intent emailIntent = getIntent();
        Button startButton = findViewById(R.id.startButton);

        //Change the user name and user email in MainActivity
        String user_email = emailIntent.getStringExtra("User_Email");
        String user_name = user_email.substring(0, user_email.indexOf("@"));
        startButton.setOnClickListener(view -> {

            //Add to sharedPreference
            editor.putString("User_Email", user_email);
            editor.putString("User_Name", user_name);
            editor.apply();

            //Move to MainActivity
            Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        });
    }
}