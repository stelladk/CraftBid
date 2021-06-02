package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CustomerProfilePrivate extends CustomerProfile {

    private static boolean SAVE_MODE=false;
    private String username;
    private final int PHOTO_PICK = 1;

    private EditText fullname_edit, email_edit, description_edit;
    boolean changed_pic = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        username = MainActivity.username; //This is how we get our username

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(MainActivity.username);

        Button edit_btn = findViewById(R.id.edit_btn);
        edit_btn.setVisibility(View.VISIBLE);

        fullname_edit = findViewById(R.id.fullname_edit);
        email_edit = findViewById(R.id.email_edit);
        description_edit = findViewById(R.id.description_edit);

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SAVE_MODE) {
                    onImageClick(v);
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void toggleEditCustomer(View view){
        if(SAVE_MODE){
            saveCustomer(view);
        }else{
            editCustomer(view);
        }
        SAVE_MODE = !SAVE_MODE;
    }

    String fullname_txt;
    String email_txt;
    String description_txt;
    public void editCustomer(View view) {
        Button edit_btn = (Button)view;
        edit_btn.setText(getResources().getString(R.string.save));

        //get old values of fields that can be edited
        fullname_txt = fullname.getText().toString();
        email_txt = email.getText().toString() ;
        description_txt = description.getText().toString();

        fullname_edit.setVisibility(View.VISIBLE);
        email_edit.setVisibility(View.VISIBLE);
        description_edit.setVisibility(View.VISIBLE);

        fullname.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
        description.setVisibility(View.GONE);
    }

    public void saveCustomer(View view) {
        Button edit_btn = (Button)view;
        edit_btn.setText(getResources().getString(R.string.edit));

        //get new values of edit texts to compare with old values from textviews
        //start an asynctask for each change
        String fullname_new = fullname_edit.getText().toString();
        String email_new = email_edit.getText().toString();
        String description_new = description_edit.getText().toString();

        if(!fullname_new.equals(fullname_txt) && !fullname_new.equals("Όνομα")) {
            new ChangeInfoTask().execute("fullname",fullname_new);
            fullname.setText(fullname_new);
        }
        if(!email_new.equals(email_txt) && !email_new.equals("Email")) {
            new ChangeInfoTask().execute("email",email_new);
            email.setText(email_new);
        }
        if(!description_new.equals(description_txt) && !description_new.equals("Περιγραφή Προφίλ")) {
            new ChangeInfoTask().execute("description",description_new);
            description.setText(description_new);
        }
        if(changed_pic) {
            changed_pic = false;
            new ChangeProfilePicTask().execute();
        }

        fullname_edit.setVisibility(View.GONE);
        email_edit.setVisibility(View.GONE);
        description_edit.setVisibility(View.GONE);

        fullname.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
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
            progressDialog = ProgressDialog.show(CustomerProfilePrivate.this,
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
            progressDialog = ProgressDialog.show(CustomerProfilePrivate.this,
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