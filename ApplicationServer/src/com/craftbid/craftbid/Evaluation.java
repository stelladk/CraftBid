package com.craftbid.craftbid;

import java.io.Serializable;
import java.util.Date;

public class Evaluation implements Serializable{
    private int id;
    private String submitted_by, refers_to, comment;
    private int rating;
    private Date date;

    public Evaluation(int id, String submitted_by, String refers_to, int rating,Date date, String comment) {
        this.id = id;
        this.submitted_by = submitted_by;
        this.refers_to = refers_to;
        this.date = date;
        this.rating = rating;
        this.comment = comment;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comments) {
        this.comment = comments;
    }

    public String getSubmitted_by() {
        return submitted_by;
    }

    public void setSubmitted_by(String submitted_by) {
        this.submitted_by = submitted_by;
    }

    public String getRefers_to() {
        return refers_to;
    }

    public void setRefers_to(String refers_to) {
        this.refers_to = refers_to;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
