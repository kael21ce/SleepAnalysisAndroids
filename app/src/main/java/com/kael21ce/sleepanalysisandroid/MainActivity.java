package com.kael21ce.sleepanalysisandroid;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.AppDatabase;
import com.kael21ce.sleepanalysisandroid.data.AppDatabaseSingleton;
import com.kael21ce.sleepanalysisandroid.data.Awareness;
import com.kael21ce.sleepanalysisandroid.data.CombineResult;
import com.kael21ce.sleepanalysisandroid.data.HealthConnectManager;
import com.kael21ce.sleepanalysisandroid.data.ProcessingAPI;
import com.kael21ce.sleepanalysisandroid.data.Sleep;
import com.kael21ce.sleepanalysisandroid.data.SleepDao;
import com.kael21ce.sleepanalysisandroid.data.V0;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    ScheduleFragment scheduleFragment = new ScheduleFragment();
    RecommendFragment recommendFragment = new RecommendFragment();
    SettingFragment settingFragment = new SettingFragment();
    private boolean creation = true;
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy" + " HH:mm", Locale.getDefault());
    private static final String TAG = "MainActivity";
    private static final String CHECK_CHANNEL_ID = "check_recommend", SURVEY_CHANNEL_ID = "alertness_survey";
    HealthConnectManager healthConnectManager;
    //health connect
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private static final String NotifyKey = "Notify_At";
    private static final String NotifySettingKey = "IsNotifySet";
    private static final String RecommendName = "Recommend";
    private static final String SurveyName1 = "Survey1", SurveyName2 = "Survey2", SurveyName3 = "Survey3", SurveyName4 = "Survey4";
    //Time after click back button
    private OnBackPressedCallback callback;
    private boolean doubleBackToExitPressedOnce = false;

    private static final int PERMISSION_REQUEST_READ_LOCATION = 0x00000001;

    //for fragment too
    private long mainSleepStart, mainSleepEnd, napSleepStart, napSleepEnd;
    private boolean isearlysleep, isenoughsleep;
    private long sleepOnset, workOnset, workOffset, sleepOnsetShow;
    private long lastSleepUpdate, lastDataUpdate, lastBackendUpdate;

    AppDatabase db;
    private List<Sleep> sleeps;
    private List<Awareness> awarenesses, sleepAwarenesses;
    private CombineResult combineResult;
    String email, username;
    long now, nineHours;
    long twoWeeks = (1000*60*60*24*14), oneDay = 1000*60*60*24, oneHour = 1000*60*60;
    long fiveMinutesToMil = (1000*60*5);
    SleepDao sleepDao;

    ArrayList<BarEntry> barEntries;


    private static final String survey_name = "SurveyType";
    private static final String survey_key = "SQMood";
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    public static ArrayList<Activity> surveyList = new ArrayList<>();
    public ArrayList<Activity> surveyList() {
        return surveyList;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);

        Log.v("MainActivity", "onCreate() is called");

        // 뒤로가기 callback 생성
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity();
                    return;
                }
                doubleBackToExitPressedOnce = true;
                Toast.makeText(MainActivity.this, "'뒤로' 버튼을 한 번 더 누르시면 종료됩니다",
                        Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        healthConnectManager = new HealthConnectManager(getApplicationContext());
        awarenesses = Collections.synchronizedList(new ArrayList<>());
        sleepAwarenesses = Collections.synchronizedList(new ArrayList<>());

        //get the shared preferences variable
        sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        // Set timezone
        TimeZone timeZone = TimeZone.getDefault();
        sdfDateTime.setTimeZone(timeZone);

        //Set the initial notification time
        if (!sharedPref.contains(NotifyKey)) {
            editor.putString(NotifyKey, "21:00").apply();
        }

        // Load db
        db = AppDatabaseSingleton.getInstance(this);

        //Create channel
        createNotificationChannel(this);
        createSurveyChannel(this);


        //Request the permission of notification in context if API >= 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                //Request again with rational
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    Toast.makeText(this, "추천 수면에 대한 알림을 받기 위해서 권한을 설정해야 합니다.",
                            Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_READ_LOCATION);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_READ_LOCATION);
                }
            }
        }

        // isOnboarding을 SharedPreference에서 확인하여 로그인 여부 확인
        if (sharedPref.contains("User_Name") && sharedPref.contains("User_Email")) {
            editor.putBoolean("isOnboarding", false); // 이전 버전의 사용자가 갑자기 로그아웃되는 경우 방지
        }
        if (!sharedPref.contains("isOnboarding")) {
            editor.putBoolean("isOnboarding", true);
        }
        if (sharedPref.getBoolean("isOnboarding", true)) {
            Intent signIntent = new Intent(MainActivity.this, BeginRegisterActivity.class);
            startActivity(signIntent);
        }

        //Show action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }

        //this is in GMT
        // nineHours represent time difference between current timezone and UTC
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime zoneNow = ZonedDateTime.now(zone);
        ZoneOffset offset = zoneNow.getOffset();
        nineHours = (1000L * offset.getTotalSeconds());

        now = System.currentTimeMillis();

        //update variables
        lastSleepUpdate = sharedPref.getLong("lastSleepUpdate", now - twoWeeks);
        lastDataUpdate = sharedPref.getLong("lastDataUpdate", now - twoWeeks);
        lastBackendUpdate = sharedPref.getLong("lastBackendUpdate", now - twoWeeks);
        email = sharedPref.getString("User_Email", "tester33");
        username = sharedPref.getString("User_Name", "tester33");

        combineResult = ProcessingAPI.run(this, sharedPref);
        sleeps = combineResult.getSleeps();
        List<V0> v0s = combineResult.getV0s();
        barEntries = combineResult.getBarEntries();
        awarenesses = combineResult.getAwarenesses();
        sleepAwarenesses = combineResult.getSleepAwarenesses();

        // 사용자의 근무 및 수면 설정값 가져오기
        sleepOnset = sharedPref.getLong("sleepOnset", now);
        workOnset = sharedPref.getLong("workOnset", now);
        workOffset = sharedPref.getLong("workOffset", now);
        sleepOnsetShow = sharedPref.getLong("sleepOnsetShow", now);
        isenoughsleep = sharedPref.getBoolean("enoughSleep", false);
        isearlysleep = sharedPref.getBoolean("earlySleep", false);

        // 계산된 추천 수면값을 불러오기
        mainSleepStart = sharedPref.getLong("mainSleepStart", now);
        mainSleepEnd = sharedPref.getLong("mainSleepEnd", now);
        napSleepStart = sharedPref.getLong("napSleepStart", now);
        napSleepEnd = sharedPref.getLong("napSleepEnd", now);


        // 오후 12시 이후 mood와 sleep quality 관련 설문 진행
        Calendar calendar_12 = Calendar.getInstance();
        int hour_12 = calendar_12.get(Calendar.HOUR_OF_DAY);
        int day_12 = calendar_12.get(Calendar.DAY_OF_MONTH);

        if (!sharedPref.contains(survey_key)) {
            editor.putInt(survey_key, 0).apply();
        }
        int surveyDay = sharedPref.getInt(survey_key, 0);
        boolean survey_1 = surveyDay != day_12 && (sharedPref.contains("User_Name") && sharedPref.contains("User_Email"));
        boolean survey_2 = !sharedPref.getString("User_Name","UserName").equals("UserName");
        boolean survey_3 = hour_12 >= 12;
        Runnable afterTransaction = () -> {
        if (survey_1) {
            if (survey_2) {
                if (survey_3) {
                    // 근무 일정 변경 관련 alert dialog 띄우기 -> 설문 진행
                    View dimBackground = findViewById(R.id.dimBackgroundMain);
                    ActionBar actionBar = getSupportActionBar();
                    showAlertDialog(dimBackground, actionBar, bottomNavigationView);
                }
            }
        }
        };

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        boolean isSchedule = sharedPref.getBoolean("isSchedule", false);
        if(isSchedule){
            editor.putBoolean("isSchedule", false);
            editor.apply();
            replaceFragment(1, false, true, afterTransaction);

            if (getIntent() != null) {
                Intent scheduleIntent = getIntent();
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();

                int year = scheduleIntent.getIntExtra("Year", calendar.get(Calendar.YEAR));
                int month = scheduleIntent.getIntExtra("Month", calendar.get(Calendar.MONTH));
                int day = scheduleIntent.getIntExtra("Day", calendar.get(Calendar.DAY_OF_MONTH));

                Log.v(TAG, "Selected: " + year + "-" + (month+1) + "-" + day);

                Bundle scheduleBundle = new Bundle();
                scheduleBundle.putInt("Year", year);
                scheduleBundle.putInt("Month", month);
                scheduleBundle.putInt("Day", day);
                scheduleFragment.setArguments(scheduleBundle);
            }
        }else {
            replaceFragment(0, false, true, afterTransaction);
        }
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>SleepWake</font>"));

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.tabHome) {
                            replaceFragment(0, true, false, afterTransaction);
                            return true;
                        } else if (item.getItemId() == R.id.tabSchedule) {
                            replaceFragment(1, true, false, afterTransaction);
                            return true;
                        } else if (item.getItemId() == R.id.tabRecommend) {
                            replaceFragment(2, true, false, afterTransaction);
                            return true;
                        } else if (item.getItemId() == R.id.tabSetting) {
                            replaceFragment(3, true, false, afterTransaction);
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
        );

        Log.v("MainActivity", "Is notification on: " +
                sharedPref.getBoolean("isNotifyOn", true));

        //Send notification
        if (!sharedPref.contains(NotifySettingKey)) {
            editor.putBoolean(NotifySettingKey, false).apply();
        }
        boolean isNotifySet = sharedPref.getBoolean(NotifySettingKey, false);
        if (!isNotifySet) {
            sendNotification(sharedPref);
            editor.putBoolean(NotifySettingKey, true).apply();
        }

        prefListener = (sharedPref, key) -> {
            if (key != null) {
                if (key.equals("isNotifyOn")) {
                    sendNotification(sharedPref);
                    Log.v(TAG, "SharedPreference listener is called 1");
                }
                if (key.equals(NotifyKey)) {
                    sendNotification(sharedPref);
                    Log.v(TAG, "SharedPreference listener is called 2");
                }
                if (key.equals("workOnset") || key.equals("workOffset") || key.equals("alertTime1")
                        || key.equals("alertTime2") || key.equals("alertTime3")) {
                    sendNotification(sharedPref);
                    Log.v(TAG, "SharedPreference listener is called 3");
                }
            }
        };
        sharedPref.registerOnSharedPreferenceChangeListener(prefListener);
    }

    // Fragment replace method -> await callback
    public void replaceFragment(int fragmentNumber, boolean setTitle, boolean setBottom, Runnable runnable) {
        if (fragmentNumber == 0) {
            // HomeFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFrame, homeFragment)
                    .runOnCommit(runnable)
                    .commit();
            if (setTitle) {
                getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>SleepWake</font>"));
            }
            if (setBottom) {
                setBottomNaviItem(R.id.tabHome);
            }
        } else if (fragmentNumber == 1) {
            // ScheduleFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFrame, scheduleFragment)
                    .runOnCommit(runnable)
                    .commit();
            if (setTitle) {
                getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>수면 기록</font>"));
            }
            if (setBottom) {
                setBottomNaviItem(R.id.tabSchedule);
            }
        } else if (fragmentNumber == 2) {
            // RecommendFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFrame, recommendFragment)
                    .runOnCommit(runnable)
                    .commit();
            if (setTitle) {
                getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>일정 변경</font>"));
            }
            if (setBottom) {
                setBottomNaviItem(R.id.tabRecommend);
            }
        } else if (fragmentNumber == 3) {
            // SettingFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainFrame, settingFragment)
                    .runOnCommit(runnable)
                    .commit();
            if (setTitle) {
                getSupportActionBar().setTitle(Html.fromHtml("<font color='#223047'>설정</font>"));
            }
            if (setBottom) {
                setBottomNaviItem(R.id.tabSetting);
            }
        }
    }

    //Create channel for notification of recommendation
    public void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHECK_CHANNEL_ID,
                    "Check Recommendation", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    //Create channel for notification for survey
    public void createSurveyChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(SURVEY_CHANNEL_ID,
                    "Alertness Survey", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void sendNotification(SharedPreferences sharedPref) {
        long now = System.currentTimeMillis();

        //Start worker with delay
        String notifyAt = sharedPref.getString(NotifyKey, "21:00");
        long targetTime = timeToSeconds(notifyAt);
        long delay = targetTime - now;
        if (delay < 0) {
            delay += oneDay;
        }
        Log.v(TAG, "Delay of the notification: " + delay);

        PeriodicWorkRequest pushRequest = new PeriodicWorkRequest.Builder(PushWorker.class,
                24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(RecommendName,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, pushRequest);

        /*
        //Send notification for survey in four time
        long surveyTime, surveyDelay1, surveyDelay2, surveyDelay3;
        //1. work onset
        if (sharedPref.contains("alertTime1")) {
            surveyTime = timeToSeconds(sharedPref.getString("alertTime1", "9:00"));
            surveyDelay1 = surveyTime - now;
        } else if (sharedPref.contains("workOnset")) {
            surveyTime = getWorkOnset();
            surveyDelay1 = surveyTime - now - oneDay;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(surveyTime);
            Date surveyDate = calendar.getTime();
            editor.putString("alertTime1", sdfSimple.format(surveyDate)).apply();
        } else {
            surveyTime = timeToSeconds("9:00");
            surveyDelay1 = surveyTime - now;

            editor.putString("alertTime1", "9:00").apply();
        }
        if (surveyDelay1 < 0) {
            surveyDelay1 += oneDay;
        }
        PeriodicWorkRequest surveyRequest1 = new PeriodicWorkRequest.Builder(SurveyWorker.class,
                24, TimeUnit.HOURS)
                .setInitialDelay(surveyDelay1, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(SurveyName1,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, surveyRequest1);
        Log.v(TAG, "Survey delay 1: " + surveyDelay1);

        //2. middle of work onset and offset
        if (sharedPref.contains("alertTime2")) {
            surveyTime = timeToSeconds(sharedPref.getString("alertTime2", "13:00"));
            surveyDelay2 = surveyTime - now;
        }
        else if (sharedPref.contains("workOnset") && sharedPref.contains("workOffset")) {
            surveyTime = (getWorkOnset() + getWorkOffset()) / 2;
            surveyDelay2 = surveyTime - now - oneDay;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(surveyTime);
            Date surveyDate = calendar.getTime();
            editor.putString("alertTime2", sdfSimple.format(surveyDate)).apply();
        } else {
            surveyTime = timeToSeconds("13:00");
            surveyDelay2 = surveyTime - now;
            editor.putString("alertTime2", "13:00").apply();
        }
        if (surveyDelay2 < 0) {
            surveyDelay2 += oneDay;
        }
        PeriodicWorkRequest surveyRequest2 = new PeriodicWorkRequest.Builder(SurveyWorker.class,
                24, TimeUnit.HOURS)
                .setInitialDelay(surveyDelay2, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(SurveyName2,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, surveyRequest2);
        Log.v(TAG, "Survey delay 2: " + surveyDelay2);

        //3. work offset
        if (sharedPref.contains("alertTime3")) {
            surveyTime = timeToSeconds(sharedPref.getString("alertTime3", "18:00"));
            surveyDelay3 = surveyTime - now;
        }
        else if(sharedPref.contains("workOffset")) {
            surveyTime = getWorkOffset();
            surveyDelay3 = surveyTime - now - oneDay;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(surveyTime);
            Date surveyDate = calendar.getTime();
            editor.putString("alertTime3", sdfSimple.format(surveyDate)).apply();
        } else {
            surveyTime = timeToSeconds("18:00");
            surveyDelay3 = surveyTime - now;
            editor.putString("alertTime3", "18:00").apply();
        }
        if (surveyDelay3 < 0) {
            surveyDelay3 += oneDay;
        }
        PeriodicWorkRequest surveyRequest3 = new PeriodicWorkRequest.Builder(SurveyWorker.class,
                24, TimeUnit.HOURS)
                .setInitialDelay(surveyDelay3, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(SurveyName3,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, surveyRequest3);
        Log.v(TAG, "Survey delay 3: " + surveyDelay3);

         */
    }

    //Change "HH:mm" to milliseconds
    public long timeToSeconds(String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        LocalTime time = LocalTime.parse(value, formatter);
        LocalDate currentDate = LocalDate.now();
        ZonedDateTime dateTime = ZonedDateTime.of(currentDate, time, ZoneId.systemDefault());

        return dateTime.toInstant().toEpochMilli();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh_button) {
            View decorView = getWindow().getDecorView();
            View menuItemView = decorView.findViewById(id);
            if (menuItemView != null) {
                //rotate
                Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);
                menuItemView.startAnimation(rotateAnimation);

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    //refresh through calling splashActivity
                    finish();
                    Intent refreshIntent = new Intent(MainActivity.this, SplashActivity.class);
                    startActivity(refreshIntent);
                }, 400);
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Broadcast receiver for the dim effect
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String hex = intent.getStringExtra("COLOR");
            BottomNavigationView view = findViewById(R.id.bottomNavigationView);
            view.setItemBackground(new ColorDrawable(Color.parseColor(hex)));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("DIM_EFFECT");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("MainActivity", "onStart is called");
        if (creation) {
            creation = false;
        } else {
            Log.v("RESUMING", "RESUMING");
            db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "sleep_wake").allowMainThreadQueries().build();
            //get the shared preferences variable
            sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
            combineResult = ProcessingAPI.run(getApplicationContext(), sharedPref);
        }
    }



    @Override
    public void onDestroy(){
        super.onDestroy();
        sharedPref.unregisterOnSharedPreferenceChangeListener(prefListener);
        Log.v(TAG, "onDestroy() is called");
    }

    public double getAwarenessValue(double H, double n, double y, double x){
        double coef_y = 0.8, coef_x = -0.16, v_vh = 1.01;
        double C = 3.37*0.5*(1+coef_y*y + coef_x * x);
        double D_up = (2.46+10.2+C)/v_vh;
        double awareness = D_up - H;
        return awareness;
    }

    public boolean isOverlap(List<Sleep> sleeps, Sleep sleepX, int sleepEx){
        for(Sleep sleep: sleeps){
            if(sleepEx == sleep.sleep_id){
                continue;
            }
            if(!(sleepX.sleepEnd <= sleep.sleepStart || sleepX.sleepStart >= sleep.sleepEnd)){
                return true;
            }
        }
        return false;
    }
    public ArrayList<BarEntry> getBarEntries() { return this.barEntries; }

    public List<Awareness> getAwarenesses(){
        return awarenesses;
    }

    public List<Awareness> getSleepAwarenesses() { return sleepAwarenesses; }

    public List<Sleep> getSleeps(){
        return sleeps;
    }

    public void addSleep(Sleep sleep){
        List<Sleep> listSleep = new ArrayList<>();
        listSleep.add(sleep);
        Log.v("SLEEP DATA ADDED", String.valueOf(sleep.sleepStart));
        sleepDao = db.sleepDao();
        sleepDao.insertAll(listSleep);
        lastDataUpdate = sleep.sleepStart - oneDay;
        editor.putLong("lastDataUpdate", lastDataUpdate);
        editor.apply();
        healthConnectManager.javWriteSleepInput(sleep.sleepStart, sleep.sleepEnd);

    }

    public void editSleep(Sleep prevSleep, Sleep updatedSleep){
        sleepDao = db.sleepDao();

        int count = 0;
        for(Sleep sleep: this.sleeps){
            if(sleep.sleepStart/60000 == prevSleep.sleepStart/60000 && sleep.sleepEnd/60000 == prevSleep.sleepEnd/60000){
                int sleepId = sleep.sleep_id;
                if(!isOverlap(this.sleeps, updatedSleep, sleepId)) {
                    lastDataUpdate = updatedSleep.sleepStart - (1000*60*60*24);
                    editor.putLong("lastDataUpdate", lastDataUpdate);
                    editor.apply();

                    Log.v("UPDATED", "PREVIOUS SLEEP IS UPDATED");

                    updatedSleep.sleep_id = sleepId;
                    sleepDao.updateSleep(sleepId, updatedSleep.sleepStart, updatedSleep.sleepEnd);
                    this.sleeps.set(count, updatedSleep);
                    return;
                }else{
                    Log.v("UPDATED", "PREVIOUS SLEEP IS NOT UPDATED");
                    return;
                }
            }
            count += 1;
        }
    }

    public boolean deleteSleep(Sleep sleepDel){
        sleepDao = db.sleepDao();

        now = System.currentTimeMillis();
        long sleepDelStart = sleepDel.sleepStart/60000;
        long sleepDelEnd = sleepDel.sleepEnd/60000;
        Log.v("SLEEP DELETE START", String.valueOf(sleepDel.sleepStart));
        Log.v("SLEEP DELETE END", String.valueOf(sleepDel.sleepEnd));

        if (sleepDelStart > sleepDelEnd) {
            sleepDelEnd += oneDay/60000;
        }

        for(Sleep sleep: this.sleeps){
            long sSleepStart = sleep.sleepStart/60000;
            long sSleepEnd = sleep.sleepEnd/60000;
            Log.v("SLEEP DELETE START", String.valueOf(sleep.sleepStart));
            Log.v("SLEEP DELETE END", String.valueOf(sleep.sleepEnd));
            if(sSleepStart == sleepDelStart && sSleepEnd == sleepDelEnd){
                Log.v("deleted broooo", "broooo");
                lastDataUpdate = sleep.sleepStart - (1000*60*60*24);
                if (lastDataUpdate < now - twoWeeks) {
                    editor.putBoolean("deleteException", true).apply();
                }
                editor.putLong("lastDataUpdate", lastDataUpdate);
                editor.apply();
                sleepDao.delete(sleep);
                this.sleeps.remove(sleep);
                return true;
            }
        }
        return false;
    }

    public long getMainSleepStart() {
        return mainSleepStart;
    }

    public void setMainSleepStart(long mainSleepStart) {
        this.mainSleepStart = mainSleepStart;
        editor.putLong("mainSleepStart", mainSleepStart);
        editor.apply();
    }

    public long getMainSleepEnd() {
        return mainSleepEnd;
    }

    public void setMainSleepEnd(long mainSleepEnd) {
        this.mainSleepEnd = mainSleepEnd;
        editor.putLong("mainSleepEnd", mainSleepEnd);
        editor.apply();
    }

    public long getNapSleepStart() {
        Log.v("function nap sleep start", String.valueOf(napSleepStart));
        return napSleepStart;
    }

    public void setNapSleepStart(long napSleepStart) {
        this.napSleepStart = napSleepStart;
        editor.putLong("napSleepStart", napSleepStart);
        editor.apply();
    }

    public long getNapSleepEnd() {
        return napSleepEnd;
    }

    public void setNapSleepEnd(long napSleepEnd) {
        this.napSleepEnd = napSleepEnd;
        editor.putLong("napSleepEnd", napSleepEnd);
        editor.apply();
    }

    public long getSleepOnset() {
        return sleepOnset;
    }

    public void setSleepOnset(long sleepOnset) {
        this.sleepOnset = sleepOnset;
        sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putLong("sleepOnset", sleepOnset);
        editor.apply();
        Log.v("sleep onset", sdfDateTime.format(new Date(sharedPref.getLong("sleepOnset", now))));
    }

    public long getWorkOnset(){
        return workOnset;
    }

    public void setWorkOnset(long workOnset){
        this.workOnset = workOnset;
        sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putLong("workOnset", workOnset);
        editor.apply();
        Log.v("work onset", sdfDateTime.format(new Date(sharedPref.getLong("workOnset", now))));
    }

    public long getWorkOffset() {
        return workOffset;
    }

    public void setWorkOffset(long workOffset) {
        this.workOffset = workOffset;
        sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putLong("workOffset", workOffset);
        editor.apply();
        Log.v("work offset", sdfDateTime.format(new Date(sharedPref.getLong("workOffset", now))));
    }

    public boolean getIsEarlySleep() {
        Log.v("MainActivity", "IsEarly: " + this.isearlysleep);
        return this.isearlysleep;
    }

    public boolean getIsEnoughSleep() {
        Log.v("MainActivity", "IsEnough: " + this.isenoughsleep);
        return this.isenoughsleep;
    }

    public long getLastSleepUpdate() {
        return lastSleepUpdate;
    }

    public void setLastSleepUpdate(long lastSleepUpdate) {
        this.lastSleepUpdate = lastSleepUpdate;
        editor.putLong("lastSleepUpdate", lastSleepUpdate);
        editor.apply();
    }

    public void setGoneBottomNavi() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }

    public void setVisibleBottomNavi() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    public void setBottomNaviItem(int selected_id) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(selected_id);
        }
    }

    // CustomAlertDialog
    private void showAlertDialog(View dimBackground, ActionBar actionBar, BottomNavigationView bottomNavigationView) {
        // Save the original color
        // Change this part if someone tries to change the primary color
        int originalActionBarColor = getResources().getColor(R.color.white, null);
        int originalNavigationBarColor = getResources().getColor(R.color.white, null);
        Window window = getWindow();

        // Dim effect
        dimBackground.setVisibility(View.VISIBLE);
        if (bottomNavigationView != null) {
            bottomNavigationView.setItemBackground(new ColorDrawable(getResources().getColor(R.color.dim, null)));
        }
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dim, null)));
        }
        window.setStatusBarColor(getResources().getColor(R.color.dim, null));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(getResources().getColor(R.color.dim, null));
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View viewDialog = inflater.inflate(R.layout.layout_custom_dialog, null);

        TextView dialogTitle = viewDialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = viewDialog.findViewById(R.id.dialogMessage);
        Button dialogButton = viewDialog.findViewById(R.id.dialogButton);
        dialogTitle.setText("알림");
        dialogMessage.setText("내일 근무 형태나 활동 시간이 변경되었나요?\n그렇다면 일정 변경 탭에서 수정해주세요");
        dialogButton.setText("확인");

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(viewDialog)
                .create();

        dialogButton.setOnClickListener(dialogV -> {
            dialog.dismiss();

            // 설문으로 이동
            Bundle temp = new Bundle();
            Intent surveyIntent = new Intent(this, SQMoodSendingActivity.class);
            surveyIntent.putExtra(survey_name, 0);
            surveyIntent.putExtra("moodData", temp);
            startActivity(surveyIntent);

            dimBackground.setVisibility(View.GONE);
            if (bottomNavigationView != null) {
                bottomNavigationView.setItemBackground(new ColorDrawable(getResources().getColor(R.color.white, null)));
            }
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(originalActionBarColor));
            }
            window.setStatusBarColor(getResources().getColor(R.color.white, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setNavigationBarColor(originalNavigationBarColor);
            }
        });
        dialog.setCancelable(true);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }
}