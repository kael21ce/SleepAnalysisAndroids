package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.TokenStorage;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SettingFragment extends Fragment {

    Boolean isFolded = true;
    private static final String NotifyKey = "Notify_At";
    Button notifyButton;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    OneTimeWorkRequest requested;
    Context context;
    long oneDay = 1000*60*60*24;
    private static final String RecommendName = "Recommend";
    private static final String survey_name = "SurveyType";
    private static final String survey_key = "SQMood";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();
        SettingFragment settingFragment = this;
        this.context = v.getContext();

        sharedPref = getActivity().getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        // 로그인 정보
        // 이메일 가져오기
        TextView accountEmailDescription = v.findViewById(R.id.accountEmailDescription);
        String email = sharedPref.getString("User_Email", "로드 오류"); // SharedPreference에 email이 저장되어 있다고 가정.
        accountEmailDescription.setText(email);

        // 토큰 만료 시간 가져오기
        Date accessExp = decodeJWTExp(TokenStorage.getInstance(context).getAccessToken());
        Date refreshExp = decodeJWTExp(TokenStorage.getInstance(context).getRefreshToken());
        TextView accountSessionDescription = v.findViewById(R.id.accountSessionDescription);
        getAuthStatus(accountSessionDescription, accessExp, refreshExp);

        // 새로고침 버튼 클릭 시 계정 정보 업데이트
        ImageButton accountRefreshButton = v.findViewById(R.id.accountRefreshButton);
        accountRefreshButton.setOnClickListener(vRef -> {
            // 회전 애니메이션
            Animation rotateAnimation = AnimationUtils.loadAnimation(v.getContext(), R.anim.rotate_animation);
            accountRefreshButton.startAnimation(rotateAnimation);
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                //refresh the information in SettingFragment
                Date newAccessExp = decodeJWTExp(TokenStorage.getInstance(v.getContext()).getAccessToken());
                Date newRefreshExp = decodeJWTExp(TokenStorage.getInstance(v.getContext()).getRefreshToken());
                getAuthStatus(accountSessionDescription, newAccessExp, newRefreshExp);
            }, 400);
        });

        // 비밀번호 변경
        LinearLayout resetPwdView = v.findViewById(R.id.ResetPwdView);
        resetPwdView.setOnClickListener(vPwd -> {
            Intent resetPwdIntent = new Intent(v.getContext(), ResetPwdActivity.class);
            startActivity(resetPwdIntent);
        });

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
        TextView notifyDescription = v.findViewById(R.id.NotifyDescription);
        TextView noNotifyDescription = v.findViewById(R.id.NoNotifyDescription);
        LinearLayout notifyView = v.findViewById(R.id.NotifyView);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notifyDescription.setText("권한 없음");
            notifyButton.setVisibility(View.INVISIBLE);
            noNotifyDescription.setVisibility(View.VISIBLE);
        } else {
            notifyDescription.setText("알림 켜짐");
            notifyButton.setVisibility(View.VISIBLE);
            noNotifyDescription.setVisibility(View.GONE);
        }

        //Initial setting
        Log.v("SettingFragment", String.valueOf(sharedPref.getBoolean("isNotifyOn", true)));
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            if (sharedPref.getBoolean("isNotifyOn", true)) {
                notifyDescription.setText("알림 켜짐");
                notifyButton.setVisibility(View.VISIBLE);
            } else {
                notifyDescription.setText("알림 꺼짐");
                notifyButton.setVisibility(View.INVISIBLE);
            }
        }

        //On/Off the notification
        notifyView.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                if (sharedPref.getBoolean("isNotifyOn", true)) {
                    editor.putBoolean("isNotifyOn", false).apply();
                    notifyDescription.setText("알림 꺼짐");
                    notifyButton.setVisibility(View.INVISIBLE);
                } else {
                    editor.putBoolean("isNotifyOn", true).apply();
                    notifyDescription.setText("알림 켜짐");
                    notifyButton.setVisibility(View.VISIBLE);
                }
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

        //Move to HideActivity
        TextView hideDescription = v.findViewById(R.id.HideDescription);
        LinearLayout hideView = v.findViewById(R.id.HideView);
        if (!sharedPref.contains("isHidden")) {
            editor.putBoolean("isHidden", true).apply();
        }
        boolean isHidden = sharedPref.getBoolean("isHidden", true);
        if (!isHidden) {
            hideDescription.setText("켜짐");
        } else {
            hideDescription.setText("꺼짐");
        }
        hideView.setOnClickListener(view -> {
            Intent hideIntent = new Intent(v.getContext(), HideActivity.class);
            startActivity(hideIntent);
        });

        LinearLayout onsetView = v.findViewById(R.id.OnsetView);
        onsetView.setOnClickListener(view -> {
            Intent sleepOnsetIntent = new Intent(v.getContext(), SleepOnsetActivity.class);
            startActivity(sleepOnsetIntent);
        });

        //Do SQMood survey again
        if (!sharedPref.contains(survey_key)) {
            editor.putInt(survey_key, 0).apply();
        }
        int surveyDay = sharedPref.getInt(survey_key, 0);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        LinearLayout sqMoodVisitView = v.findViewById(R.id.SQMoodVisitView);
        sqMoodVisitView.setVisibility(View.GONE);
        sqMoodVisitView.setOnClickListener(view -> {
            Bundle temp = new Bundle();
            Intent surveyIntent = new Intent(v.getContext(), SQMoodSendingActivity.class);
            surveyIntent.putExtra(survey_name, 0);
            surveyIntent.putExtra("moodData", temp);
            startActivity(surveyIntent);
        });

        // Log out
        LinearLayout logOutView = v.findViewById(R.id.LogOutView);
        logOutView.setOnClickListener(v1 -> {
            View dimBackground = v.findViewById(R.id.dimBackgroundSetting);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
            showAlertDialog(dimBackground, actionBar, bottomNavigationView, "알림", "정말 로그아웃 하시겠습니까?", "확인", "취소");

        });

        return v;
    }

    private void showAlertDialog(View dimBackground, ActionBar actionBar,
                                 BottomNavigationView bottomNavigationView, String title,
                                 String message, String yesButtonText, String noButtonText) {
        // Save the original color
        // Change this part if someone tries to change the primary color
        int originalActionBarColor = getResources().getColor(R.color.white, null);
        Window window = getActivity().getWindow();

        // Dim effect
        dimBackground.setVisibility(View.VISIBLE);
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dim, null)));
        }
        if (bottomNavigationView != null) {
            bottomNavigationView.setItemBackground(new ColorDrawable(Color.parseColor("#80000000")));
        }
        window.setStatusBarColor(getResources().getColor(R.color.dim, null));

        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.layout_custom_binary_dialog,
                getActivity().findViewById(R.id.BinaryDialogLayout));
        TextView dialogTitle = viewDialog.findViewById(R.id.binaryDialogTitle);
        TextView dialogMessage = viewDialog.findViewById(R.id.binaryDialogMessage);
        Button dialogYesButton = viewDialog.findViewById(R.id.dialogYesButton);
        Button dialogNoButton = viewDialog.findViewById(R.id.dialogNoButton);
        dialogTitle.setText(title);
        dialogMessage.setText(message);
        dialogYesButton.setText(yesButtonText);
        dialogNoButton.setText(noButtonText);

        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                .setView(viewDialog)
                .create();

        dialogYesButton.setOnClickListener(dialogV -> {
            // logout 진행
            //Move to BeginRegisterActivity
            Intent logOutIntent = new Intent(viewDialog.getContext(), BeginRegisterActivity.class);
            logOutIntent.putExtra("LogOut", true);
            startActivity(logOutIntent);
            getActivity().finish();

            // Dialog 종료
            dialog.dismiss();
            dimBackground.setVisibility(View.GONE);
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(originalActionBarColor));
            }
            if (bottomNavigationView != null) {
                bottomNavigationView.setItemBackground(new ColorDrawable(Color.parseColor("#FFFFFF")));
            }
            window.setStatusBarColor(getResources().getColor(R.color.white, null));
        });

        dialogNoButton.setOnClickListener(dialogV -> {
            // Dialog 종료
            dialog.dismiss();
            dimBackground.setVisibility(View.GONE);
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(originalActionBarColor));
            }
            if (bottomNavigationView != null) {
                bottomNavigationView.setItemBackground(new ColorDrawable(Color.parseColor("#FFFFFF")));
            }
            window.setStatusBarColor(getResources().getColor(R.color.white, null));
        });
        dialog.setCancelable(true);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }

    public static Date decodeJWTExp(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            // 1. JWT를 "." 기준으로 분리
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null; // Payload 부분이 존재하지 않음
            }

            // 2. Payload 부분을 Base64URL 디코딩
            String payloadJson = decodeBase64Url(parts[1]);
            if (payloadJson == null) {
                return null;
            }

            // 3. 디코딩된 문자열을 JSON 객체로 파싱
            JSONObject jsonObject = new JSONObject(payloadJson);

            // 4. exp 클레임 값을 가져오기 (Unix timestamp)
            double exp = jsonObject.optDouble("exp");
            if (Double.isNaN(exp)) {
                return null;
            }

            // 5. 초를 밀리초로 변환하여 Date 객체 생성
            long expMillis = (long) (exp * 1000);
            return new Date(expMillis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String decodeBase64Url(String input) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(input);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } else {
            byte[] decodedBytes = android.util.Base64.decode(input, android.util.Base64.URL_SAFE);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        }

    }

    // Date 객체를 "yyyy-MM-dd HH:mm" 형식의 문자열로 변환
    private String ymdhm(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    // Date 객체까지 남은 시간을 상대적인 문자열로 변환
    private String relativeTimeString(Date date) {
        if (date == null) {
            return "";
        }

        long nowMillis = System.currentTimeMillis();
        long dateMillis = date.getTime();

        // 시간 차이를 밀리초(ms) 단위로 계산
        long diff = Math.abs(nowMillis - dateMillis);

        // 밀리초를 일, 시간, 분으로 변환
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        diff -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        diff -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

        // 문자열 조합
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || (days == 0 && hours == 0)) {
            // 차이가 1분 미만이라도 "0m"을 표시하기 위한 조건
            sb.append(minutes).append("m");
        }

        String timeString = sb.toString().trim();

        // 미래 시점인지 과거 시점인지에 따라 접두사/접미사 추가
        if (dateMillis >= nowMillis) {
            return "in " + timeString;
        } else {
            return timeString + " ago";
        }
    }

    // 토큰 기한에 따른 텍스트 변화
    public void getAuthStatus(TextView textView, Date accessExp, Date refreshExp) {
        String sessionText;
        int sessionColor;

        // 세션 상태 결정
        if (refreshExp != null) {
            if (refreshExp.after(new Date())) {
                sessionText = "유효 — " + ymdhm(refreshExp);
                sessionColor = R.color.green_1;
            } else {
                sessionText = "만료 — " + ymdhm(refreshExp);
                sessionColor = R.color.red_1;
            }
        } else {
            sessionText = "미로그인";
            sessionColor = R.color.red_1;
        }

        // Access Token 상태 결정
        String accessText;
        if (accessExp == null) {
            accessText = "없음";
        } else {
            accessText = accessExp.after(new Date()) ?
                    "만료 " + relativeTimeString(accessExp) :
                    "만료됨";
        }

        // TextView 속성 바꾸기
        if (textView != null) {
            textView.setText(sessionText);
            textView.setTextColor(getResources().getColor(sessionColor, null));
        }
    }
}