package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.adapters.NotificationsAdapter;
import com.craftbid.craftbid.adapters.OffersRecyclerAdapter;
import com.craftbid.craftbid.model.Notification;
import com.craftbid.craftbid.model.Offer;
import com.craftbid.craftbid.model.Thumbnail;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationsActivity extends AppCompatActivity {
    private NotificationsAdapter adapter;
    private RecyclerView recycler;
    private ArrayList<Notification> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Ειδοποιήσεις");

        //Set Back Arrow
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recycler = findViewById(R.id.notifications_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);
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
        Intent main = new Intent(NotificationsActivity.this, MainActivity.class);
        startActivity(main);
    }

    public void purchase(Notification notification) {
        Intent purchase = new Intent(NotificationsActivity.this, PurchaseActivity.class);
        purchase.putExtra("listing_id", notification.getListing_id());
        startActivity(purchase);
    }

    // TODO waiting for server's correction to be tested
    /** On creation of screen, connects to server to get list of Offers for particular listing*/
    private class LoadNotificationsTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(NotificationsActivity.this,
                    "Getting all notifications...",
                    "Connecting to server...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("REQUEST_NOTIFICATIONS");
                out.writeObject(MainActivity.username);
                // get offers list
                notifications = (ArrayList<Notification>) in.readObject();
                //TODO get server's last response for request_notifications

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
            adapter = new NotificationsAdapter(notifications, NotificationsActivity.this);
            recycler.setAdapter(adapter);
            // TODO show message if empty
        }
    }
}