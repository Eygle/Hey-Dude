package com.crouzet.cavalec.heydude;

import com.crouzet.cavalec.heydude.model.User;
import com.crouzet.cavalec.heydude.utils.Crypto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Johan on 19/02/2015.
 */
public class HeyDudeSessionVariables {
    public static String token;

    public static User me;
    public static User dest;

    public static List<User> onlineUsers = new ArrayList<>();

    // Crypto
    public static Crypto crypto;
    public static byte[] key;
}
