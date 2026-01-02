package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.kael21ce.sleepanalysisandroid.data.AccessOnly;
import com.kael21ce.sleepanalysisandroid.data.DataUser;
import com.kael21ce.sleepanalysisandroid.data.RefreshPayload;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;
import com.kael21ce.sleepanalysisandroid.data.RetrofitClient;
import com.kael21ce.sleepanalysisandroid.data.TokenPair;
import com.kael21ce.sleepanalysisandroid.data.TokenStorage;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckActivity extends AppCompatActivity {

    String user_email, user_password, user_name;
    private static final String TAG = "AuthAPI";
    private RetrofitAPI apiService;
    private TokenStorage tokenStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        //Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Turn on the checkButton whether email is valid or not
        AppCompatButton checkButton = findViewById(R.id.checkButton);
        checkButton.setEnabled(false);
        checkButton.setBackgroundResource(R.drawable.corner_16_black_alpha);
        EditText emailText = findViewById(R.id.emailText);
        EditText passwordText = findViewById(R.id.passwordText);

        //Enable checkButton if email and password are valid
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean validEmail = isValidEmail(emailText);
                String email = emailText.getText().toString().trim();

                String password = passwordText.getText().toString().trim();
                //If text is put, change the color of text
                if (!emailText.getText().toString().isEmpty()) {
                    emailText.setTextColor(getResources().getColor(R.color.black, null));
                } else {
                    emailText.setTextColor(getResources().getColor(R.color.gray_4, null));
                }
                //If text is put, change the color of text
                if (!passwordText.getText().toString().isEmpty()) {
                    passwordText.setTextColor(getResources().getColor(R.color.black, null));
                } else {
                    passwordText.setTextColor(getResources().getColor(R.color.gray_4, null));
                }

                if (validEmail && !email.isEmpty() && !password.isEmpty()) {
                    checkButton.setEnabled(true);
                    checkButton.setBackgroundResource(R.drawable.corner_16_dim);
                } else {
                    checkButton.setEnabled(false);
                    checkButton.setBackgroundResource(R.drawable.corner_16_black_alpha);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        emailText.addTextChangedListener(textWatcher);
        passwordText.addTextChangedListener(textWatcher);

        //Move to StartActivity if checkButton is clicked
        checkButton.setOnClickListener(view -> {
            user_email = emailText.getText().toString();
            user_name = user_email.substring(0, user_email.indexOf("@"));
            user_password = passwordText.getText().toString();
            apiService = RetrofitClient.getClient(this).create(RetrofitAPI.class);
            tokenStorage = TokenStorage.getInstance(this);
            performLogin(user_email, user_password);
        });

        //Move back to BeginRegisterActivity
        ImageButton checkBackButton = findViewById(R.id.checkBackButton);
        checkBackButton.setOnClickListener(view -> {
            Intent backIntent = new Intent(CheckActivity.this, BeginRegisterActivity.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(backIntent);
        });

        // 회원가입 페이지로 이동
        TextView go2RegisterText = findViewById(R.id.go2SignupText);
        go2RegisterText.setOnClickListener(view -> {
            Intent registerIntent = new Intent(CheckActivity.this, SignupActivity.class);
            startActivity(registerIntent);
        });
    }

    //Check validity of email-form
    public Boolean isValidEmail(EditText emailText) {
        String testStr = emailText.getText().toString();
        if (!testStr.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(testStr).matches()) {
            return true;
        } else {
            return false;
        }
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

    // 로그인 요청
    private void performLogin(String email, String password) {
        DataUser dataUser = new DataUser(email, password);
        apiService.login(dataUser).enqueue(new Callback<TokenPair>() {
            @Override
            public void onResponse(Call<TokenPair> call, Response<TokenPair> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TokenPair tokenPair = response.body();
                    tokenStorage.saveTokens(tokenPair);
                    Log.d(TAG, "Log in success! Access Token: " + tokenPair.getAccessToken());
                    Intent startIntent = new Intent(CheckActivity.this, StartActivity.class);
                    startIntent.putExtra("User_Email", email);
                    startActivity(startIntent);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Log in fail: " + response.code() + " - " + errorBody);
                        Toast.makeText(getApplicationContext(), "로그인 실패: 이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
//                        View dimBackground = findViewById(R.id.dimBackgroundCheck);
//                        ActionBar actionBar = getSupportActionBar();
//                        String title = "로그인 실패";
//                        String message = "이메일 또는 비밀번호를 확인해주세요.";
//                        String buttonText = "확인";
//                        showAlertDialog(dimBackground, actionBar, title, message, buttonText);
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing is failed", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<TokenPair> call, Throwable t) {
                Log.e(TAG, "Network error " + t.getMessage());
            }
        });
    }

    // 토큰 리프레시 요청
    private void performRefresh() {
        String refreshToken = tokenStorage.getRefreshToken();
        if (refreshToken != null) {
            Log.e(TAG, "No refresh token. Log-in is required");
            return;
        }
        RefreshPayload payload = new RefreshPayload(refreshToken);
        apiService.refresh(payload).enqueue(new Callback<AccessOnly>() {
            @Override
            public void onResponse(Call<AccessOnly> call, Response<AccessOnly> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String newAccessToken = response.body().getAccessToken();
                    tokenStorage.updateAccessToken(newAccessToken);
                    Log.d(TAG, "Successful refresh! New access token: " + newAccessToken);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Refresh fail: " + response.code() + " - " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing is failed", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<AccessOnly> call, Throwable t) {
                Log.e(TAG, "Network error " + t.getMessage());
            }
        });
    }
}