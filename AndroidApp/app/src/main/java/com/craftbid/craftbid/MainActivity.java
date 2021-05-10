package com.craftbid.craftbid;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.model.Thumbnail;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public boolean logged_in = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set custom support action bar
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setDisplayShowCustomEnabled(true);
//        getSupportActionBar().setCustomView(R.layout.app_bar);
//        getSupportActionBar().setElevation(0);

//        //Set Back Arrow
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        //Change Toolbar Title
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        toolbar.setTitle("feed");

//        AppBarLayout appBar = findViewById(R.id.appBar);
//        appBar.setExpanded(true);

        List<Thumbnail> thumbnails = new ArrayList<>();
        thumbnails.add(new Thumbnail(0, "Πλεκτή τσάντα", "Ωραιότατη πλεκτή τσάντα πάρε πάρε όλα 5 ευρώ αρχική","Πλεκτά, Τσάντες", "R.drawable.bag", 5));
        thumbnails.add(new Thumbnail(1, "Βραχιόλια", "Βραχιολι χειροποιητο αν θες το παιρνεις", "Κοσμήματα",  "R.drawable.bracelet", 5));
        thumbnails.add(new Thumbnail(2, "Πλεκτά για όλους", "Πλεκτά ρούχα για όλες τις ηλικίε δεχόμαστε παραγγελίες", "Πλεκτά",  "R.drawable.knit", 15));
        thumbnails.add(new Thumbnail(3, "Πασχαλινό κερί χειροποίητο", "Κεριά Πασχαλινά για την Ανάσταση. Χρόνια Πολλά!", "Κεριά", "R.drawable.candle",  20));

        RecyclerView recycler = findViewById(R.id.feed_recyclerview);
//        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        GridLayoutManager manager = new GridLayoutManager(this, 2);
        manager.setOrientation(RecyclerView.VERTICAL);
        recycler.setLayoutManager(manager);
        FeedRecyclerAdapter adapter = new FeedRecyclerAdapter(thumbnails);
        recycler.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbarmenu, menu);
        if(logged_in){
            menu.findItem(R.id.login).setVisible(false);
            menu.findItem(R.id.signup).setVisible(false);
        }else{
            menu.findItem(R.id.logout).setVisible(false);
            menu.findItem(R.id.profile).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.login:
            case R.id.signup:
                logged_in = true;
                break;
            case R.id.logout:
                logged_in = false;
                break;
            case R.id.profile:
                openProfile();
            default:
                return super.onOptionsItemSelected(item);
        }
        invalidateOptionsMenu();
        return true;
    }

    private void openProfile() {
        Intent profile = new Intent(MainActivity.this, CustomerProfile.class);
        startActivity(profile);
    }

}