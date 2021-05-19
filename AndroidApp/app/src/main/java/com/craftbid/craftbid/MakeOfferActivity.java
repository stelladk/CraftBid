package com.craftbid.craftbid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MakeOfferActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_offer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView initialPrice = findViewById(R.id.price);
        Button creator = findViewById(R.id.creator_btn);
        ImageView photo = findViewById(R.id.listing_photo);
        TextView description = findViewById(R.id.listing_description);
        TextView location = findViewById(R.id.location);
        TextView points = findViewById(R.id.points_value);
        TextView delivery = findViewById(R.id.delivery);
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
        Intent creator = new Intent(MakeOfferActivity.this, ListingPublicActivity.class);
        startActivity(creator);
    }

    // TODO
    public void viewPhotos(View view) {
        Log.d("photos", "Viewing photos");
    }

    public void submitOffer(View view) {
        Log.d("submit", "Submit offer");
    }
}
