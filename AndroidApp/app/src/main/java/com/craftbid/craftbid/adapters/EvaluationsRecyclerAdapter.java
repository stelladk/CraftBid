package com.craftbid.craftbid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.craftbid.craftbid.CreatorProfile;
import com.craftbid.craftbid.CustomerProfile;
import com.craftbid.craftbid.R;
import com.craftbid.craftbid.model.Evaluation;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class EvaluationsRecyclerAdapter extends RecyclerView.Adapter<EvaluationsRecyclerAdapter.ViewHolder> {
    private List<Evaluation> evaluations;
    private CreatorProfile contextCreator = null;

    public EvaluationsRecyclerAdapter(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public EvaluationsRecyclerAdapter(List<Evaluation> evaluations, CreatorProfile context) {
        this.evaluations = evaluations;
        this.contextCreator = context;
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
        holder.reviewer.setText(evaluations.get(i).getSubmitted_by());
        holder.refered.setText(evaluations.get(i).getReffers_to());
        holder.comments.setText(evaluations.get(i).getComments());
        holder.rating.setRating(evaluations.get(i).getRating());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(contextCreator != null){
                    contextCreator.openProfile(evaluations.get(i).getSubmitted_by());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return evaluations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public ImageView image;
        public TextView reviewer;
        public TextView refered;
        public TextView comments;
        public RatingBar rating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            image = itemView.findViewById(R.id.reviewer_profile);
            reviewer = itemView.findViewById(R.id.reviewer_username);
            refered = itemView.findViewById(R.id.refered_username);
            comments = itemView.findViewById(R.id.comments);
            rating = itemView.findViewById(R.id.ratingBar);
        }
    }
}
