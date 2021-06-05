package com.craftbid.craftbid;

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
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.craftbid.craftbid.model.Evaluation;
import com.craftbid.craftbid.model.Report;
import com.google.android.material.snackbar.Snackbar;
import com.stelladk.arclib.ArcLayout;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;

public class EvaluationActivity extends AppCompatActivity {
    private String creatorUsername;
    private TextView eval_username, eval_email, eval_phone;
    private ArcLayout eval_photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            creatorUsername = bundle.getString("username");
        }
        eval_username = findViewById(R.id.eval_username);
        eval_email = findViewById(R.id.eval_email);
        eval_phone = findViewById(R.id.eval_phone);
        eval_photo = findViewById(R.id.eval_profile_photo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        new LoadEvaluationTask().execute();
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
        /*
        Intent profile = new Intent(EvaluationActivity.this, CreatorProfile.class);
        profile.putExtra("username", creatorUsername);
        startActivity(profile);
        */
        this.finish();
    }

    public void submitEvaluation(View view) {
        new SubmitEvaluationTask().execute();
    }


    /** Loads Evaluation screen with creator's info */
    private class LoadEvaluationTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String response, resultmsg;
        boolean is_successful = true;
        ArrayList<String> basicInfo;
        byte[] photo;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(EvaluationActivity.this,
                    "Loading creator's information...",
                    "Connecting to server...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("REQUEST_PROFILE");
                out.writeObject(creatorUsername);
                out.writeObject(false);  // only basic info is needed
                basicInfo = (ArrayList<String>) in.readObject();
                photo = (byte[]) in.readObject();

            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
                is_successful = false;
                resultmsg = "Προέκυψε σφάλμα";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            try {
                socket.close();
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(!is_successful) {
                Snackbar.make(getWindow().getDecorView().getRootView(), resultmsg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                goBack();
            }
            // load creator's info
            if(photo!=null) {
                Bitmap view = BitmapFactory.decodeByteArray(photo,0, photo.length);
                Drawable d = new BitmapDrawable(getResources(), view);
                eval_photo.setBackground(d);
            }
            eval_username.setText(creatorUsername);
            eval_email.setText(basicInfo.get(1));
            eval_phone.setText(basicInfo.get(2));
        }
    }


    private class SubmitEvaluationTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String response, resultmsg;
        boolean is_successful;

        @Override
        protected Void doInBackground(Void... voids) {
            // collect user's inputs
            int rating = (int) ((RatingBar) findViewById(R.id.eval_ratingBar)).getRating();
            String date = new Date(System.currentTimeMillis()).toString();
            String comments = ((EditText) findViewById(R.id.eval_comments)).getText().toString();

            //connect to server to send the report
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("CREATE_EVALUATION");

                //create Evaluation object and send it
                Evaluation review = new Evaluation(-1, MainActivity.username, creatorUsername, rating, date, comments);
                out.writeObject(review);
                out.flush();

                response = (String)in.readObject();
                if(response.equals("EVALUATION ADDED")) {
                    resultmsg = "Η δημιουργία της αξιολόγησης ήταν επιτυχής!";
                    is_successful = true;
                }else {
                    resultmsg = "Προέκυψε σφάλμα";
                }
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(EvaluationActivity.this,
                    "Submit Review...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            Snackbar.make( getWindow().getDecorView().getRootView(), resultmsg , Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            try {
                socket.close();
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(is_successful) goBack();
        }
    }
}
