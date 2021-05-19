package com.craftbid.craftbid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.*;
import java.net.Socket;

public class SignupCreatorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String expertise_selected;
    private final int PHOTO_PICK = 1;
    byte[] buffer = null;
    boolean is_successful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_creator);

        //TODO get expertise choices from database
        Spinner expertise = findViewById(R.id.expertise_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.expertise, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expertise.setAdapter(adapter);
        expertise.setOnItemSelectedListener(this);

        TextView password = findViewById(R.id.password_label);
        String star = getColoredSpanned("*", String.valueOf(getResources().getColor(R.color.error)));
        String text = getColoredSpanned(getResources().getString(R.string.password_en), "fff");
        password.setText(Html.fromHtml(star+text));
    }

    private String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }

    public void goBack(View view) {
        Intent login = new Intent(SignupCreatorActivity.this, LoginActivity.class);
        startActivity(login);
    }

    public void attemptSighup(View view) {
        //Start AsyncTask for signup
        new SignUpTask().execute("");


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        expertise_selected = adapterView.getItemAtPosition(i).toString();
        Snackbar.make(view, "Selected Expertise " + expertise_selected, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    //click on imageview to get an image from gallery
    public void onImageClick(View view) {
        Intent photo_picker = new Intent(Intent.ACTION_PICK,
                                         android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photo_picker.setType("image/*");
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
                    ImageView photo_profile= (ImageView) findViewById(R.id.photo_profile);
                    photo_profile.setImageBitmap(photo);
                    //convert photo to byte array to send to server
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                    buffer = new byte[1024];
                    int len = 0;
                    while ((len = in.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else {

            }
        }
    }

    //AsyncTask running when signup button is clicked, connecting to server to signup
    private class SignUpTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        String resultmsg = null;
        @Override
        protected Void doInBackground(String... params) {
            Socket socket = null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            String response;
            //get values from input fields
            String name = ((EditText)findViewById(R.id.fullname_edit)).getText().toString();
            String email = ((EditText)findViewById(R.id.email_edit)).getText().toString();
            String phone = ((EditText)findViewById(R.id.phone_edit)).getText().toString();
            boolean freelancer = ((CheckBox)findViewById(R.id.freelancer)).isChecked();
            String category = ((Spinner)findViewById(R.id.expertise_spinner)).getSelectedItem().toString();
            String username = ((EditText)findViewById(R.id.username_edit)).getText().toString();
            String password = ((EditText)findViewById(R.id.password_edit)).getText().toString();
            String repeat_password = ((EditText)findViewById(R.id.repeat_password_edit)).getText().toString();


            //validation checks
            if(name.equals("")|| email.equals("") || username.equals("") || password.equals("") || repeat_password.equals("")) {
                resultmsg = "Συμπληρώστε τα υποχρεωτικά πεδία";
            }else if(!password.equals(repeat_password)) {
                resultmsg = "Οι κωδικοί δεν ταιριάζουν";
            }else {
                is_successful = true;
            }
            /*
            try {
                socket = new Socket("192.168.2.2",6500);
            }catch(IOException e) {
                e.printStackTrace();
            }
             */
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(SignupCreatorActivity.this,
                    "Sign Up...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            Snackbar.make( getWindow().getDecorView().getRootView(), resultmsg , Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            if(is_successful) {
                Intent login = new Intent(SignupCreatorActivity.this, LoginActivity.class);
                startActivity(login);
            }
        }
    }
}