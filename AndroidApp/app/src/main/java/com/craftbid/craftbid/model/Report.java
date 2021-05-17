package com.craftbid.craftbid.model;

import java.io.Serializable;
import java.util.Date;

public class Report implements Serializable {
    private int id;
    private String submitted_by,refers_to,reason,description;
    private Date date;

    public Report(int id, String submitted_by, String refers_to, String reason, String description, Date date) {
        this.id = id;
        this.submitted_by = submitted_by;
        this.refers_to = refers_to;
        this.reason = reason;
        this.description = description;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
