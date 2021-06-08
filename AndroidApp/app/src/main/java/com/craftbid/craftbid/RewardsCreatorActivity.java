package com.craftbid.craftbid;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.adapters.RewardsRecyclerAdapter;
import com.craftbid.craftbid.model.Reward;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class RewardsCreatorActivity extends AppCompatActivity {
    Dialog dialog;
    private ArrayList<Reward> rewards = new ArrayList<>();
    private RecyclerView recycler;
    private RewardsRecyclerAdapter adapter;

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
        TextView claimed_points = findViewById(R.id.reward_points);
        claimed_points.setText("");

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

    public void removeReward(int pos){
        dialog.setContentView(R.layout.popup_confirm);
        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        dialog.show();

        dialog.findViewById(R.id.yes_btn).setOnClickListener(v -> {
            dialog.dismiss();
            new RemoveRewardsTask().execute(pos);
        });
    }
    public void closePopup(View view) {
        dialog.dismiss();
    }

    private void goBack() {
        this.finish();
        /*
        Intent back = new Intent(RewardsCreatorActivity.this, CreatorProfilePrivate.class);
        startActivity(back);
         */
    }

    //Temporary
    public int getDrawable(String name) {
        return this.getResources().getIdentifier(name, "drawable", this.getPackageName());
    }

    public String getRewardPointsString(int points){
        return getResources().getString(R.string.num_points, points);
    }

    private class ViewRewardsTask extends AsyncTask<Void, Void, Void> {
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
                out.writeObject(MainActivity.username); //send my username -> my rewards
                out.writeObject(true); //i am the creator of these rewards
                out.flush();

                rewards = (ArrayList<Reward>)in.readObject();

            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(RewardsCreatorActivity.this,
                    "Getting Rewards...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try{
                out.close();
                in.close();
                socket.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            if(rewards!=null){
                adapter = new RewardsRecyclerAdapter(rewards, RewardsCreatorActivity.this);
                adapter.setPrivateView(true);
                recycler.setAdapter(adapter);
                progressDialog.dismiss();
            }

        }
    }

    private class RemoveRewardsTask extends AsyncTask<Integer, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;
        int pos, id;
        String response;
        boolean success = false;

        @Override
        protected Void doInBackground(Integer... integers) {
            try {
                pos = integers[0];
                id = rewards.get(pos).getId();

                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("REMOVE_REWARD");
                out.writeObject(id); //sent reward id
                out.flush();

                response = (String)in.readObject();
                if(response.equals("REWARD REMOVED")){
                    success = true;
                }

            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(RewardsCreatorActivity.this,
                    "Removing Reward...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try{
                out.close();
                in.close();
                socket.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            if(success){
                rewards.remove(pos);
                adapter.notifyDataSetChanged();
            }else{
                Snackbar.make( getWindow().getDecorView().getRootView(), "Το βραβείο δεν αφαιρέθηκε!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            progressDialog.dismiss();

        }
    }
}