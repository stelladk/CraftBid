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
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_listing);

        Bundle b = getIntent().getExtras();
        int listing_id;
        if(b!=null){
            listing_id = b.getInt("listing_id");
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
            startActivity(offers);
        });
        edit_btn.setOnClickListener(v -> {
            Intent edit_listing = new Intent(ListingPrivateActivity.this, EditListingActivity.class);
            startActivity(edit_listing);
        });
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
        Intent creator = new Intent(ListingPrivateActivity.this, CreatorProfilePrivate.class);
        startActivity(creator);
    }
}
