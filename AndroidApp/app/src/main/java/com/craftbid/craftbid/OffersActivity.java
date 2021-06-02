package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.adapters.OffersRecyclerAdapter;
import com.craftbid.craftbid.model.Listing;
import com.craftbid.craftbid.model.Notification;
import com.craftbid.craftbid.model.Offer;
import com.craftbid.craftbid.model.Thumbnail;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class OffersActivity extends AppCompatActivity {
    private int listing_id;
    private List<Offer> offers;
    private OffersRecyclerAdapter adapter;
    private RecyclerView recycler;
    private TextView empty_message;
    private Dialog dialog;

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

        empty_message = findViewById(R.id.empty_msg);

        recycler = findViewById(R.id.offers_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);

        new LoadOffersTask().execute();
        dialog = new Dialog(this);
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

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(OffersActivity.this,
                    "Getting all offers for this listing...",
                    "Connecting to server...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("VIEW_OFFERS");
                out.writeObject(listing_id);
                // get offers list
                offers = (ArrayList<Offer>) in.readObject();
            }catch(IOException | ClassNotFoundException e) {
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
            progressDialog.dismiss();
            adapter = new OffersRecyclerAdapter(offers, OffersActivity.this);
            recycler.setAdapter(adapter);
            toggleEmptyMessage();
        }
    }

    /** On accepting an offer, connects to server to store customer's Notification in DB*/
    private class AcceptOfferTask extends AsyncTask<Offer, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(OffersActivity.this,
                    "Accepting offer...",
                    "Connecting to server...");
        }

        @Override
        protected Void doInBackground(Offer... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("SEND_NOTIFICATION");

                out.writeObject(new Notification(params[0].getSubmitted_for(), params[0].getSubmitted_by(), params[0].getPrice()));
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
            progressDialog.dismiss();
            adapter.notifyDataSetChanged();
            goBack();
        }
    }

    /** On declining an offer, connects to server to delete customer's offer from DB*/
    private class DeclineOfferTask extends AsyncTask<Offer, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(OffersActivity.this,
                    "Declining offer...",
                    "Connecting to server...");
        }

        @Override
        protected Void doInBackground(Offer... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("DECLINE_OFFER");

                out.writeObject(params[0].getId());
                // TODO after server's last response/success
                offers.remove(params[0]);
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
            progressDialog.dismiss();
            adapter.notifyDataSetChanged();
            dialog.dismiss();
            toggleEmptyMessage();
        }
    }
}