package com.craftbid.craftbid.adapters;

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

import com.craftbid.craftbid.NotificationsActivity;
import com.craftbid.craftbid.R;
import com.craftbid.craftbid.model.Notification;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private List<Notification> notifications;

    private NotificationsActivity context = null;
    public NotificationsAdapter(List<Notification> notifications, NotificationsActivity context) {
        this.notifications = notifications;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new NotificationsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.ViewHolder holder, int i) {
        if(context != null){
            Drawable drawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(notifications.get(i).getPhoto(), 0, notifications.get(i).getPhoto().length));
            holder.thumbnail.setBackground(drawable);
        }
        holder.final_price.setText(notifications.get(i).getPrice()+"");
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView thumbnail;
        public TextView notif_text, final_price;
        public Button continue_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            notif_text = itemView.findViewById(R.id.notif_text);
            final_price = itemView.findViewById(R.id.final_price);
            continue_btn = itemView.findViewById(R.id.continue_btn);

            continue_btn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            /*Snackbar.make(view, "Clicked on item " + getAbsoluteAdapterPosition(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*/
            final Notification notification = notifications.get(getAbsoluteAdapterPosition());
            context.purchase(notification);
        }
    }
}
