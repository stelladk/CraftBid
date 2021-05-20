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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.EvaluationsRecyclerAdapter;
import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.model.Evaluation;
import com.craftbid.craftbid.model.Thumbnail;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreatorProfilePrivate extends CreatorProfile {

    private static boolean SAVE_MODE=false;

    private EditText fullname_edit, email_edit, phone_edit, description_edit;
    private TextView fullname, email, phone, description, freelancer;
    private CheckBox freelancer_choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button edit_btn = findViewById(R.id.edit_btn);
        Button add_listing_btn = findViewById(R.id.add_listing_btn);
        MaterialButton report_btn = findViewById(R.id.report_btn);

        edit_btn.setVisibility(View.VISIBLE);
        add_listing_btn.setVisibility(View.VISIBLE);
        report_btn.setVisibility(View.INVISIBLE);


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
    }

    @Override
    public void reviewListing(int listing_id){
        Intent listing_review;
        listing_review = new Intent(CreatorProfilePrivate.this, ListingPrivateActivity.class);
        listing_review.putExtra("listing_id", listing_id);
        listing_review.putExtra("previous", "@profile");
        startActivity(listing_review);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

//    private void goBack() {
//        Intent main = new Intent(CreatorProfilePrivate.this, MainActivity.class);
//        startActivity(main);
//    }
//
//    public void openRewardsCreator(View view) {
//        Intent rewards = new Intent(CreatorProfilePrivate.this, RewardsCreatorActivity.class);
//        startActivity(rewards);
//    }

    @Override
    public void toggleEditCreator(View view){
        if(SAVE_MODE){
            saveCreator(view);
        }else{
            editCreator(view);
        }
        SAVE_MODE = !SAVE_MODE;
    }

    public void editCreator(View view) {
        Button edit_btn = (Button)view;
        edit_btn.setText(getResources().getString(R.string.save));

        fullname_edit.setVisibility(View.VISIBLE);
        email_edit.setVisibility(View.VISIBLE);
        phone_edit.setVisibility(View.VISIBLE);
        description_edit.setVisibility(View.VISIBLE);
        freelancer_choice.setVisibility(View.VISIBLE);

        fullname.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
        phone.setVisibility(View.GONE);
        description.setVisibility(View.GONE);
        freelancer.setVisibility(View.GONE);
    }

    public void saveCreator(View view) {
        Button edit_btn = (Button)view;
        edit_btn.setText(getResources().getString(R.string.edit));

        fullname_edit.setVisibility(View.GONE);
        email_edit.setVisibility(View.GONE);
        phone_edit.setVisibility(View.GONE);
        description_edit.setVisibility(View.GONE);
        freelancer_choice.setVisibility(View.GONE);

        fullname.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);
        phone.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
        freelancer.setVisibility(View.VISIBLE);
    }
}