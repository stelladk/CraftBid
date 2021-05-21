package com.craftbid.craftbid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    private EditText username_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username_edit = findViewById(R.id.username);
    }

    public void openSignupCustomer(View view) {
        Intent signupCustomer = new Intent(LoginActivity.this, SignupCustomerActivity.class);
        startActivity(signupCustomer);
    }

    public void openSignupCreator(View view) {
        Intent signupCreator = new Intent(LoginActivity.this, SignupCreatorActivity.class);
        startActivity(signupCreator);
    }

    //Open Main As Guest
    public void openMain(View view){
        Intent main = new Intent(LoginActivity.this, MainActivity.class);
        main.putExtra("username", MainActivity.GUEST);
        main.putExtra("creator", false);
        startActivity(main);
    }

    public void attemptLogin(View view) {
        //TODO
        String username = username_edit.getText().toString();
        Log.d("LOGIN", "onCreate: username("+username+")");
        //Do actions to confirm login
        Intent main = new Intent(LoginActivity.this, MainActivity.class);
        main.putExtra("username", username); //Send his username
        main.putExtra("creator", true); //TODO check if user is creator
        startActivity(main);
    }
}