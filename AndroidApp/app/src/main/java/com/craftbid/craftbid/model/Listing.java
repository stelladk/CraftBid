package com.craftbid.craftbid.model;

import android.media.Image;

public class Listing {
    private int id;
    private String name, description;
    private int reward_points;
    private float min_price;
    private int photo;

    public Listing(int id, String name, String description, int reward_points, float min_price, int photo) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.reward_points = reward_points;
        this.min_price = min_price;
        this.photo = photo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReward_points() {
        return reward_points;
    }

    public void setReward_points(int reward_points) {
        this.reward_points = reward_points;
    }

    public float getMin_price() {
        return min_price;
    }

    public void setMin_price(float min_price) {
        this.min_price = min_price;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }
}
