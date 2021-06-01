package com.craftbid.craftbid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.craftbid.craftbid.model.Offer;
import com.craftbid.craftbid.model.Report;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    private String username;
    private String creatorUsername;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        username = MainActivity.username;

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            creatorUsername = bundle.getString("username");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* get parameters (profile photo,username,email,phone)
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String creator = bundle.getString("creator");*/
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
        Intent profile = new Intent(ReportActivity.this, CreatorProfile.class);
        profile.putExtra("username", creatorUsername);
        startActivity(profile);
    }

    public void submitReport(View view){
        new SubmitTask().execute();
    }

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
                if(response.equals("CREATE REPORT SUCCESSFUL")) {
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
