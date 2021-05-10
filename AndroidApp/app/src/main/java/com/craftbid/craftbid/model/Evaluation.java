package com.craftbid.craftbid.model;

import java.io.Serializable;
import java.util.Date;

public class Evaluation implements Serializable{
    private int id;
    private String submitted_by, reffers_to, comments;
    private int rating;
    private Date date;

    public Evaluation(int id, String submitted_by, String reffers_to, int rating,Date date, String comments) {
        this.id = id;
        this.submitted_by = submitted_by;
        this.reffers_to = reffers_to;
        this.date = date;
        this.rating = rating;
        this.comments = comments;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getSubmitted_by() {
        return submitted_by;
    }

    public void setSubmitted_by(String submitted_by) {
        this.submitted_by = submitted_by;
    }

    public String getReffers_to() {
        return reffers_to;
    }

    public void setReffers_to(String reffers_to) {
        this.reffers_to = reffers_to;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
