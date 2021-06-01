package com.craftbid.craftbid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.craftbid.craftbid.model.Evaluation;
import com.craftbid.craftbid.model.Report;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;

public class EvaluationActivity extends AppCompatActivity {
    private String creatorUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            creatorUsername = bundle.getString("username");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        Intent profile = new Intent(EvaluationActivity.this, CreatorProfile.class);
        profile.putExtra("username", creatorUsername);
        startActivity(profile);
    }

    public void submitEvaluation(View view) {
        new SubmitEvaluationTask().execute();
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

                is_successful = true; // delete after uncomment
                /* TODO uncomment when server last response is fixed
                response = (String)in.readObject();
                if(response.equals("CREATE EVALUATION SUCCESSFUL")) {
                    resultmsg = "Η δημιουργία της αξιολόγησης ήταν επιτυχής!";
                    is_successful = true;
                }else {
                    resultmsg = "Προέκυψε σφάλμα";
                }*/
            }catch(IOException /*TODO also uncomment | ClassNotFoundException*/ e) {
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
