package com.craftbid.craftbid;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.craftbid.craftbid.model.Listing;
import com.craftbid.craftbid.model.Offer;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MakeOfferActivity extends AppCompatActivity {
    Toolbar toolbar;
    private int listing_id;
    private Listing listing;
    private ArrayList<byte[]> listing_photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_offer);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            listing_id = b.getInt("listing_id"); //Do we need this?
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        if(ListingPublicActivity.listing != null){
            listing = ListingPublicActivity.listing;
            listing_photos = ListingPublicActivity.listing_photos;
            loadListing();
        }

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
        Intent listing = new Intent(MakeOfferActivity.this, ListingPublicActivity.class);
        listing.putExtra("listing_id", listing_id);
        startActivity(listing);
    }

    private void loadListing(){
//        TextView name = findViewById(R.id.listing_name);
        toolbar.setTitle(listing.getName());
        Button creator_btn = findViewById(R.id.creator_btn);
        creator_btn.setText(listing.getPublished_by());
        TextView details = findViewById(R.id.listing_description);
        details.setText(listing.getDescription());
        TextView location = findViewById(R.id.location);
        location.setText(listing.getLocation());
        TextView value = findViewById(R.id.points_value);
        value.setText(String.format("%d", listing.getReward_points()));
        TextView price = findViewById(R.id.price);
        price.setText(String.format("%s", listing.getMin_price()));
        TextView delivery = findViewById(R.id.delivery);
        //Set delivery methods
        String deliveryText = "";
        if(listing.getDelivery().toLowerCase().contains("shipping")){
            deliveryText = "ταχυδρομικά";
            if(listing.getDelivery().toLowerCase().contains("hand-in-hand")){
                findViewById(R.id.handin_check).setSelected(true);
                deliveryText += ", χέρι-με-χέρι";
            }
        }else if(listing.getDelivery().toLowerCase().contains("hand-in-hand")){
            findViewById(R.id.handin_check).setSelected(true);
            deliveryText += "χέρι-με-χέρι";
        }
        delivery.setText(deliveryText);
        if(listing_photos!=null && listing_photos.size()>0){
            ImageView photo = findViewById(R.id.listing_photo);
            Bitmap thumbnail = BitmapFactory.decodeByteArray(listing_photos.get(0),0, listing_photos.get(0).length);
            photo.setImageBitmap(thumbnail);
        }
    }

    public void viewPhotos(View view) {
        Intent full = new Intent(MakeOfferActivity.this, FullscreenGalleryActivity.class);
        startActivity(full);
    }

    public void submitOffer(View view) {
        Log.d("submit", "Submit offer");
        new SubmitTask().execute();
    }

    private class SubmitTask extends AsyncTask<Void, Void, Void>{
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        @Override
        protected Void doInBackground(Void... voids) {
            //connect to server to loa listing info
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("CREATE_BID");
                //Create Offer object
                float price = Float.parseFloat(((EditText)findViewById(R.id.offer)).getText().toString());
                Offer bid = new Offer(-1, listing.getId(), MainActivity.username, price);
                out.writeObject(bid);
            }catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MakeOfferActivity.this,
                    "Submit Offer...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            try{
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            goBack();
        }
    }
}
