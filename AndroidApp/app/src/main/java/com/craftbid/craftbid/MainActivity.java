package com.craftbid.craftbid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.model.Thumbnail;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener, AdapterView.OnItemSelectedListener {

    public static boolean logged_in = false;
    private List<Thumbnail> thumbnails;
    private String search_text;
    private RecyclerView recycler;
    private FeedRecyclerAdapter adapter;
    public static String username;
    public static boolean creator;
    public static final String GUEST = "@guest";
    public static final String MAIN = "@main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get content from previous screen
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            username = bundle.getString("username", username);
            creator = bundle.getBoolean("creator", creator);
            if(!username.equals(GUEST)) logged_in = true;
        }
        Log.d("MAIN", "onCreate: username "+username);
        Log.d("MAIN", "onCreate: creator "+creator);
        //change Toolbar title
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        recycler = findViewById(R.id.feed_recyclerview);
        GridLayoutManager manager = new GridLayoutManager(this, 2);
        manager.setOrientation(RecyclerView.VERTICAL);
        recycler.setLayoutManager(manager);
        new LoadMainScreenTask().execute(); //execute AsyncTask to get list of listings
    }

    /** Create options menu on app bar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbarmenu, menu);
        if(logged_in){
            menu.findItem(R.id.login).setVisible(false);
        }else{
            menu.findItem(R.id.notif).setVisible(false);
            menu.findItem(R.id.logout).setVisible(false);
            menu.findItem(R.id.profile).setVisible(false);
        }
        return true;
    }

    /** App bar options listeners */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.login:
            case R.id.logout:
                logged_in = false;
                Intent login = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(login);
                break;
            case R.id.profile:
                openPrivateProfile();
                break;
            case R.id.notif:
                openNotifications();
                break;
            case R.id.refresh:
                finish();
                startActivity(getIntent());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        invalidateOptionsMenu();
        return true;
    }

    /** Sort list of thumbnails by price, name or category */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        ArrayList<Thumbnail> sorted = new ArrayList<>(thumbnails);
        if(position!=0) {
            //sort items
            Collections.sort(sorted, new Comparator<Thumbnail>() {
                @Override
                public int compare(Thumbnail t1, Thumbnail t2) {
                    if(position == 1){ //Price
                        return Float.compare(t1.getMin_price(), t2.getMin_price());
                    }else if(position == 2) { //Name
                        return t1.getName().compareTo(t2.getName());
                    }else if(position == 3){ //Category
                        return t1.getCategory().compareTo(t2.getCategory());
                    }
                    return 0;
                }
            });
        }
        adapter = new FeedRecyclerAdapter(sorted, MainActivity.this);
        recycler.setAdapter(adapter);
        //adapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }

    /** Listener for search bar */
    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            search_text = view.getText().toString();
            new SearchTask().execute();
            view.clearFocus();
            recycler.requestFocus();
            hideKeyboard(this);
            handled = true;
        }
        return handled;
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /** Open new screens */
    /** Open user's private profile */
    private void openPrivateProfile() {
        Intent profile;
        if(creator){
            profile = new Intent(MainActivity.this, CreatorProfilePrivate.class);
        }else{
            profile = new Intent(MainActivity.this, CustomerProfilePrivate.class);
        }
        profile.putExtra("username",username);
        profile.putExtra("previous", MAIN);
        startActivity(profile);
    }

    /** Open the notifications panel */
    private void openNotifications(){
        Intent notif = new Intent(MainActivity.this, NotificationsActivity.class);
        notif.putExtra("username",username); //the username of current user to view their notifs
        startActivity(notif);
    }

    /** Open the listing details */
    public void reviewListing(int listing_id,String published_by){
        //set PRIVATE true/false based on whether user is the creator of the listing
        boolean PRIVATE = false;
        if(published_by.equals(username)) PRIVATE = true;
        Intent listing_review;
        if(PRIVATE) listing_review = new Intent(MainActivity.this, ListingPrivateActivity.class);
        else listing_review = new Intent(MainActivity.this, ListingPublicActivity.class);
        listing_review.putExtra("listing_id", listing_id);

        if(logged_in) listing_review.putExtra("previous", MAIN);
        else listing_review.putExtra("previous", GUEST);
        startActivity(listing_review);
    }

    //a getter for the list of thumbnails
    public List<Thumbnail> getThumbnails() {
        return thumbnails;
    }



    /** AsyncTask running when screen is created, connecting to server to get list of Listing Thumbnails*/
    private class LoadMainScreenTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        @Override
        protected Void doInBackground(String... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("LOAD_MAIN_SCREEN");
                thumbnails = (ArrayList<Thumbnail>) in.readObject(); //get list of thumbnails

            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.d("here","here");
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Getting most recent Listings...",
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
            adapter = new FeedRecyclerAdapter(thumbnails, MainActivity.this);
            recycler.setAdapter(adapter);

            Spinner sort = findViewById(R.id.sort_spinner);
            ArrayAdapter<CharSequence> spinner_adapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.sort_options, android.R.layout.simple_spinner_item);
            spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sort.setAdapter(spinner_adapter);
            sort.setOnItemSelectedListener(MainActivity.this);

            EditText searchBar = findViewById(R.id.searchBar);
            searchBar.setOnEditorActionListener(MainActivity.this);
        }
    }//load main task


    /** AsyncTask running when search bar is submitted, showing search results */
    private class SearchTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        @Override
        protected Void doInBackground(String... params) {

            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("SEARCH");
                out.writeObject(false);
                out.writeObject(search_text);
                thumbnails = (ArrayList<Thumbnail>) in.readObject(); //get list of thumbnails

            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.d("here","here");
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Getting search results...",
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
            for(Thumbnail t : thumbnails) Log.d("Prints",t.getName());
            adapter = new FeedRecyclerAdapter(thumbnails, MainActivity.this);
            recycler.setAdapter(adapter);
        }
    }//search task


}