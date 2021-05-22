package com.craftbid.craftbid;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ListingPublicActivity extends AppCompatActivity {
    private int listing_id;
    private static String previous;
    Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_listing);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            listing_id = b.getInt("listing_id");
            previous = b.getString("previous", previous);
        }
        Log.d("ListingPublic", "onCreate: listing_id "+listing_id);

        if(previous.equals(MainActivity.GUEST)){
            findViewById(R.id.creator_btn).setEnabled(false);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dialog = new Dialog(this);
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
        Intent back;
        if(previous.equals(MainActivity.MAIN) || previous.equals(MainActivity.GUEST))
            back = new Intent(ListingPublicActivity.this, MainActivity.class);
        else {
            back = new Intent(ListingPublicActivity.this, CreatorProfile.class);
            back.putExtra("username", previous);
        }
        startActivity(back);
    }

    public void makeOffer(View view) {
        if(previous.equals(MainActivity.GUEST)){
            showPopup();
            return;
        }
        Intent offer = new Intent(ListingPublicActivity.this, MakeOfferActivity.class);
        offer.putExtra("listing_id", listing_id);
        startActivity(offer);
    }

    public void openProfile(View view){
        Intent profile = new Intent(ListingPublicActivity.this, CreatorProfile.class);
        profile.putExtra("username", "username"); //send creators username
        profile.putExtra("previous", String.valueOf(listing_id));
        startActivity(profile);
    }

    private void showPopup(){
        dialog.setContentView(R.layout.popup_text);
        ((TextView)dialog.findViewById(R.id.help)).setText(R.string.signIn_warning);
        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        dialog.show();

        dialog.findViewById(R.id.close_btn).setOnClickListener(v -> dialog.dismiss());
    }
}
