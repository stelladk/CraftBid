package com.craftbid.craftbid.model;

import java.io.Serializable;

public class Offer implements Serializable{
    private int id,submitted_for;
    private String submitted_by;
    private float price;

    public Offer(int id, int submitted_for,String submitted_by,float price) {
        this.id = id;
        this.submitted_by = submitted_by;
        this.submitted_for = submitted_for;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubmitted_for() {
        return submitted_for;
    }

    public void setSubmitted_for(int submitted_for) {
        this.submitted_for = submitted_for;
    }

    public String getSubmitted_by() {
        return submitted_by;
    }

    public void setSubmitted_by(String submitted_by) {
        this.submitted_by = submitted_by;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}