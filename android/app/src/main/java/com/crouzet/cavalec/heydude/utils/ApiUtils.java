package com.crouzet.cavalec.heydude.utils;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Johan on 03/03/2015.
 */
public class ApiUtils {
    public static void getOnlineUsers(JsonHttpResponseHandler jsonHandler) {
        RequestParams params = new RequestParams();
        params.add("action", "get_online_users");

        HeyDudeRestClient.get(HeyDudeRestClient.API, params, jsonHandler, 30000);
    }

    public static void connect() {
        RequestParams params = new RequestParams();
        params.add("action", "login");

        JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response){
                super.onFailure(statusCode, headers, throwable, response);

                try  {
                    if (response.has("error")) {
                        if (response.getString("error").equals("No user is register with this gId")) {
                            // If the user can't connect because it doesn't exist we add it to the server
                            RequestParams params = new RequestParams();
                            params.add("action", "login");
                            params.add("name", HeyDudeSessionVariables.name);
                            params.add("gImage", HeyDudeSessionVariables.image);
                            params.add("email", HeyDudeSessionVariables.email);
                            params.add("publicKey", "");    // TODO generate and store key
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, handler);
    }
}
