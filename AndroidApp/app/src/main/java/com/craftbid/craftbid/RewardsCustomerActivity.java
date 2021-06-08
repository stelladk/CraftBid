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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.RewardsRecyclerAdapter;
import com.craftbid.craftbid.model.Listing;
import com.craftbid.craftbid.model.Reward;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RewardsCustomerActivity extends AppCompatActivity {
    private String username;
    private ArrayList<Reward> rewards = new ArrayList<>();
    private int reward_points;
    private RecyclerView recycler;
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

        recycler = findViewById(R.id.rewards_recyclerview);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);

        new ViewRewardsTask().execute();

        dialog = new Dialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refreshmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                break;
            case R.id.refresh:
                finish();
                startActivity(getIntent());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void handlePurchase(int pos){
        int required_points = rewards.get(pos).getPrice();
        if(reward_points >= required_points) {
            dialog.setContentView(R.layout.popup_confirm);
            ((TextView) dialog.findViewById(R.id.confirm_message)).setText(R.string.get_reward_confirm);
            dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
            dialog.show();

            dialog.findViewById(R.id.yes_btn).setOnClickListener(v -> {
                new PurchaseRewardTask().execute(required_points);
                reward_points -= required_points;
                TextView claimed_points = findViewById(R.id.reward_points);
                claimed_points.setText(getResources().getString(R.string.num_claimed_reward_points, reward_points));
                dialog.dismiss();
            });
        }else{
            Snackbar.make( getWindow().getDecorView().getRootView(), "Δεν εχετε αρκετούς πόντους για αυτό το βραβείο!" , Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public void closePopup(View view) {
        dialog.dismiss();
    }

    private void goBack() {
        this.finish();
        /*Intent back = new Intent(RewardsCustomerActivity.this, CreatorProfile.class);
        back.putExtra("username", username);
        startActivity(back);*/
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
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
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
            try{
                out.close();
                in.close();
                socket.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            if(rewards!=null){
                TextView claimed_points = findViewById(R.id.reward_points);
                claimed_points.setText(getResources().getString(R.string.num_claimed_reward_points, reward_points));
                adapter = new RewardsRecyclerAdapter(rewards, RewardsCustomerActivity.this);
                recycler.setAdapter(adapter);
            }
        }
    }

    private class PurchaseRewardTask extends AsyncTask<Integer, Void, Void>{
        ProgressDialog progressDialog;
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;
        int points;
        String response;
        boolean success;

        @Override
        protected Void doInBackground(Integer... integers) {
            try {
                points = integers[0];

                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("GET_REWARD");
                out.writeObject(MainActivity.username); //send customer's username
                out.writeObject(username); //send creator's username
                out.writeObject(points); //send required points
                out.flush();

                response = (String)in.readObject();
                success = response.equals("REWARD BOUGHT SUCCESSFULLY");
            } catch (IOException | ClassNotFoundException e) {
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
            try{
                out.close();
                in.close();
                socket.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            String msg = "Προέκυψε κάποιο σφάλμα!";
            if(success){
                msg = "Η αγορά ολοκληρώθηκε! Θα επικοινωνήσει ο δημιουργός μαζί σας!";
            }
            Snackbar.make( getWindow().getDecorView().getRootView(), msg , Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}