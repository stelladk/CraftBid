package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.craftbid.craftbid.adapters.CollectionRecyclerAdapter;
import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.model.Thumbnail;

import java.util.ArrayList;
import java.util.List;

public class CreateListingActivity extends AppCompatActivity {
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.new_listing);

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Thumbnails RecyclerView
        List<String> collection = new ArrayList<>();
        collection.add("chair1");
        collection.add("chair2");
        collection.add("chair3");

        RecyclerView recycler = findViewById(R.id.collection_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler.setLayoutManager(manager);
        CollectionRecyclerAdapter adapter = new CollectionRecyclerAdapter(collection, this);
        recycler.setAdapter(adapter);

        //TODO get listing categories from database
        Spinner expertise = findViewById(R.id.listing_category);
        ArrayAdapter<CharSequence> exp_adapter = ArrayAdapter.createFromResource(this, R.array.category, android.R.layout.simple_spinner_item);
        exp_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expertise.setAdapter(exp_adapter);

        //TODO get location choices from database
        Spinner location = findViewById(R.id.location_spinner);
        ArrayAdapter<CharSequence> location_adapter = ArrayAdapter.createFromResource(this, R.array.location, android.R.layout.simple_spinner_item);
        location_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        location.setAdapter(location_adapter);

        dialog = new Dialog(this);
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
        Intent creator = new Intent(CreateListingActivity.this, CreatorProfilePrivate.class);
        startActivity(creator);
    }

    //Temporary
    public int getDrawable(String name) {
        return this.getResources().getIdentifier(name, "drawable", this.getPackageName());
    }

    public void showPopup(View view) {
        dialog.setContentView(R.layout.popup_text);
        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        dialog.show();
    }
    public void closePopup(View view) {
        dialog.dismiss();
    }
}