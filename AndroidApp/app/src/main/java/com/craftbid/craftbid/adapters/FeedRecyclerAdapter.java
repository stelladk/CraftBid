package com.craftbid.craftbid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.R;
import com.craftbid.craftbid.model.Thumbnail;
import com.google.android.material.snackbar.Snackbar;
import com.stelladk.arclib.ArcLayout;

import java.util.List;

public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder> {

    private List<Thumbnail> thumbnails;

    public FeedRecyclerAdapter(List<Thumbnail> thumbnails) {
        this.thumbnails = thumbnails;
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
        if(position == thumbnails.size()){
            holder.image.setBackgroundResource(0);
            holder.title.setText("Δείτε Περισσότερα");
            holder.category.setText("");
            holder.price.setText("");
            holder.plus_sign.setVisibility(View.VISIBLE);
            return;
        }
//        holder.image.setBackgroundResource(thumbnails.get(position).getThumbnail());
        holder.image.setBackgroundResource(R.drawable.bag);
        holder.title.setText(thumbnails.get(position).getName());
        holder.category.setText(thumbnails.get(position).getCategory());
        holder.description.setText(thumbnails.get(position).getDescription());
        holder.price.setText(thumbnails.get(position).getMin_price()+"");
    }

    @Override
    public int getItemCount() {
        return thumbnails.size() + 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ArcLayout item;
        public ArcLayout image;
        public TextView title, category, description, price;
        public ImageView plus_sign;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item);
            image = itemView.findViewById(R.id.img_item);
            title = itemView.findViewById(R.id.title_item);
            category = itemView.findViewById(R.id.category_item);
            description = itemView.findViewById(R.id.description_item);
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
