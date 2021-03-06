package com.craftbid.craftbid;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
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
    public static byte[] thumbnail;
    public static ArrayList<byte[]> listing_photos;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_listing);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            listing_id = b.getInt("listing_id");
            Log.d("LISTING", "onCreate: listing_id");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refreshmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                break;
            case R.id.refresh:
                finish();
                startActivity(getIntent());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    private void goBack() {
        this.finish();
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
        Intent profile;
        if(MainActivity.username.equals(listing.getPublished_by())) {
            profile = new Intent(ListingPublicActivity.this, CreatorProfilePrivate.class);
        }else {
            profile = new Intent(ListingPublicActivity.this, CreatorProfile.class);
        }
        profile.putExtra("username", listing.getPublished_by());
        profile.putExtra("previous", String.valueOf(listing_id));
        startActivity(profile);
    }

    public void seeMore(View view){
        Intent full = new Intent(ListingPublicActivity.this, FullscreenGalleryActivity.class);
        View image = findViewById(R.id.listing_photo);
        Pair<View, String> anim_pair = new Pair<>(image, "thumbnail");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ListingPublicActivity.this, anim_pair);
            startActivity(full, options.toBundle());
        }else{
            startActivity(full);
        }
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
                out.writeObject(listing_id);

                listing = (Listing)in.readObject();

                if(listing != null) {
                    thumbnail = (byte[])in.readObject();
                    listing_photos = (ArrayList<byte[]>) in.readObject();
                    listing_photos.add(0, thumbnail);
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
                value.setText(listing.getReward_points()+"");
                TextView price = findViewById(R.id.price);
                price.setText(String.format("%s", listing.getMin_price()));
                //set the thumbnail
                ImageView photo = findViewById(R.id.listing_photo);
                Bitmap thumbnail_view = BitmapFactory.decodeByteArray(thumbnail,0, thumbnail.length);
                photo.setImageBitmap(thumbnail_view);
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
