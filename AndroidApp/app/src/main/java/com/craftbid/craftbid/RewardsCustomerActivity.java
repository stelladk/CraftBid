package com.craftbid.craftbid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.RewardsRecyclerAdapter;
import com.craftbid.craftbid.model.Listing;
import com.craftbid.craftbid.model.Reward;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RewardsCustomerActivity extends AppCompatActivity {
    private String username;
    private ArrayList<Reward> rewards;
    private int reward_points;
    private RewardsRecyclerAdapter adapter;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            username = bundle.getString("username");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Rewards");

        //Set back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView creator = findViewById(R.id.creator_username);
        creator.setText(username);

        byte[] test = new byte[2];
        List<Reward> rewards = new ArrayList<>();
        rewards.add(new Reward(0, 40, "Ξύλινη καρέκλα",  "chair3", test));
        rewards.add(new Reward(1, 70, "Ξύλινη καρέκλα x2", "chair3", test));
        rewards.add(new Reward(2, 120, "Ξύλινη καρέκλα x4", "chair3", test));
        rewards.add(new Reward(3, 140, "Ξύλινη καρέκλα x5", "chair3",  test));

        RecyclerView recycler = findViewById(R.id.rewards_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);
        adapter = new RewardsRecyclerAdapter(rewards, this);
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

    public void openPurchase(int id){
        dialog.setContentView(R.layout.popup_confirm);
        ((TextView)dialog.findViewById(R.id.confirm_message)).setText(R.string.get_reward_confirm);
        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        dialog.show();

        dialog.findViewById(R.id.yes_btn).setOnClickListener(v -> {
            Intent purchase = new Intent(RewardsCustomerActivity.this, PurchaseActivity.class);
            purchase.putExtra("listing_id", id);
            startActivity(purchase);
        });
    }

    public void closePopup(View view) {
        dialog.dismiss();
    }

    private void goBack() {
        Intent back = new Intent(RewardsCustomerActivity.this, CreatorProfile.class);
        back.putExtra("username", username);
        startActivity(back);
    }

    //Temporary
    public int getDrawable(String name) {
        return this.getResources().getIdentifier(name, "drawable", this.getPackageName());
    }

    public String getRewardPointsString(int points){ //for points in adapter
        return getResources().getString(R.string.num_points, points);
    }

    private class ViewRewardsTask extends AsyncTask<Void, Void, Void>{
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Void... voids) {
            Socket socket;
            ObjectOutputStream out;
            ObjectInputStream in;
            try {
//                socket = new Socket("192.168.2.2",6500);
                socket = new Socket("192.168.1.5",6500);
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("VIEW_REWARDS");
                out.writeObject(username); //creator's username
                out.writeObject(false); //i am not the creator of these rewards
                out.flush();

                rewards = (ArrayList<Reward>)in.readObject();

                out.writeObject(MainActivity.username);//Send your username
                out.flush();

                reward_points = (int) in.readObject(); //get available reward_points for this creator

            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(RewardsCustomerActivity.this,
                    "Getting Rewards...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            if(rewards!=null){
                TextView claimed_points = findViewById(R.id.reward_points);
                claimed_points.setText(getResources().getString(R.string.num_claimed_reward_points, reward_points));
                //TODO FIX the points are not showing
                adapter.notifyDataSetChanged();
            }
        }
    }
}