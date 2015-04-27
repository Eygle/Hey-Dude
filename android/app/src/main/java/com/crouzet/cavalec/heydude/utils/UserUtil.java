package com.crouzet.cavalec.heydude.utils;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Johan on 16/04/2015.
 */
public class UserUtil {
    /**
     * Allows to modify current list of online users with server data
     */
    public static class ManageDataFromOnlineUsers {
        /**
         * Update current list of online users according to the server answer
         * @param data
         * @return
         */
        public static boolean update(JSONObject data) {
            boolean response = false;

            if (data.has("users")) {
                try {
                    JSONArray users = data.getJSONArray("users");
                    List<User> list = new ArrayList<>();

                    for (int i = 0; i < users.length(); ++i) {
                        JSONObject user = users.getJSONObject(i);

                        String name = null;
                        String gId = null;

                        if (user.has("name")) {
                            name = user.getString("name");
                        }
                        if (user.has("gId")) {
                            gId = user.getString("gId");
                        }

                        if (name == null || gId == null || gId.equals(HeyDudeSessionVariables.me.getId())) {
                            continue;
                        }

                        User u = new User(gId, name);

                        if (user.has("email")) {
                            u.setEmail(user.getString("email"));
                        }
                        if (user.has("image")) {
                            u.setImage(user.getString("image"));
                        }

                        list.add(u);
                    }

                    response = synchronizeUsersLists(list);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return response;
        }

        /**
         * Add the users presents on given list and absent from current list and remove users absent from given list and present in current list
         * @param list
         * @return
         */
        private static boolean synchronizeUsersLists(List<User> list) {
            boolean response = false;

            for (int i = 0; i < HeyDudeSessionVariables.onlineUsers.size(); ++i) {
                if (!list.contains(HeyDudeSessionVariables.onlineUsers.get(i))) {
                    HeyDudeSessionVariables.onlineUsers.remove(i);
                    response = true;
                }
            }

            for (int i = 0; i < list.size(); ++i) {
                User u = list.get(i);
                if (!HeyDudeSessionVariables.onlineUsers.contains(u)) {
                    HeyDudeSessionVariables.onlineUsers.add(u);
                    response = true;
                }
            }

            return response;
        }
    }
}
