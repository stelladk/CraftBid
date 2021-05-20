package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.adapters.NotificationsAdapter;
import com.craftbid.craftbid.model.Thumbnail;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        username = MainActivity.username;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Ειδοποιήσεις");

        //Set Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        List<Thumbnail> notifications = new ArrayList<>();
        notifications.add(new Thumbnail(0, "Πλεκτή τσάντα", "Ωραιότατη πλεκτή τσάντα πάρε πάρε όλα 5 ευρώ αρχική","Πλεκτά, Τσάντες", "bag", 18));
        notifications.add(new Thumbnail(1, "Βραχιόλια", "Βραχιολι χειροποιητο αν θες το παιρνεις", "Κοσμήματα",  "bracelet", 5));
        notifications.add(new Thumbnail(2, "Πλεκτά για όλους", "Πλεκτά ρούχα για όλες τις ηλικίε δεχόμαστε παραγγελίες", "Πλεκτά",  "knit", 15));
        notifications.add(new Thumbnail(3, "Πασχαλινό κερί χειροποίητο", "Κεριά Πασχαλινά για την Ανάσταση. Χρόνια Πολλά!", "Κεριά", "candle",  20));
        notifications.add(new Thumbnail(4, "Ξύλινη Καρέκλα", "Ωραιότατη Ξύλινη Καρέκλα", "Καρέκλες", "chair1", 15));
        notifications.add(new Thumbnail(5, "Ξύλινη Καρέκλα", "Ξύλινη Καρέκλα Κήπου", "Καρέκλες", "chair3", 20));
        notifications.add(new Thumbnail(6, "Πλεκτή τσάντα", "Ωραιότατη πλεκτή τσάντα πάρε πάρε όλα 5 ευρώ αρχική","Πλεκτά, Τσάντες", "bag", 12));
        notifications.add(new Thumbnail(7, "Πασχαλινό κερί χειροποίητο", "Κεριά Πασχαλινά για την Ανάσταση. Χρόνια Πολλά!", "Κεριά", "candle",  7));

        RecyclerView recycler = findViewById(R.id.notifications_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);
        NotificationsAdapter adapter = new NotificationsAdapter(notifications, this);
        recycler.setAdapter(adapter);
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
        Intent main = new Intent(NotificationsActivity.this, MainActivity.class);
        startActivity(main);
    }

    //Temporary
    public int getDrawable(String name) {
        return this.getResources().getIdentifier(name, "drawable", this.getPackageName());
    }

    public void purchase(Thumbnail notification) {
        Intent purchase = new Intent(NotificationsActivity.this, PurchaseActivity.class);
        purchase.putExtra("listing_id", notification.getId());
        startActivity(purchase);
    }
}