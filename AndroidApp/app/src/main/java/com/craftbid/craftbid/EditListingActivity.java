package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.CollectionRecyclerAdapter;
import com.craftbid.craftbid.model.Listing;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class EditListingActivity extends CreateListingActivity implements View.OnClickListener {
    private int listing_id;
    //Transfered to CreateListing
//    private Listing listing;
//    private ArrayList<byte[]> listing_photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        if(b!=null){
            listing_id = b.getInt("listing_id");
        }

        listing = ListingPrivateActivity.listing;
        listing_photos = ListingPrivateActivity.listing_photos;

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.edit);

        TextView title = findViewById(R.id.listing_name);
        title.setText(R.string.edit);

        TextView note = findViewById(R.id.note);
        note.setVisibility(View.VISIBLE);

        loadListing();

        findViewById(R.id.save_btn).setOnClickListener(this);
    }

    private void loadListing(){
        //show photos
        RecyclerView recycler = findViewById(R.id.collection_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler.setLayoutManager(manager);
        collectionAdapter = new CollectionRecyclerAdapter(listing_photos, this);
        collectionAdapter.setAdd_option(false);
        recycler.setAdapter(collectionAdapter);

        //Listing title is not editable
        findViewById(R.id.title_edit).setVisibility(View.GONE);
        TextView title_noedit = findViewById(R.id.title_noedit);
        title_noedit.setText(listing.getName());
        title_noedit.setVisibility(View.VISIBLE);

        //Show minimum price if there is one
        findViewById(R.id.init_price_edit).setVisibility(View.GONE);
        TextView init_price_noedit = findViewById(R.id.init_price_noedit);
        init_price_noedit.setText(String.format("%s", listing.getMin_price()));
        init_price_noedit.setVisibility(View.VISIBLE);
//        if(listing.getMin_price() != 0F) {
//            ((EditText) findViewById(R.id.init_price_edit)).setText(String.format("%s", listing.getMin_price()));
//        }

        //Set Quantity
        ((EditText)findViewById(R.id.quantity_edit)).setText(listing.getQuantity()+"");

        //Set delivery methods
        if(listing.getDelivery().toLowerCase().contains("ταχυδρομικά")){
            findViewById(R.id.shipment_check).setSelected(true);
        }
        if(listing.getDelivery().toLowerCase().contains("χέρι")){
            findViewById(R.id.handin_check).setSelected(true);
        }

        //Set points
        if(listing.getReward_points()>0) ((EditText)findViewById(R.id.points_edit)).setText(listing.getReward_points()+"");

        //Set description
        ((EditText)findViewById(R.id.description_edit)).setText(listing.getDescription());
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
        /*
        Intent listing = new Intent(EditListingActivity.this, ListingPrivateActivity.class);
        listing.putExtra("listing_id", listing_id);
        startActivity(listing);
         */
        this.finish();
    }

    @Override
    public void onClick(View v) {
        String location = ((Spinner)findViewById(R.id.location_spinner)).getSelectedItem().toString();
        String category = ((Spinner)findViewById(R.id.listing_category)).getSelectedItem().toString();
        EditText quantity = findViewById(R.id.quantity_edit);
        String points = ((EditText)findViewById(R.id.points_edit)).getText().toString().trim();
        String delivery = ((CheckBox)findViewById(R.id.shipment_check)).isChecked()? "ταχυδρομικά, " : "";
        delivery += ((CheckBox)findViewById(R.id.handin_check)).isChecked()? "χέρι-με-χέρι" : "";
        EditText description = findViewById(R.id.description_edit);

        //Check for errors
        if(quantity.getText().toString().trim().equals("") || quantity.getText().toString().equals("0")){
            quantity.setError("Η ποσότητα δε μπορεί να είναι μηδέν");
            return;
        }
        if(description.getText().toString().trim().equals("")){
            description.setError("Η περιγραφή είναι υποχρεωτική");
            return;
        }
        if(delivery.equals("")){
            Snackbar.make( getWindow().getDecorView().getRootView(), "Επιλέξτε τουλάχιστον ένα τρόπο παράδοσης!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        //Check for changes
        if(!location.equals(listing.getLocation())){
            new EditListingTask().execute("is_located", location);
        }
        if(!category.equals(listing.getCategory())){
            new EditListingTask().execute("category", category);
        }
        if(!delivery.equals(listing.getDelivery())){
            new EditListingTask().execute("delivery", delivery);
        }
        if(!description.getText().toString().trim().equals(listing.getDelivery().trim())){
            new EditListingTask().execute("description", description.getText().toString().trim());
        }
        if(Integer.parseInt(quantity.getText().toString()) != listing.getQuantity()){
            new EditListingTask().execute("quantity", quantity.getText().toString().trim());
        }
        if(!points.equals("") && Integer.parseInt(points) != listing.getReward_points()){
            new EditListingTask().execute("reward_points", points);
        }
    }

    private class EditListingTask extends AsyncTask<String, Void, Void>{
        ProgressDialog progressDialog;
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;
        String field, value;
        String reply;
        boolean success = false;

        @Override
        protected Void doInBackground(String... params) {
            try {
                field = params[0];
                value = params[1];

                //Request change of quantity
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("UPDATE_LISTING");
                out.writeObject(field);
                out.writeObject(value);
                out.writeObject(listing_id);
                out.flush();

                reply = (String)in.readObject();
                if(reply.equals("LISTING UPDATED")){
                    success = true;
                }

            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(EditListingActivity.this,
                    "Edit Listing...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss(); //TODO check why this bullshit is not closing
            close();
            if(success){
                goBack();
            }else{
                Snackbar.make( getWindow().getDecorView().getRootView(), "Το πεδίο "+field+" δεν ανανεώθηκε!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }

        private void close(){
            try {
                if(socket != null){
                    out.close();
                    in.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}