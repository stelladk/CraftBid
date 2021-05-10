package com.craftbid.craftbid;

import java.io.Serializable;
import java.util.Date;

public class Listing implements Serializable {
    private int id;
    private String name, description,category,published_by,thumbnail,location;
    private float min_price;
    private int reward_points, quantity;
    private Date date;

    public Listing(int id, String name, String description, String category, String published_by,String thumbnail,
                   String location, int reward_points, int quantity, float min_price, Date date) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.published_by = published_by;
        this.thumbnail = thumbnail;
        this.location = location;
        this.reward_points = reward_points;
        this.quantity = quantity;
        this.min_price = min_price;
        this.date = date;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPublished_by() {
        return published_by;
    }

    public void setPublished_by(String published_by) {
        this.published_by = published_by;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
