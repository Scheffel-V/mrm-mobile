package com.example.mrm.mobile.model;

public class CommentHistory {
    String date;
    String event;
    String comment;

    public CommentHistory(String date, String event, String comment) {
        this.date = date;
        this.event = event;
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
