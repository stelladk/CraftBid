package com.craftbid.craftbid.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.CreateListingActivity;
import com.craftbid.craftbid.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class CollectionRecyclerAdapter extends RecyclerView.Adapter<CollectionRecyclerAdapter.ViewHolder> {

    ArrayList<byte[]> collection;
    private boolean add_option = true;

    private CreateListingActivity context = null;
    public CollectionRecyclerAdapter(ArrayList<byte[]> collection, CreateListingActivity context) {
        this.collection = collection;
        this.context = context;
    }

    @NonNull
    @Override
    public CollectionRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_item, parent, false);
        return new CollectionRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionRecyclerAdapter.ViewHolder holder, int i) {
        if(i == collection.size()){
            holder.image.setImageResource(0);
            holder.plus_sign.setVisibility(View.VISIBLE);
            holder.textAdd.setVisibility(View.VISIBLE);
            holder.plus_sign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.addImage();
                }
            });
            return;
        }
        holder.plus_sign.setVisibility(View.GONE);
        holder.textAdd.setVisibility(View.GONE);
        if(context != null){
            Bitmap thumbnail = BitmapFactory.decodeByteArray(collection.get(i),0, collection.get(i).length);
            holder.image.setImageBitmap(thumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return add_option? collection.size() + 1 : collection.size();
    }

    public void setAdd_option(boolean add_option){
        this.add_option = add_option;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView image, plus_sign;
        public TextView textAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageView);
            plus_sign = itemView.findViewById(R.id.plus_sign);
            textAdd = itemView.findViewById(R.id.textAdd);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Snackbar.make(view, "Clicked on item " + getAbsoluteAdapterPosition(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
