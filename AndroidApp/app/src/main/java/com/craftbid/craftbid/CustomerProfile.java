package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.EvaluationsRecyclerAdapter;
import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.model.Evaluation;
import com.craftbid.craftbid.model.Thumbnail;
import com.stelladk.arclib.ArcLayout;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerProfile extends AppCompatActivity {
    private String username;
    private static String previous;

    protected ArrayList<Evaluation> evaluations;
    protected EditText fullname_edit, email_edit, description_edit;
    protected TextView fullname, email, description;
    protected RecyclerView evaluations_recycler;
    protected EvaluationsRecyclerAdapter adapter;
    protected ArcLayout profile_pic;
    protected byte[] pfp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            username = bundle.getString("username");
            previous = bundle.getString("previous");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(username);

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        evaluations_recycler = findViewById(R.id.reviews_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        evaluations_recycler.setLayoutManager(manager);

        fullname_edit = findViewById(R.id.fullname_edit);
        fullname = findViewById(R.id.fullname);
        email_edit = findViewById(R.id.email_edit);
        email = findViewById(R.id.email);
        description_edit = findViewById(R.id.description_edit);
        description = findViewById(R.id.description);
        profile_pic = findViewById(R.id.profile_photo);

        new GetInfoTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                goBack();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goBack() {
        this.finish();
        /*
        Intent main = new Intent(CustomerProfile.this, MainActivity.class);
        startActivity(main);
         */
    }

    public void toggleEditCustomer(View view){ }



    /** AsyncTask running when screen is created, connecting to server to get user info */
    private class GetInfoTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String fullname_txt,email_txt,phone_txt,descr_txt;

        @Override
        protected Void doInBackground(String... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("REQUEST_PROFILE");
                out.writeObject(username);
                out.writeObject(false); //not a creator
                out.flush();
                //get basic info
                ArrayList<String> info = (ArrayList<String>)in.readObject();
                fullname_txt = info.get(0);
                email_txt = info.get(1);
                phone_txt = info.get(2);
                descr_txt = info.get(3);
                //get profile pic
                pfp = (byte[])in.readObject();
                //get list of evaluations
                evaluations = (ArrayList<Evaluation>)in.readObject();
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CustomerProfile.this,
                    "Getting profile info! ...",
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

            fullname.setText(fullname_txt);
            fullname_edit.setText(fullname_txt);
            email.setText(email_txt);
            email_edit.setText(email_txt);
            description.setText(descr_txt);
            description_edit.setText(descr_txt);

            //view profile picture
            if(pfp!=null) {
                Bitmap pfp_view = BitmapFactory.decodeByteArray(pfp,0, pfp.length);
                Drawable d = new BitmapDrawable(getResources(), pfp_view);
                profile_pic.setBackground(d);
            }
            //change evaluations
            adapter = new EvaluationsRecyclerAdapter(evaluations);
            evaluations_recycler.setAdapter(adapter);

            progressDialog.dismiss();
        }
    }//get info task
}