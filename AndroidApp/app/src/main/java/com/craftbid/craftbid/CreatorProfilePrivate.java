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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.EvaluationsRecyclerAdapter;
import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.model.Evaluation;
import com.craftbid.craftbid.model.Thumbnail;
import com.google.android.material.button.MaterialButton;
import com.stelladk.arclib.ArcLayout;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreatorProfilePrivate extends CreatorProfile {

    private static boolean SAVE_MODE=false;
    private String username;

    private EditText fullname_edit, email_edit, phone_edit, description_edit;
    private TextView fullname, email, phone, description, freelancer,expertise;
    private CheckBox freelancer_choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        username = MainActivity.username; //This is how we get our username

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(MainActivity.username);
        Log.d("CreatorProfilePrivate", "onCreate: title "+toolbar.getTitle());

        Button edit_btn = findViewById(R.id.edit_btn);
        Button add_listing_btn = findViewById(R.id.add_listing_btn);
        MaterialButton report_btn = findViewById(R.id.report_btn);
        Button review_btn = findViewById(R.id.review_btn);

        edit_btn.setVisibility(View.VISIBLE);
        add_listing_btn.setVisibility(View.VISIBLE);
        report_btn.setVisibility(View.INVISIBLE);
        review_btn.setVisibility(View.INVISIBLE);

        fullname_edit = findViewById(R.id.fullname_edit);
        fullname = findViewById(R.id.fullname);
        email_edit = findViewById(R.id.email_edit);
        email = findViewById(R.id.email);
        phone_edit = findViewById(R.id.phone_edit);
        phone = findViewById(R.id.phone);
        description_edit = findViewById(R.id.description_edit);
        description = findViewById(R.id.description);
        freelancer_choice = findViewById(R.id.freelancer_choice);
        freelancer = findViewById(R.id.freelancer);
        expertise = findViewById(R.id.expertise);

        //Add async task to get profile info from server
        new GetInfoTask().execute();

        findViewById(R.id.rewards_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRewardsCreator(view);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /** Open new screens */
    /** View details of a listing */
    @Override
    public void reviewListing(int listing_id){
        Intent listing_review;
        listing_review = new Intent(CreatorProfilePrivate.this, ListingPrivateActivity.class);
        listing_review.putExtra("listing_id", listing_id);
        listing_review.putExtra("previous", "@profile");
        startActivity(listing_review);
    }

    /** Open the rewards page as viewed by creator */
    public void openRewardsCreator(View view) {
        Intent rewards = new Intent(CreatorProfilePrivate.this, RewardsCreatorActivity.class);
        startActivity(rewards);
    }

    /** Toggle profile info: editable - non editable */
    @Override
    public void toggleEditCreator(View view){
        if(SAVE_MODE){
            saveCreator(view);
        }else{
            editCreator(view);
        }
        SAVE_MODE = !SAVE_MODE;
    }

    public void editCreator(View view) {
        Button edit_btn = (Button)view;
        edit_btn.setText(getResources().getString(R.string.save));

        //TODO get old values of fields that can be edited
        //TODO add asynctask to edit profile info

        fullname_edit.setVisibility(View.VISIBLE);
        email_edit.setVisibility(View.VISIBLE);
        phone_edit.setVisibility(View.VISIBLE);
        description_edit.setVisibility(View.VISIBLE);
        freelancer_choice.setVisibility(View.VISIBLE);

        fullname.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
        phone.setVisibility(View.GONE);
        description.setVisibility(View.GONE);
        freelancer.setVisibility(View.GONE);
    }

    public void saveCreator(View view) {
        Button edit_btn = (Button)view;
        edit_btn.setText(getResources().getString(R.string.edit));

        fullname_edit.setVisibility(View.GONE);
        email_edit.setVisibility(View.GONE);
        phone_edit.setVisibility(View.GONE);
        description_edit.setVisibility(View.GONE);
        freelancer_choice.setVisibility(View.GONE);

        fullname.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);
        phone.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
        freelancer.setVisibility(View.VISIBLE);
    }



    /** AsyncTask running when screen is created, connecting to server to get user info */
    private class GetInfoTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String fullname_txt,email_txt,phone_txt,descr_txt,hasExpertise_txt;
        int isFreelancer;
        byte[] pfp;

        @Override
        protected Void doInBackground(String... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("REQUEST_PROFILE");
                out.writeObject(username);
                out.writeObject(true);
                out.flush();
                //get basic info
                ArrayList<String> info = (ArrayList<String>)in.readObject();
                fullname_txt = info.get(0);
                email_txt = info.get(1);
                phone_txt = info.get(2);
                descr_txt = info.get(3);
                //get profile pic
                pfp = (byte[])in.readObject();
                //get additional creator info
                hasExpertise_txt = (String)in.readObject();
                isFreelancer = (int)in.readObject();
                //get list of evaluations
                evaluations = (ArrayList<Evaluation>)in.readObject();
                //get list of listing thumbnails
                thumbnails = (ArrayList<Thumbnail>)in.readObject();
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.d("here","here");
            progressDialog = ProgressDialog.show(CreatorProfilePrivate.this,
                    "Getting profile info...",
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
            email.setText(email_txt);
            phone.setText(phone_txt);
            description.setText(descr_txt);
            freelancer.setText(isFreelancer==1 ? "Freelancer" : "");
            expertise.setText(hasExpertise_txt);
            //view profile picture
            if(pfp!=null) {
                ArcLayout photo = findViewById(R.id.profile_photo);
                Bitmap pfp_view = BitmapFactory.decodeByteArray(pfp,0, pfp.length);
                Drawable d = new BitmapDrawable(getResources(), pfp_view);
                photo.setBackground(d);
            }
            //change listings and evaluations
            //listings
            adapter = new FeedRecyclerAdapter(thumbnails, CreatorProfilePrivate.this);
            thumbnails_recycler.setAdapter(adapter);
            //evaluations
            adapter2 = new EvaluationsRecyclerAdapter(evaluations, CreatorProfilePrivate.this);
            evaluations_recycler.setAdapter(adapter2);

            progressDialog.dismiss();
        }
    }//load main task
}