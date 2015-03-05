package com.crouzet.cavalec.heydude.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.crouzet.cavalec.heydude.HeyDudeConstants;
import com.crouzet.cavalec.heydude.model.User;
import com.crouzet.cavalec.heydude.utils.ApiUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
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

            ApiUtils.getUserCallingMe(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        Log.d("Request success", response.toString());
                        new UpdateData().execute(response);
                        if (mRunning) {
                            Log.d(TAG, "Scheduling new refresh");
                            handler.postDelayed(r, CHECK_CALLS_DELAY);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject error) {
                    super.onFailure(statusCode, headers, throwable, error);

                    try {
                        Log.d("Request error", error.toString());
                        if (mRunning) {
                            Log.d(TAG, "Scheduling new refresh");
                            handler.postDelayed(r, CHECK_CALLS_DELAY);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRunning = true;

        Log.d(TAG, "onCreate");

        long currentTime = System.currentTimeMillis();
        long diff = currentTime - mTimeAtDestroy;
        if (mTimeAtDestroy > 0 && diff < CHECK_CALLS_DELAY){
            Log.d(TAG, "Schedule update online users in " + ((CHECK_CALLS_DELAY - diff) / 1000) + " seconds");
            handler.postDelayed(checkCalls, CHECK_CALLS_DELAY - diff);
        } else{
            Log.d(TAG, "Update now.");
            handler.post(checkCalls);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRunning = false;
        mTimeAtDestroy = System.currentTimeMillis();
        handler.removeCallbacks(checkCalls);
        Log.d(TAG, "onDestroy");
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
                    String IP = data[0].getString("IP");

                    caller = new User(gId, name, IP);

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