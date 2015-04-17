package com.crouzet.cavalec.heydude.model;

import java.io.Serializable;

/**
 * Created by Johan on 19/02/2015.
 */
public class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String image;

    public User(String gId, String name) {
        this.id = gId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User && id.equals(((User) o).getId());
    }
}
