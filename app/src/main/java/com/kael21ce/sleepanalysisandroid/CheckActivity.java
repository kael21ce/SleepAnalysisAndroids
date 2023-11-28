package com.kael21ce.sleepanalysisandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class CheckActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

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
            String user_email = emailText.getText().toString();
            Intent startIntent = new Intent(CheckActivity.this, StartActivity.class);
            startIntent.putExtra("User_Email", user_email);
            startActivity(startIntent);
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
}