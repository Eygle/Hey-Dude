package com.crouzet.cavalec.heydude.model;

import java.util.Date;

/**
 * Created by Johan on 19/02/2015.
 */
public class Message {
    private String msg;
    private String authorName;
    private String destName;
    private Date sended = null;
    private Date received = null;

    public Message(String m, String a, String d, Date s, Date r) {
        msg = m;
        authorName = a;
        destName = d;
        sended = s;
        received = r;
    }

    public Message(String m, String a, String d,  Date s) {
        msg = m;
        authorName = a;
        destName = d;
        sended = s;
    }

    public Message(String m, String a, String d) {
        msg = m;
        authorName = a;
        destName = d;
    }

    public Date getReceived() {
        return received;
    }

    public Date getSended() {
        return sended;
    }

    public String getMsg() {
        return msg;
    }

    public String getDestName() {
        return destName;
    }

    public String getAuthorName() {
        return authorName;
    }
}
