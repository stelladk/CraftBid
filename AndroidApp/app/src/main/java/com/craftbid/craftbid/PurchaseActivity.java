package com.craftbid.craftbid;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.craftbid.craftbid.model.Listing;
import com.craftbid.craftbid.model.Reward;
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

        new LoadDeliveryOptionsTask().execute();
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
        Intent main = new Intent(PurchaseActivity.this, MainActivity.class);
        startActivity(main);
    }

    public void openMain(View view) {
        goBack();
    }

    /** Loads delivery options and location on screen creation */
    private class LoadDeliveryOptionsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private Socket socket = null;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;
        private final String resultmsg = "Προέκυψε σφάλμα.";
        private boolean is_successful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PurchaseActivity.this,
                    "Load delivery options...",
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
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                is_successful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            try {
                socket.close();
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(!is_successful) {
                Snackbar.make(getWindow().getDecorView().getRootView(), resultmsg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                goBack();
            }
            purchase_location.setText(listing.getLocation());
            availableDeliveryOptions(listing.getDelivery());
        }
    }
}
