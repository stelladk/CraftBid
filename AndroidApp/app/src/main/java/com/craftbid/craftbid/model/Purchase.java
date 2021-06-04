package com.craftbid.craftbid.model;

import java.io.Serializable;

public class Purchase implements Serializable {
    private int id, done_on;
    private String done_by,date;

    public Purchase(int id,String done_by, int done_on, String date) {
        this.id = id;
        this.done_by = done_by;
        this.done_on = done_on;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDone_on() {
        return done_on;
    }

    public void setDone_on(int done_on) {
        this.done_on = done_on;
    }

    public String getDone_by() {
        return done_by;
    }

    public void setDone_by(String done_by) {
        this.done_by = done_by;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
