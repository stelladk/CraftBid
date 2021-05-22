package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.RewardsRecyclerAdapter;
import com.craftbid.craftbid.model.Reward;

import java.util.ArrayList;
import java.util.List;

public class RewardsCreatorActivity extends AppCompatActivity {
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Rewards");

        //Set back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView title = findViewById(R.id.rewards_title);
        title.setText(getResources().getString(R.string.my_rewards));
//        TextView creator_username = findViewById(R.id.creator_username);
        TextView claimed_points = findViewById(R.id.reward_points);
        claimed_points.setText("");

        List<Reward> rewards = new ArrayList<>();
        rewards.add(new Reward(0, 40, "Ξύλινη καρέκλα",  "chair3", "mitsos_creations"));
        rewards.add(new Reward(1, 70, "Ξύλινη καρέκλα x2", "chair3", "mitsos_creations"));
        rewards.add(new Reward(2, 120, "Ξύλινη καρέκλα x4", "chair3", "mitsos_creations"));
        rewards.add(new Reward(3, 140, "Ξύλινη καρέκλα x5", "chair3",  "mitsos_creations"));

        RecyclerView recycler = findViewById(R.id.rewards_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);
        RewardsRecyclerAdapter adapter = new RewardsRecyclerAdapter(rewards, this);
        adapter.setPrivateView(true);
        recycler.setAdapter(adapter);

        dialog = new Dialog(this);
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

    public void removeReward(int id){
        dialog.setContentView(R.layout.popup_confirm);
        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        dialog.show();

        dialog.findViewById(R.id.yes_btn).setOnClickListener(v -> {
            // TODO proceed to removing the reward
            Log.d("reward","reward removed");
        });
    }
    public void closePopup(View view) {
        dialog.dismiss();
    }

    private void goBack() {
        Intent back = new Intent(RewardsCreatorActivity.this, CreatorProfilePrivate.class);
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