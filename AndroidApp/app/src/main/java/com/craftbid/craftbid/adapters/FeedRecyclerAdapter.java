package com.craftbid.craftbid.adapters;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.CreatorProfile;
import com.craftbid.craftbid.MainActivity;
import com.craftbid.craftbid.R;
import com.craftbid.craftbid.model.Thumbnail;
import com.stelladk.arclib.ArcLayout;

import java.util.List;

public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder> {

    private List<Thumbnail> thumbnails;

    private MainActivity context = null;
    private CreatorProfile context2 = null;
    public FeedRecyclerAdapter(List<Thumbnail> thumbnails, MainActivity context) {
        this.thumbnails = thumbnails;
        this.context = context;
    }
    public FeedRecyclerAdapter(List<Thumbnail> thumbnails, CreatorProfile context) {
        this.thumbnails = thumbnails;
        this.context2 = context;
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
        holder.plus_sign.setVisibility(View.GONE);
        if(context != null){
            Drawable drawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(thumbnails.get(position).getThumbnail(), 0, thumbnails.get(position).getThumbnail().length));
            holder.image.setBackground(drawable);
        }else if(context2!=null){
            Drawable drawable = new BitmapDrawable(context2.getResources(), BitmapFactory.decodeByteArray(thumbnails.get(position).getThumbnail(), 0, thumbnails.get(position).getThumbnail().length));
            holder.image.setBackground(drawable);
        }
        holder.title.setText(thumbnails.get(position).getName());
        holder.category.setText(thumbnails.get(position).getCategory());
        holder.description.setText(thumbnails.get(position).getDescription());
        holder.price.setText(thumbnails.get(position).getMin_price()+"");

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context != null){
                    context.reviewListing(thumbnails.get(position).getId(),thumbnails.get(position).getPublished_by());
                }else if(context2 != null){
                    context2.reviewListing(thumbnails.get(position).getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return thumbnails.size();
    }

    //Filter view according to search results
    public void filter(List<Thumbnail> filteredList){
        thumbnails = filteredList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
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
        }
    }
    public void setThumbnails(List<Thumbnail> thumbnails){
        this.thumbnails = thumbnails;
    }

    public List<Thumbnail> getThumbnails() {
        return thumbnails;
    }
}
