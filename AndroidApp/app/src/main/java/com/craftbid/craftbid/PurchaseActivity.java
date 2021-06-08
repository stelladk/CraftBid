package com.craftbid.craftbid;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import com.craftbid.craftbid.model.Purchase;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;

public class PurchaseActivity extends AppCompatActivity {
    private int listing_id;
    private TextView purchase_location;
    private EditText address, address_number, address_TK;
    private Listing listing;
    private Dialog dialog, dialog2;
    private RadioButton courier, handTohand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            listing_id = b.getInt("listing_id");
        }
        purchase_location = findViewById(R.id.purchase_location);
        address = findViewById(R.id.purchase_address_edit);
        address_number = findViewById(R.id.purchase_number_edit);
        address_TK = findViewById(R.id.purchase_TK_edit);
        courier = findViewById(R.id.delivery_courier);
        handTohand = findViewById(R.id.delivery_handToHand);

        addFocusListeners(address);
        addFocusListeners(address_number);
        addFocusListeners(address_TK);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        new LoadPurchaseScreenTask().execute();
        dialog = new Dialog(this);
        dialog2 = new Dialog(this);
    }

    /** Adds focus listener in input field to check right radiobutton*/
    private void addFocusListeners(EditText field) {
        field.setOnFocusChangeListener((v, hasFocus) -> {
            handTohand.setChecked(!hasFocus);
            courier.setChecked(hasFocus);
        });
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
        this.finish();
        /*
        Intent notifications = new Intent(PurchaseActivity.this, NotificationsActivity.class);
        startActivity(notifications);
         */
    }

    /** For courier delivery, connects to server to store purchase in DB*/
    public void submitPurchase(View view) {
        courier.setChecked(true);
        CharSequence addr_num = address_number.getText();
        CharSequence addr_TK = address_TK.getText();
        if((address.getText()==null || addr_num==null || addr_TK==null) ||
                (addr_num.length()==0 || addr_TK.length()==0) ||
                (Integer.parseInt(addr_num.toString())==0 || Integer.parseInt(addr_TK.toString())==0)) {
            Snackbar.make(getWindow().getDecorView().getRootView(), "Παρακαλώ συμπληρώστε ορθά όλα τα στοιχεία.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        dialog2.setContentView(R.layout.popup_shipping);
        ((TextView)dialog2.findViewById(R.id.shipping_address)).setText(address.getText());
        ((TextView)dialog2.findViewById(R.id.shipping_number)).setText(addr_num);
        ((TextView)dialog2.findViewById(R.id.shipping_TK)).setText(addr_TK);
        dialog2.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        dialog2.findViewById(R.id.purchase_okay).setOnClickListener(v -> {
            new PurchaseTask().execute();
        });
        dialog2.show();
    }

    /** Opens popup with creator's contact info */
    public void contactCreator(View view) {
        handTohand.setChecked(true);
        removeFocus();
        dialog.show();
    }
    public void closePopup(View view) {
        if(dialog.isShowing()) dialog.dismiss();
        else dialog2.dismiss();
    }


    /** Set radiobutton checked based on user's click on screen */
    public void setChecked(View view) {
        if(view.getId() == R.id.purchase_layout_courier) {
            courier.setChecked(true);
        }
        else {
            handTohand.setChecked(true);
            removeFocus();
        }
    }

    /** Removes focus from input fields*/
    private void removeFocus() {
        address_number.clearFocus();
        address.clearFocus();
        address_TK.clearFocus();
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
            if(!listing.getDelivery().equalsIgnoreCase(getResources().getString(R.string.shipment))) {
                String email = basicInfo.get(1);
                String phone = basicInfo.get(2);
                dialog.setContentView(R.layout.popup_purchase);
                ((TextView)dialog.findViewById(R.id.contact_phone)).setText(phone);
                ((TextView)dialog.findViewById(R.id.contact_email)).setText(email);
                dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
                dialog.findViewById(R.id.purchase_okay).setOnClickListener(v -> {
                    new PurchaseTask().execute();
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

    /** Connects to server to store Purchase in DB.
     * Boolean parameter is needed to determine whether user's input is needed*/
    private class PurchaseTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private Socket socket = null;
        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;
        private String resultmsg = "Η παραγγελία σας καταχωρήθηκε.";
        private boolean is_successful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PurchaseActivity.this,
                    "Δημιουργία παραγγελίας...",
                    "Σύνδεση με τον διακομιστή...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(), NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("CREATE_PURCHASE");
                out.writeObject(new Purchase(-1, MainActivity.username, listing_id, new Date(System.currentTimeMillis()).toString()));
                String response = (String) in.readObject();
                if(!response.equals("PURCHASE ADDED")) {
                    is_successful = false;
                    resultmsg = "Προέκυψε σφάλμα.";
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                is_successful = false;
                resultmsg = "Προέκυψε σφάλμα.";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            try {
                if(socket!=null) socket.close();
                if(out!=null) out.close();
                if(in!=null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Snackbar.make(getWindow().getDecorView().getRootView(), resultmsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            if(is_successful) goBack();
        }
    }
}
