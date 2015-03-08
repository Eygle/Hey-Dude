package com.crouzet.cavalec.heydude.utils;

import com.crouzet.cavalec.heydude.HeyDudeApplication;
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
    public static final String ACCEPT_CALL = "accept";
    public static final String REFUSE_CALL = "refuse";

    public static void getOnlineUsers(JsonHttpResponseHandler jsonHandler) {
        RequestParams params = new RequestParams();
        params.add("action", "online_users");

        HeyDudeRestClient.get(HeyDudeRestClient.API, params, jsonHandler, 30000);
    }

    public static void getUserCallingMe(JsonHttpResponseHandler jsonHandler) {
        RequestParams params = new RequestParams();
        params.add("action", "who_is_calling_me");

        HeyDudeRestClient.get(HeyDudeRestClient.API, params, jsonHandler, 30000);
    }

    public static void getCallStatus(JsonHttpResponseHandler jsonHandler) {
        RequestParams params = new RequestParams();
        params.add("action", "call_status");
        params.add("destGId", HeyDudeSessionVariables.dest.getId());

        HeyDudeRestClient.get(HeyDudeRestClient.API, params, jsonHandler, 30000);
    }

    public static void login() {
        RequestParams params = new RequestParams();
        params.add("action", "login");
        params.add("name", HeyDudeSessionVariables.name);
        params.add("image", HeyDudeSessionVariables.image);
        params.add("email", HeyDudeSessionVariables.email);
        params.add("publicKey", "");    // TODO generate and store key

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }

    public static void logout() {
        RequestParams params = new RequestParams();
        params.add("action", "logout");

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }

    public static void call(JsonHttpResponseHandler jsonHandler) {
        RequestParams params = new RequestParams();
        params.add("action", "call");
        params.add("destGId", HeyDudeSessionVariables.dest.getId());

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, jsonHandler);
    }

    public static void answerCall(String answer) {
        RequestParams params = new RequestParams();
        params.add("action", "answer");
        params.add("destGId", HeyDudeSessionVariables.dest.getId());
        params.add("status", answer);

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler(), 30000);
    }

    public static void hangup() {
        RequestParams params = new RequestParams();
        params.add("action", "hang_up");
        params.add("destGId", HeyDudeSessionVariables.dest.getId());

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }

    public static void deleteAccount() {
        RequestParams params = new RequestParams();
        params.add("action", "delete_account");
        params.add("destGId", HeyDudeSessionVariables.dest.getId());

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }
}
