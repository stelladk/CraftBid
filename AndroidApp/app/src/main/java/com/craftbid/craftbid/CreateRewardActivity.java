package com.craftbid.craftbid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.craftbid.craftbid.model.Listing;
import com.craftbid.craftbid.model.Reward;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CreateRewardActivity extends AppCompatActivity {

    private static final int PHOTO_PICK = 1;
    byte[] buffer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reward);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /** Opens gallery to pick a photo for reward */
    public void onImageClick(View view){
        Intent photo_picker = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photo_picker.setType("image/jpeg");
        startActivityForResult(photo_picker,PHOTO_PICK);
    }
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        Log.d("MOUA", "in activityResult");
        if(reqCode == PHOTO_PICK) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    InputStream in = getContentResolver().openInputStream(uri);
                    Bitmap photo = BitmapFactory.decodeStream(in);
                    ImageView photo_profile= (ImageView) findViewById(R.id.reward_photo);
                    photo_profile.setImageBitmap(photo);
                    //convert photo to byte array to send to server
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    buffer = stream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                Log.d("error","no image given");
            }
        }
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
        Intent creator = new Intent(CreateRewardActivity.this, CreatorProfilePrivate.class);
        startActivity(creator);
    }

    public void saveReward(View view) {
        Log.d("saveReward", "saved");
        new CreateRewardTask().execute();
    }

    private class CreateRewardTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String response, resultmsg;
        boolean is_successful = false;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CreateRewardActivity.this,
                    "Create reward...",
                    "Connecting to server...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //get values from input fields
            String reward_name = ((EditText) findViewById(R.id.reward_name)).getText().toString();
            String points_str = ((EditText) findViewById(R.id.set_reward_points)).getText().toString();
            int points = Integer.parseInt((points_str.equals("") ? "0" : points_str));

            if(reward_name.equals("") || points==0){
                resultmsg= "Συμπληρώστε όνομα βραβείου και πόντους!";
                return null;
            }
            if(buffer == null){
                resultmsg = "Πρέπει να προσθέσετε φωτογραφία!";
                return null;
            }
            // Reward object
            Reward reward = new Reward(-1,points, reward_name, MainActivity.username, buffer);

            //connect to server to add reward
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("ADD_REWARD");
                out.writeObject(reward);
                out.writeObject(buffer);

                response = (String)in.readObject();
                if(response.equals("REWARD ADDED")) {
                    resultmsg = "Η προσθήκη του βραβείου ήταν επιτυχής!";
                    is_successful = true;
                }
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
                resultmsg = "Προέκυψε σφάλμα.";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            Snackbar.make( getWindow().getDecorView().getRootView(), resultmsg , Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            try {
                if(socket!=null) socket.close();
                if(out!=null) out.close();
                if(in!=null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(is_successful) goBack();
        }
    }
}
