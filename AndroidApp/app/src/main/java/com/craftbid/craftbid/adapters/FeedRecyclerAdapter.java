package com.craftbid.craftbid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.R;
import com.craftbid.craftbid.model.Listing;
import com.google.android.material.snackbar.Snackbar;
import com.stelladk.arclib.ArcLayout;

import org.w3c.dom.Text;

import java.util.List;

public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder> {

    private List<Listing> listings;

    public FeedRecyclerAdapter(List<Listing> listings) {
        this.listings = listings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listing_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == listings.size()){
            holder.image.setBackgroundResource(0);
            holder.title.setText("Δείτε Περισσότερα");
            holder.label.setText("");
            holder.price.setText("");
            holder.plus_sign.setVisibility(View.VISIBLE);
            return;
        }
        holder.image.setBackgroundResource(listings.get(position).getPhoto());
        holder.title.setText(listings.get(position).getName());
        holder.label.setText(listings.get(position).getDescription());
        holder.price.setText(listings.get(position).getMin_price()+"");
    }

    @Override
    public int getItemCount() {
        return listings.size() + 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ArcLayout item;
        public ArcLayout image;
        public TextView title;
        public TextView label;
        public TextView price;
        public ImageView plus_sign;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item);
            image = itemView.findViewById(R.id.img_item);
            title = itemView.findViewById(R.id.title_item);
            label = itemView.findViewById(R.id.label_item);
            price = itemView.findViewById(R.id.price_item);
            plus_sign = itemView.findViewById(R.id.plus_sign);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Snackbar.make(view, "Clicked on item " + getAbsoluteAdapterPosition(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
