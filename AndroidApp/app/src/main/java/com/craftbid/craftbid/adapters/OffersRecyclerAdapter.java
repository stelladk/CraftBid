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
import com.craftbid.craftbid.OffersActivity;
import com.craftbid.craftbid.R;
import com.craftbid.craftbid.model.Offer;
import com.craftbid.craftbid.model.Thumbnail;
import com.google.android.material.snackbar.Snackbar;
import com.stelladk.arclib.ArcLayout;

import java.util.List;

public class OffersRecyclerAdapter extends RecyclerView.Adapter<OffersRecyclerAdapter.ViewHolder> {

    private List<Offer> offers;
    private OffersActivity context;

    public OffersRecyclerAdapter(List<Offer> offers, OffersActivity context) {
        this.offers = offers;
        this.context = context;
    }

    @NonNull
    @Override
    public OffersRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.offer_item, parent, false);
        return new OffersRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OffersRecyclerAdapter.ViewHolder holder, int i) {
        holder.profile.setBackgroundResource(R.drawable.karen);

        holder.username.setText(offers.get(i).getSubmitted_by());
        holder.offer.setText(offers.get(i).getPrice()+"");

        holder.accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.acceptOffer(offers.get(i).getId());
            }
        });
        holder.decline_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.declineOffer(offers.get(i).getId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView profile;
        public TextView username, offer;
        public Button accept_btn, decline_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.user_profile);
            username = itemView.findViewById(R.id.username);
            offer = itemView.findViewById(R.id.offer);
            accept_btn = itemView.findViewById(R.id.accept_btn);
            decline_btn = itemView.findViewById(R.id.decline_btn);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            context.openProfile();
        }
    }
}