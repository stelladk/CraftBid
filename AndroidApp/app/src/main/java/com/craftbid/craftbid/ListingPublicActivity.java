package com.craftbid.craftbid;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

import com.craftbid.craftbid.model.Listing;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ListingPublicActivity extends AppCompatActivity {
    private int listing_id;
    private static String previous;
    Dialog dialog;

    public static Listing listing;
    public static ArrayList<byte[]> listing_photos;

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

        new LoadListingTask().execute(); //Load listing info

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
        profile.putExtra("username", "mam"); //TODO send creators username
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

    private class LoadListingTask extends AsyncTask<Void, Void, Void>{
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        @Override
        protected Void doInBackground(Void... voids) {
            //connect to server to load listing info
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("VIEW_LISTING");
//                out.writeObject(listing_id);
                out.writeObject(5); //TODO change back to listing_id

                listing = (Listing)in.readObject();
                if(listing != null) {
                    listing_photos = (ArrayList<byte[]>) in.readObject();
                }
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ListingPublicActivity.this,
                    "View Listing...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            if(listing != null) {
                TextView name = findViewById(R.id.listing_name);
                name.setText(listing.getName());
                Button creator_btn = findViewById(R.id.creator_btn);
                creator_btn.setText(listing.getPublished_by());
                TextView details = findViewById(R.id.details);
                details.setText(listing.getDescription());
                TextView location = findViewById(R.id.location);
                location.setText(listing.getLocation());
                TextView value = findViewById(R.id.points_value);
                Log.d("PUBLIC LISTING", "onPostExecute: value "+value);
                value.setText(listing.getReward_points()+"");
                TextView price = findViewById(R.id.price);
                price.setText(String.format("%s", listing.getMin_price()));
                Log.d("LISTING PUBLIC", "onPostExecute: photos "+listing.getTotal_photos());
                Log.d("LISTING PUBLIC", "onPostExecute: listing_photos "+listing_photos.size());
                if(listing_photos.size() > 0){
                    ImageView photo = findViewById(R.id.listing_photo);
                    Bitmap thumbnail = BitmapFactory.decodeByteArray(listing_photos.get(0),0, listing_photos.get(0).length);
                    photo.setImageBitmap(thumbnail);
                }
            }
            try {
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
