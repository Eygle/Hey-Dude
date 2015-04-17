package com.crouzet.cavalec.heydude.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.crouzet.cavalec.heydude.HeyDudeConstants;
import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.http.ApiUtils;
import com.crouzet.cavalec.heydude.http.ResponseHandler;
import com.crouzet.cavalec.heydude.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Johan on 03/03/2015.
 * Service which keeps the online users list update
 */
public class BackgroundServiceUpdateOnlineUsers extends Service {
    public static final String TAG = BackgroundServiceUpdateOnlineUsers.class.getSimpleName();
    private final int UPDATE_USERS_DELAY = 5000;

    private Handler handler = new Handler();

    public static boolean mRunning = false;

    private static long mTimeAtDestroy;

    /**
     * Runnable to update online users
     */
    private Runnable updateOnlineUsers = new Runnable() {
        @Override
        public void run() {
            final Runnable r = this;

            if (HeyDudeSessionVariables.me == null) {
                handler.postDelayed(r, UPDATE_USERS_DELAY);
                return;
            }
            ApiUtils.getOnlineUsers(new ResponseHandler() {
                @Override
                public void success(JSONObject response) {
                    Log.v(TAG, "Online request success: " + response.toString());
                    new UpdateData().execute(response);

                    if (mRunning) {
                        Log.v(TAG, "Scheduling new refresh");
                        handler.postDelayed(r, UPDATE_USERS_DELAY);
                    }
                }

                @Override
                public void failure() {
                    if (mRunning) {
                        Log.v(TAG, "Scheduling new refresh");
                        handler.postDelayed(r, UPDATE_USERS_DELAY);
                    }
                }
            });
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRunning = true;

        Log.v(TAG, "onCreate");

        long currentTime = System.currentTimeMillis();
        long diff = currentTime - mTimeAtDestroy;
        if (mTimeAtDestroy > 0 && diff < UPDATE_USERS_DELAY){
            Log.v(TAG, "Schedule update online users in " + ((UPDATE_USERS_DELAY - diff) / 1000) + " seconds");
            handler.postDelayed(updateOnlineUsers, UPDATE_USERS_DELAY - diff);
        } else{
            Log.v(TAG, "Update now.");
            handler.post(updateOnlineUsers);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRunning = false;
        mTimeAtDestroy = System.currentTimeMillis();
        handler.removeCallbacks(updateOnlineUsers);
        Log.v(TAG, "onDestroy");
    }


    /**
     * Async Task to update online users data
     */
    private class UpdateData extends AsyncTask<JSONObject, Integer, Boolean> {

        protected Boolean doInBackground(JSONObject... data) {
            return ManageDataFromOnlineUsers.update(data[0]);
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Intent intent = new Intent();
                intent.setAction(HeyDudeConstants.BROADCAST_REFRESH_USER_LIST);
                sendBroadcast(intent);
            }
        }
    }

    /**
     * Allows to modify current list of online users with server data
     */
    private static class ManageDataFromOnlineUsers {
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

                        if (name == null || gId == null) {
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
