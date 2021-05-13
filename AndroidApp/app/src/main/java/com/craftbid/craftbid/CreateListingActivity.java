package com.craftbid.craftbid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.craftbid.craftbid.adapters.CollectionRecyclerAdapter;
import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.model.Thumbnail;

import java.util.ArrayList;
import java.util.List;

public class CreateListingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

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
        ArrayAdapter<CharSequence> spinner_adapter = ArrayAdapter.createFromResource(this, R.array.category, android.R.layout.simple_spinner_item);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expertise.setAdapter(spinner_adapter);
    }

    //Temporary
    public int getDrawable(String name) {
        return this.getResources().getIdentifier(name, "drawable", this.getPackageName());
    }
}