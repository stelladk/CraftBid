package com.craftbid.craftbid;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.craftbid.craftbid.model.Evaluation;
import com.craftbid.craftbid.model.Listing;
import com.craftbid.craftbid.model.Offer;
import com.craftbid.craftbid.model.Report;
import com.google.android.material.snackbar.Snackbar;
import com.stelladk.arclib.ArcLayout;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ReportActivity extends AppCompatActivity {
    private String creatorUsername;
    private TextView report_username, report_email, report_phone;
    private ArcLayout report_photo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            creatorUsername = bundle.getString("username");
        }
        report_username = findViewById(R.id.report_username);
        report_email = findViewById(R.id.report_email);
        report_phone = findViewById(R.id.report_phone);
        report_photo = findViewById(R.id.report_profile_photo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        new LoadReportTask().execute();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBack();
        }
        return super.onOptionsItemSelected(item);
    }
    private void goBack() {
        /*
        Intent profile = new Intent(ReportActivity.this, CreatorProfile.class);
        profile.putExtra("username", creatorUsername);
        startActivity(profile);
        */
        this.finish();
    }

    public void submitReport(View view){
        new SubmitTask().execute();
    }


    /** Loads Report screen with reported creator's info */
    private class LoadReportTask extends AsyncTask<Void, Void, Void> {
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
            progressDialog = ProgressDialog.show(ReportActivity.this,
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
                report_photo.setBackground(d);
            }
            report_username.setText(creatorUsername);
            report_email.setText(basicInfo.get(1));
            report_phone.setText(basicInfo.get(2));
        }
    }

    /** Connecting to server to submit report to DB */
    private class SubmitTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String response, resultmsg;
        boolean is_successful;

        @Override
        protected Void doInBackground(Void... voids) {
            // collect user's inputs
            String description = ((EditText) findViewById(R.id.report_comments)).getText().toString();
            String date = new Date(System.currentTimeMillis()).toString();
            int reason_id = ((RadioGroup) findViewById(R.id.reasons_group)).getCheckedRadioButtonId();
            String reason;
            if(reason_id==R.id.report_message) reason = "report_message";
            else if(reason_id==R.id.report_no_reality) reason = "report_no_reality";
            else if(reason_id==R.id.report_no_showUp) reason = "report_no_showUp";
            else reason = "report_deceive";

            //connect to server to send the report
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("CREATE_REPORT");

                //Create Report object and send it
                Report report = new Report(-1, MainActivity.username, creatorUsername, reason, description, date);
                out.writeObject(report);
                out.flush();
                response = (String)in.readObject();
                if(response.equals("REPORT ADDED")) {
                    resultmsg = "Η δημιουργία της αναφοράς ήταν επιτυχής!";
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
            progressDialog = ProgressDialog.show(ReportActivity.this,
                    "Submit Report...",
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
