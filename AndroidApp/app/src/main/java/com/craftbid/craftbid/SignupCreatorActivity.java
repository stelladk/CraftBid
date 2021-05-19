package com.craftbid.craftbid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.*;

public class SignupCreatorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String expertise_selected;
    private final int PHOTO_PICK = 1;

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
        //TODO
        //Do actions to confirm sign up
        Intent main = new Intent(SignupCreatorActivity.this, MainActivity.class);
        startActivity(main);
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
    
    public void onImageClick(View view) {
        Snackbar.make(view, "Click! " , Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        Intent photo_picker = new Intent(Intent.ACTION_PICK,
                                         android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photo_picker.setType("image/*");
        startActivityForResult(photo_picker,PHOTO_PICK);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                Uri uri = data.getData();
                InputStream in = getContentResolver().openInputStream(uri);
                Bitmap photo = BitmapFactory.decodeStream(in);
                ImageView photo_profile= (ImageView) findViewById(R.id.photo_profile);
                photo_profile.setImageBitmap(photo);
                //convert photo to byte array to send to server
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
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