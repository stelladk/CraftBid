package com.craftbid.craftbid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class SignupCustomerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_customer);

        TextView password = findViewById(R.id.password_label);
        String star = getColoredSpanned("*", String.valueOf(getResources().getColor(R.color.error)));
        String text = getColoredSpanned(getResources().getString(R.string.password_en), "fff");
        password.setText(Html.fromHtml(star+text));
    }
    private String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }
}