package com.craftbid.craftbid.model;

import java.io.Serializable;

public class Notification implements Serializable {
    private int listing_id;
    private String belongs_to;
    private float price;
    private byte[] photo;

    public Notification(int listing_id,String belongs_to, float price, byte[] photo) {
        this.listing_id = listing_id;
        this.belongs_to = belongs_to;
        this.price = price;
        this.photo = photo;
    }

    public Notification(int listing_id,String belongs_to, float price) {
        this.listing_id = listing_id;
        this.belongs_to = belongs_to;
        this.price = price;
    }

    public int getListing_id() {
        return listing_id;
    }

    public void setListing_id(int listing_id) {
        this.listing_id = listing_id;
    }

    public String getBelongs_to() {
        return belongs_to;
    }

    public void setBelongs_to(String belongs_to) {
        this.belongs_to = belongs_to;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public byte[] getPhoto() { return this.photo;}

    public void setPhoto(byte[] photo) { this.photo = photo;}
}
