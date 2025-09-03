package com.kael21ce.sleepanalysisandroid;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResetPwdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_pwd);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ResetPwdLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
            Log.d("ResetPwdActivity", "Reset password");
            finish();
        });
    }
}