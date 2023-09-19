package com.kael21ce.sleepanalysisandroid;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();
    RecommendFragment recommendFragment = new RecommendFragment();
    SettingFragment settingFragment = new SettingFragment();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, homeFragment).commit();
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>2Sleep</font>"));

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.tabHome) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.mainFrame, homeFragment).commit();
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>2Sleep</font>"));
                            return true;
                        } else if (item.getItemId() == R.id.tabSchedule) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.mainFrame, scheduleFragment).commit();
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>수면 기록</font>"));
                            return true;
                        } else if (item.getItemId() == R.id.tabRecommend) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.mainFrame, recommendFragment).commit();
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>추천 수면</font>"));
                            return true;
                        } else if (item.getItemId() == R.id.tabSetting) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.mainFrame, settingFragment).commit();
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>설정</font>"));
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
        );

    }
}
