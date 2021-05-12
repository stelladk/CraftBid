package com.craftbid.craftbid.model;

import java.io.Serializable;

public class Reward implements Serializable {
    private int price,id;
    private String name,photo,offered_by;

    public Reward(int id, int price, String name, String photo, String offered_by) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.photo = photo;
        this.offered_by = offered_by;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getOffered_by() {
        return offered_by;
    }

    public void setOffered_by(String offered_by) {
        this.offered_by = offered_by;
    }
}
