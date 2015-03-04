package com.crouzet.cavalec.heydude;

import com.crouzet.cavalec.heydude.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Johan on 19/02/2015.
 */
public class HeyDudeSessionVariables {
    public static String id;
    public static String login;
    public static String email;
    public static String image;

    public static List<User> onlineUsers = new ArrayList<>();

    public static User dest = null;
}
