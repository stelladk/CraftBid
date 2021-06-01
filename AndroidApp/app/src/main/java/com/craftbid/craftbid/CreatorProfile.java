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
import android.widget.Button;

import com.craftbid.craftbid.adapters.EvaluationsRecyclerAdapter;
import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.model.Evaluation;
import com.craftbid.craftbid.model.Listing;
import com.craftbid.craftbid.model.Thumbnail;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class CreatorProfile extends AppCompatActivity {
    private static final int SHOWN_ITEMS = 2; //TODO change regarding how many ites we want to show each time seeMore is clicked
    private String username;
    private static String previous;
    protected ArrayList<Thumbnail> thumbnails;
    protected ArrayList<Evaluation> evaluations;
    protected RecyclerView thumbnails_recycler;
    protected RecyclerView evaluations_recycler;
    protected FeedRecyclerAdapter adapter;
    protected EvaluationsRecyclerAdapter adapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_profile);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            username = bundle.getString("username");
            previous = bundle.getString("previous", previous);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(username);

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /*
        //Thumbnails RecyclerView
        byte[] test = new byte[2];
        thumbnails = new ArrayList<>();
        thumbnails.add(new Thumbnail(0, "Ξύλινη Καρέκλα", "Ωραιότατη Ξύλινη Καρέκλα", "Καρέκλες", 15, test));
        thumbnails.add(new Thumbnail(1, "Ξύλινη Καρέκλα", "Ξύλινη Καρέκλα Κήπου", "Καρέκλες", 20, test));
        thumbnails.add(new Thumbnail(2, "Ξύλινη Καρέκλα", "Απλή Ξύλινη Καρέκλα", "Καρέκλες", 15, test));
        thumbnails.add(new Thumbnail(3, "Ξύλινη Καρέκλα", "Χειροποίητη Ξύλινη Καρέκλα", "Καρέκλες", 15, test));
        */

        thumbnails_recycler = findViewById(R.id.thumbnails_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        thumbnails_recycler.setLayoutManager(manager);

        //adapter = new FeedRecyclerAdapter(new ArrayList<>(thumbnails.subList(0,SHOWN_ITEMS)), this);
        //thumbnails_recycler.setAdapter(adapter);

        /*
        //Reviews RecyclerView
        String now = "random date";
        List<Evaluation> evaluations = new ArrayList<>();
        evaluations.add(new Evaluation(0, "aekara_21", "mitsos_creations", 4, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(1, "maria_karen", "mitsos_creations", 3, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(2, "takis_32", "mitsos_creations", 4, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(3, "someone", "mitsos_creations", 5, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(4, "someone2", "mitsos_creations", 4, now, getResources().getString(R.string.lorem_ipsum)));
        evaluations.add(new Evaluation(5, "someone3", "mitsos_creations", 5, now, getResources().getString(R.string.lorem_ipsum)));
        */

        evaluations_recycler = findViewById(R.id.reviews_recyclerview);
        RecyclerView.LayoutManager manager_r = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        evaluations_recycler.setLayoutManager(manager_r);

        //adapter2 = new EvaluationsRecyclerAdapter(evaluations, this);
        //evaluations_recycler.setAdapter(adapter2);

        MaterialButton report_btn = findViewById(R.id.report_btn);
        report_btn.addOnCheckedChangeListener((button, isChecked) -> {
            Intent report = new Intent(CreatorProfile.this, ReportActivity.class);
            // TODO pass creator's info (profile photo,username,email,phone)
            report.putExtra("username", username);
            startActivity(report);
        });

        findViewById(R.id.rewards_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRewardsCustomer(view);
            }
        });

    }

    /** Go back to previous screen */
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
        Intent back;
        if(previous.equals(MainActivity.MAIN)){
            back = new Intent(CreatorProfile.this, MainActivity.class);
        }else{
            back = new Intent(CreatorProfile.this, ListingPublicActivity.class);
            back.putExtra("listing_id", previous); //Send listing id
        }
        startActivity(back);
    }

    /** Open page with rewards as viewed by the customers */
    public void openRewardsCustomer(View view) {
        Intent rewards = new Intent(CreatorProfile.this, RewardsCustomerActivity.class);
        rewards.putExtra("username", username);
        startActivity(rewards);
    }

    /** Open the profile of a user from their evaluation */
    public void openProfile(String username) {
        //TODO check if he is a creator
        Intent profile = new Intent(CreatorProfile.this, CustomerProfile.class);
        profile.putExtra("username", username); //Send username of evaluation
        startActivity(profile);
    }
    public void toggleEditCreator(View view) { }

    /** Moves user to create Listing activity */
    public void addListing(View view) {
        Intent listing = new Intent(CreatorProfile.this, CreateListingActivity.class);
        startActivity(listing);
    }

    /** Moves user to create Evaluation activity */
    public void addEvaluation(View view) {
        Intent eval = new Intent(CreatorProfile.this, EvaluationActivity.class);
        eval.putExtra("username", username);
        startActivity(eval);
    }

    /** View a listing's details */
    public void reviewListing(int listing_id) {
        Intent listing_review;
        if(username.equals(MainActivity.username)) {
            listing_review = new Intent(CreatorProfile.this, ListingPrivateActivity.class);
            listing_review.putExtra("previous", "@profile");
        }else{
            listing_review = new Intent(CreatorProfile.this, ListingPublicActivity.class);
            listing_review.putExtra("previous", username); //TODO send creator's username
        }
        listing_review.putExtra("listing_id", listing_id);
        startActivity(listing_review);
    }

    public List<Thumbnail> getThumbnails() {
        return thumbnails;
    }

}