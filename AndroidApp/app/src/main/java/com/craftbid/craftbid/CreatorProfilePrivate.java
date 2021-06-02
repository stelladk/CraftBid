package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;


import java.io.*;
import java.net.Socket;

public class CreatorProfilePrivate extends CreatorProfile {

    private static boolean SAVE_MODE=false;
    private String username;
    private final int PHOTO_PICK = 1;

    private EditText fullname_edit, email_edit, phone_edit, description_edit;
    boolean changed_pic = false;

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
        email_edit = findViewById(R.id.email_edit);
        phone_edit = findViewById(R.id.phone_edit);
        description_edit = findViewById(R.id.description_edit);
        freelancer_choice = findViewById(R.id.freelancer_choice);

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SAVE_MODE) {
                    onImageClick(v);
                }
            }
        });

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

    String fullname_txt;
    String email_txt;
    String phone_txt;
    String description_txt;
    String isFreelancer_txt;
    public void editCreator(View view) {
        Button edit_btn = (Button)view;
        edit_btn.setText(getResources().getString(R.string.save));

        //get old values of fields that can be edited
        fullname_txt = fullname.getText().toString();
        email_txt = email.getText().toString() ;
        phone_txt = phone.getText().toString() ;
        description_txt = description.getText().toString();
        isFreelancer_txt = freelancer.getText().toString();

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

        //get new values of edit texts to compare with old values from textviews
        //start an asynctask for each change
        String fullname_new = fullname_edit.getText().toString();
        String email_new = email_edit.getText().toString();
        String phone_new = phone_edit.getText().toString();
        String description_new = description_edit.getText().toString();
        String isFreelancer_new = freelancer_choice.isChecked() == true ? "Freelancer" : "";

        if(phone_new.equals("")) {
            //phone cannot be null for creators
            Snackbar.make(view, "Phone cannot be blank for Creators", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }else {
            if(!fullname_new.equals(fullname_txt) && !fullname_new.equals("Όνομα")) {
                new ChangeInfoTask().execute("fullname",fullname_new);
                fullname.setText(fullname_new);
            }
            if(!email_new.equals(email_txt) && !email_new.equals("Email")) {
                new ChangeInfoTask().execute("email",email_new);
                email.setText(email_new);
            }
            if(!phone_new.equals(phone_txt) && !phone_new.equals("Τηλέφωνο")) {
                new ChangeInfoTask().execute("phoneNumber",phone_new);
                phone.setText(phone_new);
            }
            if(!description_new.equals(description_txt) && !description_new.equals("Περιγραφή Προφίλ")) {
                new ChangeInfoTask().execute("description",description_new);
                description.setText(description_new);
            }
            if(!isFreelancer_new.equals(isFreelancer_txt)) {
                new ChangeInfoTask().execute("isFreelancer", (isFreelancer_new.equals("") ? "0" : "1"));
                freelancer.setText(isFreelancer_new);
            }if(changed_pic) {
                changed_pic = false;
                new ChangeProfilePicTask().execute();
            }
        }

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

    /** click on arc layout image to get an image from gallery */
    public void onImageClick(View view) {
        Intent photo_picker = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photo_picker.setType("image/jpeg");
        startActivityForResult(photo_picker,PHOTO_PICK);
    }
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if(reqCode == PHOTO_PICK) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    InputStream in = getContentResolver().openInputStream(uri);
                    Bitmap photo = BitmapFactory.decodeStream(in);
                    Drawable d = new BitmapDrawable(getResources(), photo);
                    profile_pic.setBackground(d);
                    //convert photo to byte array to send to server
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    pfp = stream.toByteArray();
                    changed_pic = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                Log.d("error","no image given");
            }
        }
    }



    /** AsyncTask running to send a change in profile info */
    private class ChangeInfoTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String field,new_val;
        String reply;

        @Override
        protected Void doInBackground(String... params) {
            try {
                //get field to be changed and new value from parameters
                field = params[0];
                new_val = params[1];

                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("UPDATE_PROFILE");
                out.writeObject(username);
                out.writeObject(field);
                out.writeObject(new_val);
                out.flush();

                reply = (String)in.readObject();
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CreatorProfilePrivate.this,
                    "Updating profile info...",
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
            if(reply.equals("MAIL ALREADY EXISTS!")) {
                //email wasn't change cause given mail already exists
                Snackbar.make( getWindow().getDecorView().getRootView(), "Mail Already Exists", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            progressDialog.dismiss();
        }
    }//change info task


    /** AsyncTask running to send a new profile pic  */
    private class ChangeProfilePicTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String reply;

        @Override
        protected Void doInBackground(String... params) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("CHANGE_PROFILE_PICTURE");
                out.writeObject(username);
                out.writeObject(pfp);
                out.flush();

                reply = (String)in.readObject();
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CreatorProfilePrivate.this,
                    "Updating profile pic...",
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
        }
    }//change profile pic task

}