package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.kael21ce.sleepanalysisandroid.data.DataSurvey;
import com.kael21ce.sleepanalysisandroid.data.DataUser;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StartActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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