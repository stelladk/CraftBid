package com.craftbid.craftbid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class EditListingActivity extends CreateListingActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.edit);

        TextView title = findViewById(R.id.listing_name);
        title.setText(R.string.edit);

        TextView note = findViewById(R.id.note);
        note.setVisibility(View.VISIBLE);
    }
}