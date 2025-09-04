package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.annotations.SerializedName;
import com.kael21ce.sleepanalysisandroid.data.ResetPasswordPayload;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;
import com.kael21ce.sleepanalysisandroid.data.RetrofitClient;

import java.io.IOException;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPwdActivity extends AppCompatActivity {

    String email, current_password, new_password, new_password_check;
    private RetrofitAPI apiService;
    private static final String TAG = "AuthAPI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_pwd);

        // Action bar 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Status bar, navigation bar 색상 변경
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.gray_1, null));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(getResources().getColor(R.color.gray_1, null));
        }

        // 뒤로가기 버튼
        ImageButton backButton = findViewById(R.id.ResetPwdBackButton);
        backButton.setOnClickListener(view -> finish());

        // 이메일 설정
        SharedPreferences sharedPref = getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        TextView emailText = findViewById(R.id.currentAccountTextView);
        email = sharedPref.getString("User_Email", "로드 오류");
        emailText.setText(email);

        // 변경 버튼 기본 설정
        AppCompatButton resetButton = findViewById(R.id.resetButton);
        resetButton.setEnabled(false);
        resetButton.setBackgroundResource(R.drawable.corner_16_black_alpha);

        // 입력된 비밀번호 받아오기
        EditText currentPwdText = findViewById(R.id.currentPasswordText);
        EditText newPwdText = findViewById(R.id.newPasswordText);
        EditText newPwdCheckText = findViewById(R.id.newPasswordCheckText);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentPwd = currentPwdText.getText().toString().trim();
                String newPwd = newPwdText.getText().toString().trim();
                String newPwdCheck = newPwdCheckText.getText().toString().trim();

                // 글자 입력 시 글자 색을 변경
                if (!currentPwdText.getText().toString().isEmpty()) {
                    currentPwdText.setTextColor(getResources().getColor(R.color.black, null));
                } else {
                    currentPwdText.setTextColor(getResources().getColor(R.color.gray_4, null));
                }
                if (!newPwdText.getText().toString().isEmpty()) {
                    newPwdText.setTextColor(getResources().getColor(R.color.black, null));
                } else {
                    newPwdText.setTextColor(getResources().getColor(R.color.gray_4, null));
                }
                if (!newPwdCheckText.getText().toString().isEmpty()) {
                    newPwdCheckText.setTextColor(getResources().getColor(R.color.black, null));
                } else {
                    newPwdCheckText.setTextColor(getResources().getColor(R.color.gray_4, null));
                }

                // 비밀번호 변경 버튼 활성화
                if (!currentPwd.isEmpty() && !newPwd.isEmpty() && !newPwdCheck.isEmpty()) {
                    resetButton.setEnabled(true);
                    resetButton.setBackgroundResource(R.drawable.corner_16_dim);
                } else {
                    resetButton.setEnabled(false);
                    resetButton.setBackgroundResource(R.drawable.corner_16_black_alpha);
                }
            }
        };
        currentPwdText.addTextChangedListener(textWatcher);
        newPwdText.addTextChangedListener(textWatcher);
        newPwdCheckText.addTextChangedListener(textWatcher);

        // 비밀번호 변경 버튼
        resetButton.setOnClickListener(view -> {
            current_password = currentPwdText.getText().toString();
            new_password = newPwdText.getText().toString();
            new_password_check = newPwdCheckText.getText().toString();
            View dimBackground = findViewById(R.id.dimBackgroundReset);
            if (new_password.length() < 8) {
                showAlertDialog(dimBackground,
                        "비밀번호는 8자 이상이어야 합니다.", false);
            } else if (new_password.equals(current_password)) {
                showAlertDialog(dimBackground,
                        "새 비밀번호는 현재 비밀번호와 달라야 합니다.", false);
            } else if (!new_password_check.equals(new_password)) {
                showAlertDialog(dimBackground,
                        "새 비밀번호가 일치하지 않습니다.", false);
            } else {
                apiService = RetrofitClient.getClient(this).create(RetrofitAPI.class);
                resetPassword(current_password, new_password, email, dimBackground);
            }
        });
    }

    // Custom alert
    private void showAlertDialog(View dimBackground, String message, boolean isSuccess) {
        // Save the original color
        // Change this part if someone tries to change the primary color
        int originalNavigationBarColor = getResources().getColor(R.color.white, null);
        Window window = getWindow();

        // Dim effect
        dimBackground.setVisibility(View.VISIBLE);
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
        dialogMessage.setText(message);
        dialogButton.setText("확인");

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(viewDialog)
                .create();

        dialogButton.setOnClickListener(dialogV -> {
            dialog.dismiss();
            dimBackground.setVisibility(View.GONE);
            window.setStatusBarColor(getResources().getColor(R.color.white, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setNavigationBarColor(originalNavigationBarColor);
            }
            if (isSuccess) {
                finish(); // 비밀번호 변경 성공 시 MainActivity로 이동
            }
        });
        dialog.setCancelable(true);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }

    // 서버와 통신해 비밀번호 변경
    private void resetPassword(@Nullable String current_password, @NonNull String new_password, @Nullable String email,
                               @NonNull View dimBackground) {
        ResetPasswordPayload payload = new ResetPasswordPayload(new_password)
                .setCurrentPassword(current_password)
                .setEmail(email);
        apiService.resetPassword(payload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Success to reset password");
                    // 비밀번호 변경 성공시 alert
                    showAlertDialog(dimBackground, "비밀번호가 변경되었습니다.", true);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "알 수 없는 에러";
                        Log.e(TAG, "Failed to reset password: " + response.code() + " - " + errorBody);
                        // 비밀번호 실패 시 alert
                        showAlertDialog(dimBackground, "비밀번호 변경 실패.", false);
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing is failed", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }
}