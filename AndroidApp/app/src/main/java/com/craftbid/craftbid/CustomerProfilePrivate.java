package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class CustomerProfilePrivate extends CustomerProfile {

    private static boolean SAVE_MODE=false;

    //private EditText fullname_edit, email_edit, description_edit;
    //private TextView fullname, email, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button edit_btn = findViewById(R.id.edit_btn);
        edit_btn.setVisibility(View.VISIBLE);

        fullname_edit = findViewById(R.id.fullname_edit);
        fullname = findViewById(R.id.fullname);
        email_edit = findViewById(R.id.email_edit);
        email = findViewById(R.id.email);
        description_edit = findViewById(R.id.description_edit);
        description = findViewById(R.id.description);
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

    public void editCustomer(View view) {
        Button edit_btn = (Button)view;
        edit_btn.setText(getResources().getString(R.string.save));

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

        fullname_edit.setVisibility(View.GONE);
        email_edit.setVisibility(View.GONE);
        description_edit.setVisibility(View.GONE);

        fullname.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
    }
}