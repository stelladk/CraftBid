package com.craftbid.craftbid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void openSignupCustomer(View view) {
        Intent signupCustomer = new Intent(LoginActivity.this, SignupCustomerActivity.class);
        startActivity(signupCustomer);
    }

    public void openSignupCreator(View view) {
        Intent signupCreator = new Intent(LoginActivity.this, SignupCreatorActivity.class);
        startActivity(signupCreator);
    }

    public void openMain(View view){
        Intent main = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(main);
    }

    public void attemptLogin(View view) {
        //TODO
        //Do actions to confirm login
        Intent main = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(main);
    }
}