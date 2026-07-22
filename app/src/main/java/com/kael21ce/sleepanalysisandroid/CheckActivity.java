package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.kael21ce.sleepanalysisandroid.data.ApiClient;
import com.kael21ce.sleepanalysisandroid.data.DataUser;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;
import com.kael21ce.sleepanalysisandroid.data.TokenPair;
import com.kael21ce.sleepanalysisandroid.data.TokenStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckActivity extends AppCompatActivity {

    String user_email;
    String user_password;
    String user_name;
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
            sendUser();
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

    private void sendUser(){
        // "user/"는 더 이상 존재하지 않는 라우트라서 로그인이 계속 실패했음.
        // 실제 로그인은 JWT를 발급하는 token/ 이다 (iOS AuthAPI.login과 동일).
        RetrofitAPI retrofitAPI = ApiClient.api(this);

        DataUser dataUser = new DataUser(user_email, user_password);
        Call<TokenPair> call = retrofitAPI.login(dataUser);
        call.enqueue(new Callback<TokenPair>() {
            @Override
            public void onResponse(Call<TokenPair> call, Response<TokenPair> response) {
                Log.v("RESPONSE", "Response Code : " + response.code());

                TokenPair pair = response.body();
                if (response.code() <= 300 && pair != null && pair.getAccess() != null) {
                    TokenStorage.save(CheckActivity.this, pair.getAccess(), pair.getRefresh());
                    Intent startIntent = new Intent(CheckActivity.this, StartActivity.class);
                    startIntent.putExtra("User_Email", user_email);
                    startActivity(startIntent);
                    finish();
                }else{
                    Toast.makeText(CheckActivity.this, "이메일 또는 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenPair> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
                Log.v("ERROR", "Error found is : " + t.getMessage());
            }
        });
    }
}