package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.craftbid.craftbid.adapters.EvaluationsRecyclerAdapter;
import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.model.Evaluation;
import com.craftbid.craftbid.model.Thumbnail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreatorProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Profile");

        //Set Back Arrow
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Thumbnails RecyclerView
        List<Thumbnail> thumbnails = new ArrayList<>();
        thumbnails.add(new Thumbnail(0, "Ξύλινη Καρέκλα", "Ωραιότατη Ξύλινη Καρέκλα","Καρέκλες", "chair1", 15));
        thumbnails.add(new Thumbnail(1, "Ξύλινη Καρέκλα", "Ξύλινη Καρέκλα Κήπου", "Καρέκλες",  "chair3", 20));
        thumbnails.add(new Thumbnail(2, "Ξύλινη Καρέκλα", "Απλή Ξύλινη Καρέκλα", "Καρέκλες",  "chair2", 15));
        thumbnails.add(new Thumbnail(3, "Ξύλινη Καρέκλα", "Χειροποίητη Ξύλινη Καρέκλα", "Καρέκλες", "chair4",  15));

        RecyclerView thumbnails_recycler = findViewById(R.id.thumbnails_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        thumbnails_recycler.setLayoutManager(manager);
        FeedRecyclerAdapter adapter = new FeedRecyclerAdapter(thumbnails, this);
        thumbnails_recycler.setAdapter(adapter);

        //Reviews RecyclerView
        Date now = new Date();
        List<Evaluation> evaluations = new ArrayList<>();
        evaluations.add(new Evaluation(0, "aekara_21","mitsos_creations", 4, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(1, "maria_karen","mitsos_creations",  3, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(2, "takis_32","mitsos_creations", 4, now,  getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(3, "someone","mitsos_creations", 5, now,  getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(4, "someone2","mitsos_creations",  4, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(5, "someone3","mitsos_creations", 5, now,  getResources().getString(R.string.lorem_ipsum)));

        RecyclerView reviews_recycler = findViewById(R.id.reviews_recyclerview);
        RecyclerView.LayoutManager manager_r = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        reviews_recycler.setLayoutManager(manager_r);
        EvaluationsRecyclerAdapter adapter_r = new EvaluationsRecyclerAdapter(evaluations);
        reviews_recycler.setAdapter(adapter_r);
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
        Intent main = new Intent(CreatorProfile.this, MainActivity.class);
        startActivity(main);
    }

    //Temporary
    public int getDrawable(String name) {
        return this.getResources().getIdentifier(name, "drawable", this.getPackageName());
    }

    public void openRewardsCustomer(View view) {
        Intent rewardsCustomer = new Intent(CreatorProfile.this, RewardsCustomerActivity.class);
        startActivity(rewardsCustomer);
    }
}