package com.craftbid.craftbid;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ListingPrivateActivity extends ListingPublicActivity {
    private int listing_id;
    private static String previous;
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_listing);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            listing_id = b.getInt("listing_id");
            previous = b.getString("previous", previous);
        }
        Log.d("ListingPrivate", "onCreate: previous "+previous);

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
            offers.putExtra("listing_location", listing.getLocation());
            offers.putExtra("listing_points", listing.getReward_points());
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

    public void openProfile(View view) {
        Intent profile = new Intent(ListingPrivateActivity.this, CreatorProfilePrivate.class);
        profile.putExtra("previous", listing_id); //Send listing id
        startActivity(profile);
    }

    public void seeMore(View view){
        Intent full = new Intent(ListingPrivateActivity.this, FullscreenGalleryActivity.class);
        View image = findViewById(R.id.listing_photo);
        Pair<View, String> anim_pair = new Pair<>(image, "thumbnail");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ListingPrivateActivity.this, anim_pair);
            startActivity(full, options.toBundle());
        }else{
            startActivity(full);
        }
    }
}
