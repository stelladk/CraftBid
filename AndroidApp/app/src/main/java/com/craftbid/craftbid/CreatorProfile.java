package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.EvaluationsRecyclerAdapter;
import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.model.Evaluation;
import com.craftbid.craftbid.model.Listing;
import com.craftbid.craftbid.model.Thumbnail;
import com.google.android.material.button.MaterialButton;
import com.stelladk.arclib.ArcLayout;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class CreatorProfile extends AppCompatActivity {
    private String username;
    private static String previous;

    protected ArrayList<Thumbnail> thumbnails;
    protected ArrayList<Evaluation> evaluations;
    protected RecyclerView thumbnails_recycler;
    protected RecyclerView evaluations_recycler;
    protected FeedRecyclerAdapter adapter;
    protected EvaluationsRecyclerAdapter adapter2;
    protected TextView fullname, email, phone, description, freelancer,expertise;
    protected EditText fullname_edit, email_edit, phone_edit, description_edit;
    protected CheckBox freelancer_choice;
    protected ArcLayout profile_pic;
    protected byte[] pfp;

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

        thumbnails_recycler = findViewById(R.id.thumbnails_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        thumbnails_recycler.setLayoutManager(manager);

        evaluations_recycler = findViewById(R.id.reviews_recyclerview);
        RecyclerView.LayoutManager manager_r = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        evaluations_recycler.setLayoutManager(manager_r);

        fullname_edit = findViewById(R.id.fullname_edit);
        fullname = findViewById(R.id.fullname);
        email_edit = findViewById(R.id.email_edit);
        email = findViewById(R.id.email);
        phone_edit = findViewById(R.id.phone_edit);
        phone = findViewById(R.id.phone);
        description_edit = findViewById(R.id.description_edit);
        description = findViewById(R.id.description);
        freelancer_choice = findViewById(R.id.freelancer_choice);
        freelancer = findViewById(R.id.freelancer);
        expertise = findViewById(R.id.expertise);
        profile_pic = findViewById(R.id.profile_photo);

        //Add async task to get profile info from server
        new GetInfoTask().execute();

        //report button
        MaterialButton report_btn = findViewById(R.id.report_btn);
        report_btn.addOnCheckedChangeListener((button, isChecked) -> {
            Intent report = new Intent(CreatorProfile.this, ReportActivity.class);
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
        this.finish();
        /*
        Intent back;
        if(previous.equals(MainActivity.MAIN)){
            back = new Intent(CreatorProfile.this, MainActivity.class);
        }else{
            back = new Intent(CreatorProfile.this, ListingPublicActivity.class);
            back.putExtra("listing_id", previous); //Send listing id
        }
        startActivity(back);
         */
    }

    /** Open page with rewards as viewed by the customers */
    public void openRewardsCustomer(View view) {
        Intent rewards = new Intent(CreatorProfile.this, RewardsCustomerActivity.class);
        rewards.putExtra("username", username);
        startActivity(rewards);
    }

    /** Open the profile of a user from their evaluation */
    public void openProfile(String username) {
        //TODO check if he is a creator (creators can add evaluations too)
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



    /** AsyncTask running when screen is created, connecting to server to get user info */
    private class GetInfoTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String fullname_txt,email_txt,phone_txt,descr_txt,hasExpertise_txt;
        int isFreelancer;

        @Override
        protected Void doInBackground(String... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("REQUEST_PROFILE");
                out.writeObject(username);
                out.writeObject(true);
                out.flush();
                //get basic info
                ArrayList<String> info = (ArrayList<String>)in.readObject();
                fullname_txt = info.get(0);
                email_txt = info.get(1);
                phone_txt = info.get(2);
                descr_txt = info.get(3);
                //get profile pic
                pfp = (byte[])in.readObject();
                //get additional creator info
                hasExpertise_txt = (String)in.readObject();
                isFreelancer = (int)in.readObject();
                //get list of evaluations
                evaluations = (ArrayList<Evaluation>)in.readObject();
                //get list of listing thumbnails
                thumbnails = (ArrayList<Thumbnail>)in.readObject();
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CreatorProfile.this,
                    "Getting profile info! ...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                in.close();
                out.close();
                socket.close();
            }catch(IOException e) {
                e.printStackTrace();
            }

            fullname.setText(fullname_txt);
            fullname_edit.setText(fullname_txt);
            email.setText(email_txt);
            email_edit.setText(email_txt);
            phone.setText(phone_txt);
            phone_edit.setText(phone_txt);
            description.setText(descr_txt);
            description_edit.setText(descr_txt);
            freelancer.setText(isFreelancer==1 ? "Freelancer" : "");
            freelancer_choice.setChecked(isFreelancer==1 ? true : false);
            expertise.setText(hasExpertise_txt);
            //view profile picture
            if(pfp!=null) {
                Bitmap pfp_view = BitmapFactory.decodeByteArray(pfp,0, pfp.length);
                Drawable d = new BitmapDrawable(getResources(), pfp_view);
                profile_pic.setBackground(d);
            }
            //change listings and evaluations
            //listings
            adapter = new FeedRecyclerAdapter(thumbnails, CreatorProfile.this);
            thumbnails_recycler.setAdapter(adapter);
            //evaluations
            adapter2 = new EvaluationsRecyclerAdapter(evaluations, CreatorProfile.this);
            evaluations_recycler.setAdapter(adapter2);

            progressDialog.dismiss();
        }
    }//get info task

}