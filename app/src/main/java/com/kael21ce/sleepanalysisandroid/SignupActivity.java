package com.kael21ce.sleepanalysisandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kael21ce.sleepanalysisandroid.data.ApiClient;
import com.kael21ce.sleepanalysisandroid.data.DataUser;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    String user_email, user_password, user_name, password_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.SignupLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Action bar 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 이메일이 유효하지 않을 때 signupButton 비활성화
        AppCompatButton signupButton = findViewById(R.id.signupButton);
        signupButton.setEnabled(false);
        signupButton.setBackgroundResource(R.drawable.corner_16_black_alpha);
        EditText emailText = findViewById(R.id.signupEmailText);
        EditText passwordText = findViewById(R.id.signupPasswordText);
        EditText passwordCheckText = findViewById(R.id.signupPasswordCheckText);

        // 이메일과 비밀번호가 유효할 때 signupButton 활성화
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();
                String passwordCheck = passwordCheckText.getText().toString();
                boolean validEmail = isValidEmail(emailText);
                boolean emptyEmail = email.isEmpty();
                boolean emptyPassword = password.isEmpty();
                boolean emptyPasswordCheck = passwordCheck.isEmpty();
                boolean passwordLength = password.length() >= 6; // 비밀번호 글자 수 확인

                // Text 입력 시 text 색 변경
                if (!emailText.getText().toString().isEmpty()) {
                    emailText.setTextColor(getResources().getColor(R.color.black, null));
                } else {
                    emailText.setTextColor(getResources().getColor(R.color.gray_4, null));
                }
                if (!passwordText.getText().toString().isEmpty()) {
                    passwordText.setTextColor(getResources().getColor(R.color.black, null));
                } else {
                    passwordText.setTextColor(getResources().getColor(R.color.gray_4, null));
                }
                if (!passwordCheckText.getText().toString().isEmpty()) {
                    passwordCheckText.setTextColor(getResources().getColor(R.color.black, null));
                } else {
                    passwordCheckText.setTextColor(getResources().getColor(R.color.gray_4, null));
                }

                if (validEmail && !emptyEmail && !emptyPassword && !emptyPasswordCheck && passwordLength) {
                    signupButton.setEnabled(true);
                    signupButton.setBackgroundResource(R.drawable.corner_16_dim);
                } else {
                    signupButton.setEnabled(false);
                    signupButton.setBackgroundResource(R.drawable.corner_16_black_alpha);
                }
            }
        };

        emailText.addTextChangedListener(textWatcher);
        passwordText.addTextChangedListener(textWatcher);
        passwordCheckText.addTextChangedListener(textWatcher);

        // signupButton 클릭 시 회원가입 정보 입력
        signupButton.setOnClickListener(v -> {
            user_password = passwordText.getText().toString();
            password_check = passwordCheckText.getText().toString();
            if (user_password.equals(password_check)) {
                user_email = emailText.getText().toString();
                user_name = user_email.substring(0, user_email.indexOf("@"));
                sendUser();
            } else {
                // 경고 띄우기
                View dimBackground = findViewById(R.id.dimBackgroundSignup);
                ActionBar actionBar = getSupportActionBar();
                showAlertDialog(dimBackground, actionBar, "알림", "비밀번호가 일치하지 않습니다.", "확인");
            }
        });


        // 로그인 페이지로 이동
        findViewById(R.id.go2LoginText).setOnClickListener(v -> startActivity(new Intent(SignupActivity.this, CheckActivity.class)));
    }

    // 이메일 형식이 타당한지 확인
    public Boolean isValidEmail(EditText emailText) {
        String testStr = emailText.getText().toString();
        return !testStr.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(testStr).matches();
    }

    // 회원가입 요청 전송. signup/은 토큰을 안 주기 때문에(iOS AuthAPI.signup과 동일한 응답
    // 형태), 성공하면 이어서 로그인까지 호출해 토큰을 받아둔다. 안 그러면 회원가입은 되는데
    // 토큰이 없어서 이후 인증이 필요한 API 호출이 전부 401로 실패하게 된다.
    private void sendUser() {
        RetrofitAPI retrofitAPI = ApiClient.api(this);

        DataUser dataUser = new DataUser(user_email, user_password);
        Call<DataUser> call = retrofitAPI.createSignupUser(dataUser);
        call.enqueue(new Callback<DataUser>() {
            @Override
            public void onResponse(Call<DataUser> call, Response<DataUser> response) {
                Log.v("RESPONSE", "Response Code : " + response.code());

                if (response.code() <= 300) {
                    loginAfterSignup();
                } else {
                    View dimBackground = findViewById(R.id.dimBackgroundSignup);
                    ActionBar actionBar = getSupportActionBar();
                    showAlertDialog(dimBackground, actionBar, "알림", "이미 가입된 이메일이거나 회원가입에 실패했습니다.", "확인");
                }
            }

            @Override
            public void onFailure(Call<DataUser> call, Throwable t) {
                Log.v("ERROR", "Error found is : " + t.getMessage());
            }
        });
    }

    // 가입 직후 자동 로그인해서 토큰을 저장
    private void loginAfterSignup() {
        RetrofitAPI retrofitAPI = ApiClient.api(this);
        DataUser dataUser = new DataUser(user_email, user_password);
        Call<com.kael21ce.sleepanalysisandroid.data.TokenPair> call = retrofitAPI.login(dataUser);
        call.enqueue(new Callback<com.kael21ce.sleepanalysisandroid.data.TokenPair>() {
            @Override
            public void onResponse(Call<com.kael21ce.sleepanalysisandroid.data.TokenPair> call,
                                    Response<com.kael21ce.sleepanalysisandroid.data.TokenPair> response) {
                com.kael21ce.sleepanalysisandroid.data.TokenPair pair = response.body();
                if (pair != null && pair.getAccess() != null) {
                    com.kael21ce.sleepanalysisandroid.data.TokenStorage.save(
                            SignupActivity.this, pair.getAccess(), pair.getRefresh());
                }
                Intent startIntent = new Intent(SignupActivity.this, StartActivity.class);
                startIntent.putExtra("User_Email", user_email);
                startActivity(startIntent);
                finish();
            }

            @Override
            public void onFailure(Call<com.kael21ce.sleepanalysisandroid.data.TokenPair> call, Throwable t) {
                Log.v("ERROR", "Error found is : " + t.getMessage());
                // 로그인 자동화가 실패해도 가입 자체는 됐으니 로그인 화면으로 보낸다
                startActivity(new Intent(SignupActivity.this, CheckActivity.class));
                finish();
            }
        });
    }

    private void showAlertDialog(View dimBackground, ActionBar actionBar,
                                 String title,
                                 String message, String buttonText) {
        // Save the original color
        // Change this part if someone tries to change the primary color
        int originalActionBarColor = getResources().getColor(R.color.white, null);
        int originalNavigationBarColor = getResources().getColor(R.color.white, null);
        Window window = getWindow();


        // Dim effect
        dimBackground.setVisibility(View.VISIBLE);
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
        dialogTitle.setText(title);
        dialogMessage.setText(message);
        dialogButton.setText(buttonText);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(viewDialog)
                .create();

        dialogButton.setOnClickListener(dialogV -> {
            dialog.dismiss();
            dimBackground.setVisibility(View.GONE);
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

    private void showAlertDialogNoDim(String title, String message, String buttonText) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View viewDialog = inflater.inflate(R.layout.layout_custom_dialog, null);
        TextView dialogTitle = viewDialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = viewDialog.findViewById(R.id.dialogMessage);
        Button dialogButton = viewDialog.findViewById(R.id.dialogButton);
        dialogTitle.setText(title);
        dialogMessage.setText(message);
        dialogButton.setText(buttonText);

        android.app.AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(viewDialog)
                .create();

        dialogButton.setOnClickListener(dialogV -> {
            dialog.dismiss();
        });
        dialog.setCancelable(true);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }
}