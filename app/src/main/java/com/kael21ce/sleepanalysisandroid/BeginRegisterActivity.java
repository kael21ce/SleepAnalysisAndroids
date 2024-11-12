package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class BeginRegisterActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_register);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Delete SharedPreference from log out
        Intent logOutIntent = getIntent();
        if (logOutIntent.getBooleanExtra("LogOut", false)) {
            SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
            Log.v("BeginRegister", "Log out and all data were deleted");
        }

        Button beginButton = findViewById(R.id.beginButton);
        beginButton.setOnClickListener(view -> {
            //Move to CheckActivity
            Intent checkIntent = new Intent(BeginRegisterActivity.this, CheckActivity.class);
            startActivity(checkIntent);
        });
    }

    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "\'뒤로\' 버튼을 한 번 더 누르시면 종료됩니다",
                Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}