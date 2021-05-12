package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.FeedRecyclerAdapter;
import com.craftbid.craftbid.adapters.RewardsRecyclerAdapter;
import com.craftbid.craftbid.model.Reward;
import com.craftbid.craftbid.model.Thumbnail;

import java.util.ArrayList;
import java.util.List;

public class RewardsCustomerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards_customer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Rewards");

        //Set back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView claimed_points = findViewById(R.id.reward_points);
        claimed_points.setText(getResources().getString(R.string.num_claimed_reward_points, 50));

        List<Reward> rewards = new ArrayList<>();
        rewards.add(new Reward(0, 40, "Ξύλινη καρέκλα",  "chair3", "mitsos_creations"));
        rewards.add(new Reward(1, 70, "Ξύλινη καρέκλα x2", "chair3", "mitsos_creations"));
        rewards.add(new Reward(2, 120, "Ξύλινη καρέκλα x4", "chair3", "mitsos_creations"));
        rewards.add(new Reward(3, 140, "Ξύλινη καρέκλα x5", "chair3",  "mitsos_creations"));

        RecyclerView recycler = findViewById(R.id.rewards_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);
        RewardsRecyclerAdapter adapter = new RewardsRecyclerAdapter(rewards, this);
        recycler.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                goBack();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goBack() {
        Intent back = new Intent(RewardsCustomerActivity.this, CreatorProfile.class);
        startActivity(back);
    }

    //Temporary
    public int getDrawable(String name) {
        return this.getResources().getIdentifier(name, "drawable", this.getPackageName());
    }

    public String getRewardPointsString(int points){
        return getResources().getString(R.string.num_points, points);
    }
}