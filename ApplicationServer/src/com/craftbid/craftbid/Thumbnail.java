package com.craftbid.craftbid;

import java.io.Serializable;
import java.util.Arrays;

public class Thumbnail implements Serializable{
    private int id;
    private String name, description,category;
    private float min_price;
    byte[] thumbnail;

    public Thumbnail(int id, String name, String description, String category,float min_price,byte[] thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.min_price = min_price;
        this.thumbnail = Arrays.copyOfRange(thumbnail,0,thumbnail.length-1);
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = Arrays.copyOfRange(thumbnail,0,thumbnail.length-1);
    }
}
