package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class BeginRegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_register);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button beginButton = findViewById(R.id.beginButton);
        beginButton.setOnClickListener(view -> {
            //Move to CheckActivity
            Intent checkIntent = new Intent(BeginRegisterActivity.this, CheckActivity.class);
            startActivity(checkIntent);
        });
    }
}