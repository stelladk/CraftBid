package com.craftbid.craftbid.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.craftbid.craftbid.R;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class FullscreenGalleryAdapter extends PagerAdapter {
    private Activity activity;
    private ArrayList<byte[]> images;

    public FullscreenGalleryAdapter(Activity activity, ArrayList<byte[]> images) {
        this.activity = activity;
        this.images = images;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((ConstraintLayout) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup parent, int position) {
        ImageView imageView;

        View viewLayout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fullscreen_gallery_item, parent, false);

        imageView = (ImageView) viewLayout.findViewById(R.id.imageView);

        Bitmap thumbnail_view = BitmapFactory.decodeByteArray(images.get(position),0, images.get(position).length);
        imageView.setImageBitmap(thumbnail_view);

        ((ViewPager) parent).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager) container).removeView((ConstraintLayout) object);
    }
}
