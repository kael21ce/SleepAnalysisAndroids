package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
        Button checkButton = findViewById(R.id.checkButton);
        checkButton.setEnabled(false);
        checkButton.setBackgroundColor(getResources().getColor(R.color.blue_2, null));
        EditText emailText = findViewById(R.id.emailText);
        emailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //If text is put, change the color of text
                if (!emailText.getText().toString().isEmpty()) {
                    emailText.setTextColor(getResources().getColor(R.color.black, null));
                } else {
                    emailText.setTextColor(getResources().getColor(R.color.gray_4, null));
                }
                //Check validity of text
                if (isValidEmail(emailText)) {
                    checkButton.setEnabled(true);
                    checkButton.setBackgroundColor(getResources().getColor(R.color.blue_1, null));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //Move to StartActivity if checkButton is clicked
        checkButton.setOnClickListener(view -> {
            user_email = emailText.getText().toString();
            user_name = user_email.substring(0, user_email.indexOf("@"));
            sendUser();
        });

        //Move back to BeginRegisterActivity
        ImageButton checkBackButton = findViewById(R.id.checkBackButton);
        checkBackButton.setOnClickListener(view -> {
            Intent backIntent = new Intent(CheckActivity.this, BeginRegisterActivity.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(backIntent);
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
                .baseUrl("https://sleep-math.com/sleepapp/")
                // as we are sending data in json format so
                // we have to add Gson converter factory
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                // at last we are building our retrofit builder.
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        DataUser dataUser = new DataUser(user_name);
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
                }else{
                    Toast.makeText(CheckActivity.this, "Username is already taken", Toast.LENGTH_SHORT).show();
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