package com.crouzet.cavalec.heydude.model;

import java.util.Date;

/**
 * Created by Johan on 19/02/2015.
 * Message model
 */
public class Message {
    // Message id
    private long id;

    // Message text
    private String message;

    // Message author
    private String authorName;

    // Message receiver
    private String destName;

    // Author image
    private String image;

    // Message date
    private Date date;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getDestName() {
        return destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
