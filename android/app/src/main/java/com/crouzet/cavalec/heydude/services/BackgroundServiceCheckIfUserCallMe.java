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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Johan on 04/03/2015.
 */
public class BackgroundServiceCheckIfUserCallMe extends Service {
    public static final String TAG = BackgroundServiceCheckIfUserCallMe.class.getSimpleName();
    private final int CHECK_CALLS_DELAY = 3000;

    private Handler handler = new Handler();

    public static boolean mRunning = false;

    private static long mTimeAtDestroy;

    /**
     * Runnable to update online users
     */
    private Runnable checkCalls = new Runnable() {
        @Override
        public void run() {
            final Runnable r = this;

            if (HeyDudeSessionVariables.me == null) {
                handler.postDelayed(r, CHECK_CALLS_DELAY);
                return;
            }
            ApiUtils.getUserCallingMe(new ResponseHandler() {
                @Override
                public void success(JSONObject response) {
                    Log.v(TAG, "Calls request success: " + response.toString());
                    new UpdateData().execute(response);

                    if (mRunning) {
                        Log.v(TAG, "Scheduling new refresh");
                        handler.postDelayed(r, CHECK_CALLS_DELAY);
                    }
                }

                @Override
                public void failure() {
                    if (mRunning) {
                        Log.v(TAG, "Scheduling new refresh");
                        handler.postDelayed(r, CHECK_CALLS_DELAY);
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
        if (mTimeAtDestroy > 0 && diff < CHECK_CALLS_DELAY){
            Log.v(TAG, "Schedule update online users in " + ((CHECK_CALLS_DELAY - diff) / 1000) + " seconds");
            handler.postDelayed(checkCalls, CHECK_CALLS_DELAY - diff);
        } else{
            Log.v(TAG, "Update now.");
            handler.post(checkCalls);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRunning = false;
        mTimeAtDestroy = System.currentTimeMillis();
        handler.removeCallbacks(checkCalls);
        Log.v(TAG, "onDestroy");
    }


    /**
     * Async Task to update online users data
     */
    private class UpdateData extends AsyncTask<JSONObject, Integer, Boolean> {

        private User caller = null;

        protected Boolean doInBackground(JSONObject... data) {
            boolean response = false;

            try {
                if (data[0].has("gId")) {
                    String gId = data[0].getString("gId");
                    String name = data[0].getString("name");

                    caller = new User(gId, name);

                    if (data[0].has("image")) {
                        caller.setImage(data[0].getString("image"));
                    }
                    if (data[0].has("email")) {
                        caller.setEmail(data[0].getString("email"));
                    }

                    response = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return response;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Intent intent = new Intent();
                intent.setAction(HeyDudeConstants.BROADCAST_RECEIVE_CALL);

                intent.putExtra("caller", caller);

                sendBroadcast(intent);
            }
        }
    }
}