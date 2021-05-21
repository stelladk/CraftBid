package com.craftbid.craftbid;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.craftbid.craftbid.model.Evaluation;

public class EvaluationActivity extends AppCompatActivity {
    private String username;
    private String creatorUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

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
        Intent profile = new Intent(EvaluationActivity.this, CreatorProfile.class);
        profile.putExtra("username", creatorUsername);
        startActivity(profile);
    }

    public void submitEvaluation(View view) {
        // TODO create Evaluation object and send it
        goBack();
    }
}
