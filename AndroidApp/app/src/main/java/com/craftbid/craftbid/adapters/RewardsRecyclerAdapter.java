package com.craftbid.craftbid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    private static boolean PRIVATE_VIEW = false;
    private List<Reward> rewards;

    public RewardsRecyclerAdapter(List<Reward> rewards) {
        this.rewards = rewards;
    }

    //Temporary
    private RewardsCustomerActivity context = null;
    private RewardsCreatorActivity context2 = null;
    public RewardsRecyclerAdapter(List<Reward> rewards, RewardsCustomerActivity context) {
        this.rewards = rewards;
        this.context = context;
    }
    public RewardsRecyclerAdapter(List<Reward> rewards, RewardsCreatorActivity context2) {
        this.rewards = rewards;
        this.context2 = context2;
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
        if(i == rewards.size()){
            holder.image.setBackgroundResource(0);
            holder.title.setText("Προσθήκη");
            holder.category.setText("");
            holder.description.setText("");
            holder.price.setText("");
            holder.plus_sign.setVisibility(View.VISIBLE);
            holder.points.setText("");
            holder.select_btn.setVisibility(View.INVISIBLE);
            return;
        }
//        holder.image.setBackgroundResource(thumbnails.get(position).getThumbnail());
        if(context != null){
            holder.image.setBackgroundResource(context.getDrawable(rewards.get(i).getPhoto()));
            holder.points.setText(context.getRewardPointsString(rewards.get(i).getPrice()));
        }else if(context2 != null){
            holder.image.setBackgroundResource(context2.getDrawable(rewards.get(i).getPhoto()));
            holder.points.setText(context2.getRewardPointsString(rewards.get(i).getPrice()));
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

    }

    @Override
    public int getItemCount() {
        return PRIVATE_VIEW? rewards.size()+1 : rewards.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

            select_btn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Snackbar.make(view, "Selected " + getAbsoluteAdapterPosition(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
