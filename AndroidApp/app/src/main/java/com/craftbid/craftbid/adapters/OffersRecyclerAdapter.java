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

    public OffersRecyclerAdapter(List<Offer> offers) {
        this.offers = offers;
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

            accept_btn.setOnClickListener(this);
            decline_btn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.accept_btn:
                    Snackbar.make(view, "Clicked on accept " + getAbsoluteAdapterPosition(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    break;
                case R.id.decline_btn:
                    Snackbar.make(view, "Clicked on decline " + getAbsoluteAdapterPosition(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    break;
            }
        }
    }
}