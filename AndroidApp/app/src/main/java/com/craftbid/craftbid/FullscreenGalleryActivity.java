package com.craftbid.craftbid;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.craftbid.craftbid.adapters.FullscreenGalleryAdapter;

import java.util.ArrayList;

public class FullscreenGalleryActivity extends AppCompatActivity {
    private ArrayList<byte[]> images;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen_gallery);

        if(ListingPublicActivity.listing_photos != null){
            images = ListingPublicActivity.listing_photos;
            name = ListingPublicActivity.listing.getName();
        }else {
            images = new ArrayList<>();
            name = "";
        }

        ViewPager pager = findViewById(R.id.viewPager);
        FullscreenGalleryAdapter adapter = new FullscreenGalleryAdapter(images);
        pager.setAdapter(adapter);

        TextView title = findViewById(R.id.title);
        title.setText(name);

        // close button click event
        ImageButton close_btn = findViewById(R.id.close_btn);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

}