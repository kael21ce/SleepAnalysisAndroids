package com.kael21ce.sleepanalysisandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SettingFragment extends Fragment {

    Boolean isFolded = true;
    private static final String NotifyKey = "Notify_At";
    Button notifyButton;
    TextView notifyDescription;
    TextView noNotifyDescription;
    LinearLayout notifyView;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    OneTimeWorkRequest requested;
    Context context;
    long oneDay = 1000*60*60*24;

    // 알림 토글을 눌러도 반응이 없어 보이던 문제 수정: 권한이 없으면 여기서 실제로
    // 시스템 권한 요청을 띄우고, 거부되면 별도 안내를 보여준다.
    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    editor.putBoolean("isNotifyOn", true).apply();
                    refreshNotifyUi();
                } else {
                    showNotificationPermissionDeniedDialog();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();
        SettingFragment settingFragment = this;
        this.context = v.getContext();

        sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        //Setting the time of the periodic notification
        if (!sharedPref.contains(NotifyKey)) {
            editor.putString(NotifyKey, "21:00").apply();
        }


        notifyButton = v.findViewById(R.id.notifyButton);

        //initial setting of notifySetting
        if (!sharedPref.contains("isNotifyOn")) {
            editor.putBoolean("isNotifyOn", true).apply();
        }

        notifyButton.setText("설정");
        notifyDescription = v.findViewById(R.id.NotifyDescription);
        noNotifyDescription = v.findViewById(R.id.NoNotifyDescription);
        notifyView = v.findViewById(R.id.NotifyView);

        Log.v("SettingFragment", String.valueOf(sharedPref.getBoolean("isNotifyOn", true)));
        refreshNotifyUi();

        //On/Off the notification
        notifyView.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                boolean turnOn = !sharedPref.getBoolean("isNotifyOn", true);
                editor.putBoolean("isNotifyOn", turnOn).apply();
                refreshNotifyUi();
            } else {
                // 이전엔 권한이 없으면 그냥 아무 일도 안 일어났음(탭해도 반응 없음)
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        });


        notifyButton.setOnClickListener(view -> {
//            TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), settingFragment);
//            timePickerDialog.setData(1);
//            timePickerDialog.setTimePicker(notifyAt_complex);
//            timePickerDialog.show();
            Intent notifyIntent = new Intent(v.getContext(), NotifyActivity.class);
            startActivity(notifyIntent);
        });

        // Log out
        LinearLayout logOutView = v.findViewById(R.id.LogOutView);
        logOutView.setOnClickListener(v1 -> {
            //Move to BeginRegisterActivity
            Intent logOutIntent = new Intent(v.getContext(), BeginRegisterActivity.class);
            logOutIntent.putExtra("LogOut", true);
            startActivity(logOutIntent);
            getActivity().finish();
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 시스템 설정 앱에서 알림 권한을 바꾸고 돌아온 경우도 반영
        if (context != null && notifyView != null) {
            refreshNotifyUi();
        }
    }

    private void refreshNotifyUi() {
        boolean granted = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        if (!granted) {
            notifyDescription.setText("권한 없음");
            notifyButton.setVisibility(View.INVISIBLE);
            noNotifyDescription.setVisibility(View.VISIBLE);
            return;
        }
        noNotifyDescription.setVisibility(View.GONE);
        if (sharedPref.getBoolean("isNotifyOn", true)) {
            notifyDescription.setText("알림 켜짐");
            notifyButton.setVisibility(View.VISIBLE);
        } else {
            notifyDescription.setText("알림 꺼짐");
            notifyButton.setVisibility(View.INVISIBLE);
        }
    }

    private void showNotificationPermissionDeniedDialog() {
        new AlertDialog.Builder(context)
                .setTitle("알림 권한이 꺼져 있습니다")
                .setMessage("기기 설정 > 알림에서 SleepWake 알림을 허용해주세요.")
                .setPositiveButton("설정 열기", (dialog, which) -> {
                    Intent intent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    } else {
                        intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.fromParts("package", context.getPackageName(), null));
                    }
                    startActivity(intent);
                })
                .setNegativeButton("취소", null)
                .show();
    }
}