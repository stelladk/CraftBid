package com.craftbid.craftbid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class SignupCreatorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String expertise_selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_creator);

        //TODO get expertise choices from database
        Spinner expertise = findViewById(R.id.expertise_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.expertise, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expertise.setAdapter(adapter);
        expertise.setOnItemSelectedListener(this);

        TextView password = findViewById(R.id.password_label);
        String star = getColoredSpanned("*", String.valueOf(getResources().getColor(R.color.error)));
        String text = getColoredSpanned(getResources().getString(R.string.password_en), "fff");
        password.setText(Html.fromHtml(star+text));
    }

    private String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }

    public void goBack(View view) {
        Intent login = new Intent(SignupCreatorActivity.this, LoginActivity.class);
        startActivity(login);
    }

    public void attemptSighup(View view) {
        //TODO
        //Do actions to confirm sign up
        Intent main = new Intent(SignupCreatorActivity.this, MainActivity.class);
        startActivity(main);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        expertise_selected = adapterView.getItemAtPosition(i).toString();
        Snackbar.make(view, "Selected Expertise " + expertise_selected, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}