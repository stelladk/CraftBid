package com.craftbid.craftbid.model;

public class Evaluation {
    private int id;
    private String date;
    private int rating;
//    private User reviewer;
//    private Creator reviewed;
    private String reviewer, reviewed;
    private String comments;

    public Evaluation(int id, String date, int rating, String reviewer, String reviewed, String comments) {
        this.id = id;
        this.date = date;
        this.rating = rating;
        this.reviewer = reviewer;
        this.reviewed = reviewed;
        this.comments = comments;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getReviewed() {
        return reviewed;
    }

    public void setReviewed(String reviewed) {
        this.reviewed = reviewed;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
