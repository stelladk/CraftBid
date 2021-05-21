package com.craftbid.craftbid;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ListingPrivateActivity extends AppCompatActivity {
    private int listing_id;
    private String previous;
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_listing);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            listing_id = b.getInt("listing_id");
            previous = b.getString("previous");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Button offers_btn = findViewById(R.id.show_offers_btn);
        Button edit_btn = findViewById(R.id.edit_btn);

        findViewById(R.id.make_offer_btn).setVisibility(View.INVISIBLE);
        offers_btn.setVisibility(View.VISIBLE);
        edit_btn.setVisibility(View.VISIBLE);

        offers_btn.setOnClickListener(v -> {
            Intent offers = new Intent(ListingPrivateActivity.this, OffersActivity.class);
            offers.putExtra("listing_id", listing_id);
            startActivity(offers);
        });
        edit_btn.setOnClickListener(v -> {
            Intent edit_listing = new Intent(ListingPrivateActivity.this, EditListingActivity.class);
            edit_listing.putExtra("listing_id", listing_id);
            startActivity(edit_listing);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void goBack() {
        Intent back;
        if(previous.equals("@main"))
            back = new Intent(ListingPrivateActivity.this, MainActivity.class);
        else
            back = new Intent(ListingPrivateActivity.this, CreatorProfilePrivate.class);
        startActivity(back);
    }

    private void openProfile(View view) {
        Intent profile = new Intent(ListingPrivateActivity.this, CreatorProfilePrivate.class);
        profile.putExtra("previous", listing_id); //Send listing id
        startActivity(profile);
    }
}
