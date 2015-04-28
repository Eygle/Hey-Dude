package com.crouzet.cavalec.heydude.model;

import java.io.Serializable;

/**
 * Created by Johan on 19/02/2015.
 * User model
 */
public class User implements Serializable {
    // User id
    private String id;

    // User name
    private String name;

    // User email
    private String email;

    // User image
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
