package com.crouzet.cavalec.heydude.model;

/**
 * Created by Johan on 19/02/2015.
 */
public class Contact {
    private int id;
    private String firstName;
    private String lastName;

    public Contact(int i, String f, String l) {
        id = i;
        firstName = f;
        lastName = l;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
