package com.craftbid.craftbid;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ReportActivity extends AppCompatActivity {
    private String username;
    private String creatorUsername;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        username = MainActivity.username;

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            creatorUsername = bundle.getString("username");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* get parameters (profile photo,username,email,phone)
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String creator = bundle.getString("creator");*/
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void goBack() {
        Intent profile = new Intent(ReportActivity.this, CreatorProfile.class);
        profile.putExtra("username", creatorUsername);
        startActivity(profile);
    }

    public void submitReport(View view){
        //TODO create report object and send it to AppServer
        goBack();
    }
}
