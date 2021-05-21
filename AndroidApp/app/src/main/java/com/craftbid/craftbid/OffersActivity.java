package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.adapters.OffersRecyclerAdapter;
import com.craftbid.craftbid.model.Offer;
import com.craftbid.craftbid.model.Thumbnail;

import java.util.ArrayList;
import java.util.List;

public class OffersActivity extends AppCompatActivity {
    private int listing_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            listing_id = b.getInt("listing_id");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Προσφορές");

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //offers RecyclerView
        List<Offer> offers = new ArrayList<>();
        offers.add(new Offer(0, 12, "maria_karen", 30.45f));
        offers.add(new Offer(1, 12, "maria_karen", 10.45f));
        offers.add(new Offer(2, 12, "maria_karen", 20.45f));
        offers.add(new Offer(3, 12, "maria_karen", 7.45f));


        RecyclerView recycler = findViewById(R.id.offers_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);
        OffersRecyclerAdapter adapter = new OffersRecyclerAdapter(offers, this);
        recycler.setAdapter(adapter);
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
        Intent listing = new Intent(OffersActivity.this, ListingPrivateActivity.class);
        listing.putExtra("listing_id", listing_id);
        startActivity(listing);
    }

    public void acceptOffer(int id) {
        //TODO show popup
    }

    public void declineOffer(int id) {
        //TODO show popup
    }

    public void openProfile(String username) {
        //TODO check if he is a creator
        Intent profile = new Intent(OffersActivity.this, CustomerProfile.class);
        profile.putExtra("username", username); //Send user's username
        startActivity(profile);
    }
}