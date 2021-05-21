package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.craftbid.craftbid.adapters.EvaluationsRecyclerAdapter;
import com.craftbid.craftbid.model.Evaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerProfile extends AppCompatActivity {
    private String username;
    private static String previous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        username = MainActivity.username;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(username);

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Date now = new Date();
        List<Evaluation> evaluations = new ArrayList<>();
        evaluations.add(new Evaluation(0, "maria_karen","takis_32", 4, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(1, "maria_karen","mitsos_creations",  3, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(2, "maria_karen","kaitoula_32", 1, now,  getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(3, "maria_karen","someone", 5, now,  getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(4, "maria_karen","someone2",  2, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(5, "maria_karen","someone3", 3, now,  getResources().getString(R.string.lorem_ipsum)));

        RecyclerView recycler = findViewById(R.id.reviews_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);
        EvaluationsRecyclerAdapter adapter = new EvaluationsRecyclerAdapter(evaluations);
        recycler.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                goBack();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goBack() {
        Intent main = new Intent(CustomerProfile.this, MainActivity.class);
        startActivity(main);
    }

    public void toggleEditCustomer(View view){

    }
}