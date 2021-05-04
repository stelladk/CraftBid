package com.craftbid.craftbid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.R;
import com.craftbid.craftbid.model.Evaluation;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class EvaluationsRecyclerAdapter extends RecyclerView.Adapter<EvaluationsRecyclerAdapter.ViewHolder> {
    List<Evaluation> evaluations;

    public EvaluationsRecyclerAdapter(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.evaluation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        holder.image.setImageResource(R.drawable.karen);
        holder.reviewer.setText(evaluations.get(i).getReviewer());
        holder.reviewed.setText(evaluations.get(i).getReviewed());
        holder.comments.setText(evaluations.get(i).getComments());
        holder.rating.setRating(evaluations.get(i).getRating());
    }

    @Override
    public int getItemCount() {
        return evaluations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //        public ConstraintLayout item;
        public ImageView image;
        public TextView reviewer;
        public TextView reviewed;
        public TextView comments;
        public RatingBar rating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            item = itemView.findViewById(R.id.item);
            image = itemView.findViewById(R.id.reviewer_profile);
            reviewer = itemView.findViewById(R.id.reviewer_username);
            reviewed = itemView.findViewById(R.id.reviewed_username);
            comments = itemView.findViewById(R.id.comments);
            rating = itemView.findViewById(R.id.ratingBar);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Snackbar.make(view, "Clicked on item " + getAbsoluteAdapterPosition(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
