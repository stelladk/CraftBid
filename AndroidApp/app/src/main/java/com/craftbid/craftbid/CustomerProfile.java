package com.craftbid.craftbid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.craftbid.craftbid.adapters.EvaluationsRecyclerAdapter;
import com.craftbid.craftbid.model.Evaluation;

import java.util.ArrayList;
import java.util.List;

public class CustomerProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Profile");

        //Set Back Arrow
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        List<Evaluation> evaluations = new ArrayList<>();
        evaluations.add(new Evaluation(0, "", 4, "maria_karen", "takis_32", getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(1, "", 3, "maria_karen", "mitsos_creations", getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(2, "", 1, "maria_karen", "kaitoula_32", getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(3, "", 5, "maria_karen", "someone", getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(4, "", 2, "maria_karen", "someone2", getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(5, "", 3, "maria_karen", "someone3", getResources().getString(R.string.lorem_ipsum)));

        RecyclerView recycler = findViewById(R.id.reviews_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);
        EvaluationsRecyclerAdapter adapter = new EvaluationsRecyclerAdapter(evaluations);
        recycler.setAdapter(adapter);
    }
}