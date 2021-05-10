package com.craftbid.craftbid;

import java.io.Serializable;

public class Thumbnail implements Serializable{
    private String name, description,category,thumbnail;
    private float min_price;

    public Thumbnail(String name, String description, String category,String thumbnail,float min_price) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.thumbnail = thumbnail;
        this.min_price = min_price;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

}
