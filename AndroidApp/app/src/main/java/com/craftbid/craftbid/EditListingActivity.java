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
        collectionAdapter = new CollectionRecyclerAdapter(listing_photos, this); //TODO show pictures correctly
        collectionAdapter.setAdd_option(false);
        recycler.setAdapter(collectionAdapter);

        //Listing title is not editable
        findViewById(R.id.title_edit).setVisibility(View.GONE);
        TextView title_noedit = findViewById(R.id.title_noedit);
        title_noedit.setText(listing.getName());
        title_noedit.setVisibility(View.VISIBLE);

        //Show minimum price if there is one
        if(listing.getMin_price() != 0F) {
            ((EditText) findViewById(R.id.init_price_edit)).setText(String.format("%s", listing.getMin_price()));
        }

        //Set Quantity
        ((EditText)findViewById(R.id.quantity_edit)).setText(listing.getQuantity()+"");

        //Set delivery methods
        if(listing.getDelivery().toLowerCase().contains("shipping")){
            findViewById(R.id.shipment_check).setSelected(true);
        }
        if(listing.getDelivery().toLowerCase().contains("hand-in-hand")){
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
        Log.d("EDIT", "openListing: OPENED");
        Intent listing = new Intent(EditListingActivity.this, ListingPrivateActivity.class);
        listing.putExtra("listing_id", listing_id);
        startActivity(listing);
    }

    @Override
    public void onClick(View v) {
        new EditListingTask().execute();
    }

    private class EditListingTask extends AsyncTask<Void, Void, Void>{
        ProgressDialog progressDialog;
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                //TODO change delivery not available
                //TODO make their views not editable

                //Request change of quantity
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                EditText quantity = findViewById(R.id.quantity_edit);
                requestChange(out, "quantity", quantity.getText().toString());
                close();

                //Request change of points
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                EditText points = findViewById(R.id.points_edit);
                requestChange(out, "reward_points", points.getText().toString());
                close();

                //Request change of description
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                EditText desc = findViewById(R.id.description_edit);
                requestChange(out, "description", desc.getText().toString());
                close();

            }catch(IOException e) {
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
            progressDialog.dismiss();
            close();
            goBack();
        }

        private void requestChange(ObjectOutputStream out, String field, String value) throws IOException {
            out.writeObject("UPDATE_LISTING");
            out.writeObject(field);
            out.writeObject(value);
            out.writeObject(listing_id);
            out.flush();
            //TODO not working
            Log.d("EDIT_LISTING", "requestChange: sent "+field);
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