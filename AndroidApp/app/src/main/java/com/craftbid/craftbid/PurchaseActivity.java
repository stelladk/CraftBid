package com.craftbid.craftbid;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.craftbid.craftbid.model.Listing;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class PurchaseActivity extends AppCompatActivity {
    private int listing_id;
    private TextView purchase_location;
    private Listing listing;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            listing_id = b.getInt("listing_id");
        }
        purchase_location = findViewById(R.id.purchase_location);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        new LoadPurchaseScreenTask().execute();
        dialog = new Dialog(this);
    }

    /** Disables unavailable delivery option if any*/
    private void availableDeliveryOptions(String delivery) {
        RadioButton hand = findViewById(R.id.delivery_handToHand);
        RadioButton courier = findViewById(R.id.delivery_courier);
        if(delivery.equalsIgnoreCase(getResources().getString(R.string.shipment))){
            hand.setTextColor(getResources().getColor(R.color.grey));
            hand.setEnabled(false);
            courier.setChecked(true);
            LinearLayout details = findViewById(R.id.purchase_layout_hand);
            final int childCount = details.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View v = details.getChildAt(i);
                v.setEnabled(false);
                if (v instanceof TextView)
                    ((TextView) v).setTextColor(getResources().getColor(R.color.grey));
                else if (v instanceof Button)
                    ((Button) v).setBackgroundColor(getResources().getColor(R.color.grey));
            }
            findViewById(R.id.hand_not_offered).setVisibility(View.VISIBLE);
        }
        else if(delivery.equalsIgnoreCase(getResources().getString(R.string.hand_in_hand))) {
            courier.setTextColor(getResources().getColor(R.color.grey));
            courier.setEnabled(false);
            hand.setChecked(true);
            LinearLayout details = findViewById(R.id.purchase_layout_courier);
            int childCount = details.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View v = details.getChildAt(i);
                v.setEnabled(false);
                if (v instanceof EditText)
                    ((EditText) v).setTextColor(getResources().getColor(R.color.grey));
                else if (v instanceof Button)
                    ((Button) v).setBackgroundColor(getResources().getColor(R.color.grey));
                else if (v instanceof TextView)
                    ((TextView) v).setTextColor(getResources().getColor(R.color.grey));
            }

            details = findViewById(R.id.purchase_layout_courier_numbers);
            childCount = details.getChildCount();
            for (int i = 0; i < childCount; i++) {
                EditText v = (EditText) details.getChildAt(i);
                v.setEnabled(false);
                v.setHintTextColor(getResources().getColor(R.color.grey));
            }
            findViewById(R.id.courier_not_offerer).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBack();
        }
        return super.onOptionsItemSelected(item);
    }
    private void goBack() {
        Intent notifications = new Intent(PurchaseActivity.this, NotificationsActivity.class);
        startActivity(notifications);
    }

    /** Gets contact info from server and opens popup after server's response*/
    public void contactCreator(View view) {
        dialog.show();
    }
    public void closePopup(View view) {
        dialog.dismiss();
    }

    /** Loads delivery options and location on screen creation.
     * If hand-to-hand option is available, loads creator's information too. */
    private class LoadPurchaseScreenTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private Socket socket = null;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;
        private final String resultmsg = "Προέκυψε σφάλμα.";
        private boolean is_successful = true;
        private ArrayList<String> basicInfo;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PurchaseActivity.this,
                    "Load delivery options and contact info...",
                    "Connecting to server...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket(NetInfo.getServer_ip(), NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("VIEW_LISTING");
                out.writeObject(listing_id);
                listing = (Listing) in.readObject();
                if(listing==null) is_successful=false;

                // non needed info
                in.readObject(); //thumbnail
                in.readObject(); //photos
                close();

                // TODO after DB's collation change, make appropriate changes for delivery options
                if(!listing.getDelivery().equals(getResources().getString(R.string.shipment))){
                    socket = new Socket(NetInfo.getServer_ip(), NetInfo.getServer_port());
                    in = new ObjectInputStream(socket.getInputStream());
                    out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject("REQUEST_PROFILE");
                    out.writeObject(listing.getPublished_by());
                    out.writeObject(false);  // only basic info is needed
                    basicInfo = (ArrayList<String>) in.readObject();

                    // non needed info
                    in.readObject(); // photo
                    in.readObject(); // evaluations
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                is_successful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            close();
            progressDialog.dismiss();
            if(!is_successful) {
                Snackbar.make(getWindow().getDecorView().getRootView(), resultmsg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                goBack();
            }
            purchase_location.setText(listing.getLocation());
            availableDeliveryOptions(listing.getDelivery());

            // TODO same here
            // set hand-to-hand popup parameters
            if(!listing.getDelivery().equals(getResources().getString(R.string.shipment))) {
                String email = basicInfo.get(1);
                String phone = basicInfo.get(2);
                dialog.setContentView(R.layout.popup_purchase);
                ((TextView)dialog.findViewById(R.id.contact_phone)).setText(phone);
                ((TextView)dialog.findViewById(R.id.contact_email)).setText(email);
                dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
                dialog.findViewById(R.id.purchase_okay).setOnClickListener(v -> {
                    // TODO new PurchaseTask().execute();
                });
            }
        }

        private void close(){
            try {
                socket.close();
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
