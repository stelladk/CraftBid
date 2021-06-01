package com.craftbid.craftbid;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.adapters.CollectionRecyclerAdapter;
import com.craftbid.craftbid.model.Listing;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CreateListingActivity extends AppCompatActivity {
    private final int PHOTO_PICK = 1;
    private Dialog dialog;
    private ArrayList<byte[]> collection;
    CollectionRecyclerAdapter collectionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.new_listing);

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreateListing(view);
            }
        });

        new LoadSpinnersTask().execute();

        //Thumbnails RecyclerView
        collection = new ArrayList<>();
//        collection.add("chair1");
//        collection.add("chair2");
//        collection.add("chair3");

        RecyclerView recycler = findViewById(R.id.collection_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler.setLayoutManager(manager);
        collectionAdapter = new CollectionRecyclerAdapter(collection, this); //TODO show pictures correctly
        recycler.setAdapter(collectionAdapter);

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
        Intent creator = new Intent(CreateListingActivity.this, CreatorProfilePrivate.class);
        startActivity(creator);
    }

    //Temporary
    public int getDrawable(String name) {
        return this.getResources().getIdentifier(name, "drawable", this.getPackageName());
    }

    public void showPopup(View view) {
        dialog.setContentView(R.layout.popup_text);
        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        dialog.show();
    }
    public void closePopup(View view) {
        dialog.dismiss();
    }

    public void addImage(){
        Intent photo_picker = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photo_picker.setType("image/jpeg");
        startActivityForResult(photo_picker,PHOTO_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHOTO_PICK) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    InputStream in = getContentResolver().openInputStream(uri);
                    Bitmap photo = BitmapFactory.decodeStream(in);
                    //convert photo to byte array to send to server
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    collection.add(stream.toByteArray());
                    collectionAdapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                Log.d("error","no image given");
            }
        }
    }

    public void attemptCreateListing(View view) {
        new CreateListingTask().execute();
    }


    /** AsyncTask running when screen is created, connecting to server to get list of Expertises */
    private class LoadSpinnersTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        ArrayList<String> categories = null;
        ArrayList<String> locations = null;

        @Override
        protected Void doInBackground(String... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("REQUEST_CATEGORIES");
                categories = (ArrayList<String>) in.readObject();
                out.writeObject("REQUEST_LOCATIONS");
                locations = (ArrayList<String>) in.readObject();

            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CreateListingActivity.this,
                    "Getting Lists...",
                    "Connecting to server...");
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

            //set content of spinner of category
            Spinner category = findViewById(R.id.listing_category);
            ArrayAdapter<String> category_adapter = new ArrayAdapter<>(CreateListingActivity.this, android.R.layout.simple_spinner_item, categories);
            category_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            category.setAdapter(category_adapter);

            //set content of spinner of location
            Spinner location = findViewById(R.id.location_spinner);
            ArrayAdapter<String> location_adapter = new ArrayAdapter<>(CreateListingActivity.this, android.R.layout.simple_spinner_item, locations);
            location_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            location.setAdapter(location_adapter);
        }
    }

    private class CreateListingTask extends AsyncTask<Void, Void, Void>{
        ProgressDialog progressDialog;
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;
        String msg;
        boolean success = false;

        @Override
        protected Void doInBackground(Void... voids) {
            //get values from input fields
            //TODO check if they are empty
            String title = ((EditText)findViewById(R.id.title_edit)).getText().toString();
            String description = ((EditText)findViewById(R.id.description_edit)).getText().toString();
            String category = ((Spinner)findViewById(R.id.listing_category)).getSelectedItem().toString();
            String location = ((Spinner)findViewById(R.id.location_spinner)).getSelectedItem().toString();
            int points = getEditTextValue(findViewById(R.id.points_edit), 0);
            int quantity = getEditTextValue(findViewById(R.id.quantity_edit), 1);
            float min_price = getEditTextValue(findViewById(R.id.init_price_edit), 0.0F);
            String date = String.valueOf(new Date()); //TODO take date format from database
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date formattedDate;
            try {
                formattedDate = inputFormat.parse(date);
                date = outputFormat.format(formattedDate);
                Log.d("CREATE LISTING", "doInBackground: DATE "+date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            boolean shipment = ((CheckBox)findViewById(R.id.shipment_check)).isChecked();
            boolean handin = ((CheckBox)findViewById(R.id.handin_check)).isChecked();
            String delivery = (shipment? "Ταχυδρομικά":"") + ", " + (handin? "Χέρι-με-χέρι":"");

            if(title.equals("") || description.equals("")){
                msg = "Συμπληρώστε τίτλο και περιγραφή!";
                return null;
            }
            if(delivery.equals(", ")){
                msg = "Επιλέξτε τουλάχιστον ένα τρόπο παράδοσης!";
                return null;
            }
            if(collection.size() < 1){
                msg = "Πρέπει να προσθέσεις τουλάχιστον μία φωτογραφία!";
                return null;
            }

            Listing newListing = new Listing(-1, title, description, category, MainActivity.username,
                    location, points, quantity, min_price, date, delivery, collection.size());

            //TODO check if inputs are empty
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("CREATE_LISTING");
                out.writeObject(newListing);
                out.flush();

                for(byte[] img : collection){
                    out.writeObject(img);
                    out.flush();
                }

                String response = (String)in.readObject();
                if (response.equals("LISTING CREATION SUCCESSFUL")) {
                    success = true;
                }
                else {
                    msg = "Η αγγελία δεν δημιουργήθηκε!";
                    success = false;
                }
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CreateListingActivity.this,
                    "Create Listing...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            try {
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(success){
                goBack();
            }else{
                Snackbar.make( getWindow().getDecorView().getRootView(), msg , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }

        private int getEditTextValue(EditText editText, int deflt){
            String value = editText.getText().toString();
            return value.equals("")? deflt : Integer.parseInt(value);
        }

        private float getEditTextValue(EditText editText, float deflt){
            String value = editText.getText().toString();
            return value.equals("")? deflt : Integer.parseInt(value);
        }
    }
}