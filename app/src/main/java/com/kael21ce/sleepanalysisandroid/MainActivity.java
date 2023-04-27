package com.kael21ce.sleepanalysisandroid;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();
    RecommendFragment recommendFragment = new RecommendFragment();
    SettingFragment settingFragment = new SettingFragment();

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getSupportFragmentManager().beginTransaction().add(R.id.mainFrame, homeFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case 1000004:
                            getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame,
                                    homeFragment).commit();
                            break;
                        case 1000002:
                            getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame,
                                    scheduleFragment).commit();
                            break;
                        case 1000007:
                            getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame,
                                    recommendFragment).commit();
                            break;
                        case 1000003:
                            getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame,
                                    settingFragment).commit();
                            break;
                    }
                    return true;
                }
        );
    }
}
