package com.craftbid.craftbid.adapters;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.CreateRewardActivity;
import com.craftbid.craftbid.CreatorProfile;
import com.craftbid.craftbid.MainActivity;
import com.craftbid.craftbid.R;
import com.craftbid.craftbid.RewardsCreatorActivity;
import com.craftbid.craftbid.RewardsCustomerActivity;
import com.craftbid.craftbid.model.Reward;
import com.craftbid.craftbid.model.Thumbnail;
import com.google.android.material.snackbar.Snackbar;
import com.stelladk.arclib.ArcLayout;

import java.util.List;

public class RewardsRecyclerAdapter extends RecyclerView.Adapter<RewardsRecyclerAdapter.ViewHolder>{

    private boolean PRIVATE_VIEW = false;
    private List<Reward> rewards;

    public RewardsRecyclerAdapter(List<Reward> rewards) {
        this.rewards = rewards;
    }

    //Temporary
    private RewardsCustomerActivity contextCustomer = null;
    private RewardsCreatorActivity contextCreator = null;
    public RewardsRecyclerAdapter(List<Reward> rewards, RewardsCustomerActivity context) {
        this.rewards = rewards;
        this.contextCustomer = context;
    }
    public RewardsRecyclerAdapter(List<Reward> rewards, RewardsCreatorActivity context2) {
        this.rewards = rewards;
        this.contextCreator = context2;
    }

    public void setPrivateView(boolean type){
        PRIVATE_VIEW = type;
    }

    @NonNull
    @Override
    public RewardsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reward_item, parent, false);
        return new RewardsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardsRecyclerAdapter.ViewHolder holder, int i) {
        // add button
        if(i == rewards.size()){
            holder.image.setBackgroundResource(0);
            holder.title.setText("Προσθήκη");
            holder.category.setText("");
            holder.description.setText("");
            holder.price.setText("");
            holder.plus_sign.setVisibility(View.VISIBLE);
            holder.points.setText("");
            holder.select_btn.setVisibility(View.INVISIBLE);
            holder.plus_sign.setOnClickListener(v -> {
                Intent add_reward = new Intent(contextCreator, CreateRewardActivity.class);
                contextCreator.startActivity(add_reward);
            });
            return;
        }
//        holder.image.setBackgroundResource(thumbnails.get(position).getThumbnail());
        if(contextCustomer != null){
//            holder.image.setBackgroundResource(contextCustomer.getDrawable(rewards.get(i).getPhoto()));
            Drawable drawable = new BitmapDrawable(contextCustomer.getResources(), BitmapFactory.decodeByteArray(rewards.get(i).getPhoto(), 0, rewards.get(i).getPhoto().length));
            holder.image.setBackground(drawable);
            holder.points.setText(contextCustomer.getRewardPointsString(rewards.get(i).getPrice()));
        }else if(contextCreator != null){
//            holder.image.setBackgroundResource(contextCreator.getDrawable(rewards.get(i).getPhoto()));
            Drawable drawable = new BitmapDrawable(contextCreator.getResources(), BitmapFactory.decodeByteArray(rewards.get(i).getPhoto(), 0, rewards.get(i).getPhoto().length));
            holder.image.setBackground(drawable);
            holder.points.setText(contextCreator.getRewardPointsString(rewards.get(i).getPrice()));
        }else{
            holder.image.setBackgroundResource(R.drawable.chair1);
            holder.points.setText(rewards.get(i).getPrice());
        }
        holder.title.setText(rewards.get(i).getOffered_by());
        holder.description.setText(rewards.get(i).getName());
        holder.category.setText("");
        holder.price.setText("");

        if(PRIVATE_VIEW){
            holder.select_btn.setText("Αφαίρεση");
        }

        holder.select_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PRIVATE_VIEW){
                    contextCreator.removeReward(i);
                }else{
                    contextCustomer.openPurchase(rewards.get(i).getId());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return PRIVATE_VIEW? rewards.size()+1 : rewards.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ArcLayout image;
        public TextView title, category, description, price, points;
        public ImageView plus_sign;
        public Button select_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_item);
            title = itemView.findViewById(R.id.title_item);
            category = itemView.findViewById(R.id.category_item);
            description = itemView.findViewById(R.id.description_item);
            price = itemView.findViewById(R.id.price_item);
            points = itemView.findViewById(R.id.points);
            plus_sign = itemView.findViewById(R.id.plus_sign);
            select_btn = itemView.findViewById(R.id.select_btn);
        }
    }
}
