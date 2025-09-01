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

import com.kael21ce.sleepanalysisandroid.data.DataUser;
import com.kael21ce.sleepanalysisandroid.data.RetrofitAPI;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.sleep-math.com/sleepapp/")
                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                // at last we are building our retrofit builder.
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        DataUser dataUser = new DataUser(user_email, user_password);
        Call<DataUser> call = retrofitAPI.createUser(dataUser);
        call.enqueue(new Callback<DataUser>() {
            @Override
            public void onResponse(Call<DataUser> call, Response<DataUser> response) {
                // this method is called when we get response from our api.
//                Toast.makeText(StartActivity.this, "Data added to API", Toast.LENGTH_SHORT).show();

                // we are getting response from our body
                // and passing it to our modal class.
                DataUser responseFromAPI = response.body();

                // on below line we are getting our data from modal class and adding it to our string.
                String responseString = "Response Code : " + response.code() + "\nName : "  + "\n" ;
                Log.v("RESPONSE", responseString);

                if(response.code() <= 300) {
                    Intent startIntent = new Intent(CheckActivity.this, StartActivity.class);
                    startIntent.putExtra("User_Email", user_email);
                    startActivity(startIntent);
                    finish();
                }else{
                    Toast.makeText(CheckActivity.this, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DataUser> call, Throwable t) {
                // setting text to our text view when
                // we get error response from API.
                Log.v("ERROR", "Error found is : " + t.getMessage());
            }
        });
    }
}