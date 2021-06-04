package com.craftbid.craftbid;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.adapters.OffersRecyclerAdapter;
import com.craftbid.craftbid.model.Notification;
import com.craftbid.craftbid.model.Offer;
import com.craftbid.craftbid.model.Thumbnail;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OffersActivity extends AppCompatActivity {
    private int listing_id, points;
    private String location;
    private List<Offer> offers;
    private OffersRecyclerAdapter adapter;
    private RecyclerView recycler;
    private TextView empty_message, listing_details, listing_location, listing_points;
    private ImageView listing_photo;
    private Dialog dialog;
    private boolean after_accept =  false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            listing_id = b.getInt("listing_id");
            location = b.getString("listing_location");
            points = b.getInt("listing_points", 0);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Προσφορές");

        //Set Back Arrow
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        empty_message = findViewById(R.id.empty_msg);
        listing_details = findViewById(R.id.offer_listing_details);
        listing_location = findViewById(R.id.offer_location);
        listing_points = findViewById(R.id.offer_points_value);
        listing_photo = findViewById(R.id.offer_listing_photo);

        recycler = findViewById(R.id.offers_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);
        dialog = new Dialog(this);

        new LoadOffersTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void goBack() {
        Intent listing = new Intent(OffersActivity.this, ListingPrivateActivity.class);
        listing.putExtra("listing_id", listing_id);
        startActivity(listing);
    }

    /** Shows message if offers list is empty */
    private void toggleEmptyMessage(){
        if(adapter.getItemCount() == 0){
            empty_message.setVisibility(View.VISIBLE);
        }else{
            empty_message.setVisibility(View.GONE);
        }
    }

    /** Opens popup for confirmation and runs AsyncTask to send accept offer request to AppServer */
    public void acceptOffer(Offer offer) {
        dialog.setContentView(R.layout.popup_confirm);
        ((TextView)dialog.findViewById(R.id.confirm_message)).setText(R.string.accept_offer);
        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        dialog.show();
        dialog.findViewById(R.id.yes_btn).setOnClickListener(v -> {
            new AcceptOfferTask().execute(offer);
            dialog.dismiss();
        });
        dialog.findViewById(R.id.no_btn).setOnClickListener(v -> {
            dialog.dismiss();
        });
    }
    /** Opens popup for confirmation and runs AsyncTask to send decline offer request to AppServer*/
    public void declineOffer(Offer offer) {
        dialog.setContentView(R.layout.popup_confirm);
        ((TextView)dialog.findViewById(R.id.confirm_message)).setText(R.string.decline_offer);
        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        dialog.show();
        dialog.findViewById(R.id.yes_btn).setOnClickListener(v -> {
            new DeclineOfferTask().execute(offer);
        });
        dialog.findViewById(R.id.no_btn).setOnClickListener(v -> {
            dialog.dismiss();
        });
    }
    public void closePopup(View view) {
        dialog.dismiss();
    }

    public void openProfile(String username) {
        //TODO check if he is a creator
        Intent profile = new Intent(OffersActivity.this, CustomerProfile.class);
        profile.putExtra("username", username); //Send user's username
        startActivity(profile);
    }

    /** On creation of screen, connects to server to get list of Offers for particular listing*/
    private class LoadOffersTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Thumbnail thumbnail;
        boolean is_successful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(OffersActivity.this,
                    "Getting all offers for this listing...",
                    "Connecting to server...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // get listing's info
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("SEARCH");
                out.writeObject(true);  //request search by listing_id
                out.writeObject(String.valueOf(listing_id));
                thumbnail = (Thumbnail) ((ArrayList<Thumbnail>) in.readObject()).get(0);
                close();

                // get offers request
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("VIEW_OFFERS");
                out.writeObject(listing_id);
                // get offers list
                offers = (ArrayList<Offer>) in.readObject();

            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
                is_successful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            close();

            if(!is_successful){
                Snackbar.make(getWindow().getDecorView().getRootView(), "Προέκυψε σφάλμα", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                goBack();
            }
            // set listing image
            byte[] photo = thumbnail.getThumbnail();
            if(photo!=null) {
                Bitmap view = BitmapFactory.decodeByteArray(photo,0, photo.length);
                Drawable d = new BitmapDrawable(getResources(), view);
                listing_photo.setBackground(d);
            }
            // set description, location, points
            listing_details.setText(thumbnail.getDescription());
            listing_location.setText(location);
            listing_points.setText(String.valueOf(points));

            progressDialog.dismiss();
            adapter = new OffersRecyclerAdapter(offers, OffersActivity.this);
            recycler.setAdapter(adapter);
            toggleEmptyMessage();
        }

        private void close() {
            try {
                in.close();
                out.close();
                socket.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** On accepting an offer, connects to server to store customer's Notification in DB*/
    private class AcceptOfferTask extends AsyncTask<Offer, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        private Offer offer;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(OffersActivity.this,
                    "Accepting offer...",
                    "Connecting to server...");
        }

        @Override
        protected Void doInBackground(Offer... params) {
            offer = params[0];
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("SEND_NOTIFICATION");

                out.writeObject(new Notification(offer.getSubmitted_for(), offer.getSubmitted_by(), offer.getPrice()));
            }catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                in.close();
                out.close();
                socket.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
            // send decline_offer to remove offer from DB
            after_accept = true;
            new DeclineOfferTask().execute(offer);

            progressDialog.dismiss();
        }
    }

    /** On declining an offer, connects to server to delete customer's offer from DB*/
    private class DeclineOfferTask extends AsyncTask<Offer, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        private boolean is_successful = false;

        @Override
        protected void onPreExecute() {
            if(!after_accept) {
                progressDialog = ProgressDialog.show(OffersActivity.this,
                        "Declining offer...",
                        "Connecting to server...");
            }
        }

        @Override
        protected Void doInBackground(Offer... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("DECLINE_OFFER");
                out.writeObject(params[0].getId());

                String response = (String) in.readObject();
                if(response.equals("OFFER DECLINED")) {
                    is_successful = true;
                    offers.remove(params[0]);
                }
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if(in!=null) in.close();
                if(out!=null) out.close();
                if(socket!=null) socket.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
            if(!after_accept) {
                progressDialog.dismiss();
            }
            if(!is_successful) {
                Snackbar.make(getWindow().getDecorView().getRootView(), "Προέκυψε σφάλμα.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            dialog.dismiss();
            adapter.notifyDataSetChanged();
            toggleEmptyMessage();
            after_accept = false;
        }
    }
}