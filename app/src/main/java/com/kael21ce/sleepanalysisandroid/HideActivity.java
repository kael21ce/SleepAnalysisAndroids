package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class HideActivity extends AppCompatActivity {

    private static final String password = "7890";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide);

        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Back button
        ImageButton hideBackButton = findViewById(R.id.HideBackButton);
        hideBackButton.setOnClickListener(view -> {
            finish();
        });

        //Check password
        boolean isHidden = sharedPref.getBoolean("isHidden", false);
        Button hideButton = findViewById(R.id.HideCheckButton);
        EditText hidePassword = findViewById(R.id.HidePassword);
        hideButton.setOnClickListener(view -> {
            String writtenPassword = hidePassword.getEditableText().toString();
            if (!writtenPassword.isEmpty() || !writtenPassword.isBlank()) {
                if (writtenPassword.equals(password)) {
                    if (isHidden) {
                        Toast.makeText(this, "수면 추천을 공개합니다", Toast.LENGTH_SHORT).show();
                        editor.putBoolean("isHidden", false).apply();
                    } else {
                        Toast.makeText(this, "수면 추천을 잠급니다", Toast.LENGTH_SHORT).show();
                        editor.putBoolean("isHidden", true).apply();
                    }
                    //Move to SettingFragment
                    Intent settingIntent = new Intent(this, MainActivity.class);
                    startActivity(settingIntent);
                } else {
                    Toast.makeText(this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            }

        });
    }
}