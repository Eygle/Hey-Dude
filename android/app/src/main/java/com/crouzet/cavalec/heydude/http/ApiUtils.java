package com.crouzet.cavalec.heydude.http;

import android.util.Base64;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.utils.CryptoRSA;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Johan on 03/03/2015.
 * Methods used to communicate with the server
 */
public class ApiUtils {
    public static final String ACCEPT_CALL = "accept";
    public static final String REFUSE_CALL = "refuse";

    /**
     * Send login
     */
    public static void login() {
        RequestParams params = new RequestParams();
        params.add("action", "login");
        params.add("name", HeyDudeSessionVariables.me.getName());
        params.add("image", HeyDudeSessionVariables.me.getImage());
        params.add("email", HeyDudeSessionVariables.me.getEmail());
        params.add("token", HeyDudeSessionVariables.token);
        params.add("publicKey", Base64.encodeToString(CryptoRSA.getInstance().getPubKey(), Base64.DEFAULT));

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }

    /**
     * Send logout
     */
    public static void logout() {
        RequestParams params = new RequestParams();
        params.add("action", "logout");

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }

    /**
     * Call other user
     */
    public static void call() {
        RequestParams params = new RequestParams();
        params.add("action", "call");
        params.add("destGId", HeyDudeSessionVariables.dest.getId());

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }

    /**
     * Answer pending call
     * @param answer can be values in ACCEPT_CALL or REFUSE_CALL
     * @param gId Receiver Google id
     */
    public static void answerCall(String answer, String gId) {
        RequestParams params = new RequestParams();
        params.add("action", "answer");
        params.add("destGId", gId);
        params.add("status", answer);

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler(), 30000);
    }

    /**
     * Stop a call
     */
    public static void hangup() {
        RequestParams params = new RequestParams();
        params.add("action", "hang_up");
        params.add("destGId", HeyDudeSessionVariables.dest.getId());

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }

    /**
     * Remove account from server
     */
    public static void deleteAccount() {
        RequestParams params = new RequestParams();
        params.add("action", "delete_account");
        params.add("destGId", HeyDudeSessionVariables.dest.getId());

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }

    /**
     * Send encrypted message
     * @param message encrypted message
     * @param iv Initialization vector
     */
    public static void sendMessage(byte[] message, byte[] iv) {
        RequestParams params = new RequestParams();
        params.add("action", "sendMessage");
        params.add("message", Base64.encodeToString(message, Base64.DEFAULT));
        params.add("iv", Base64.encodeToString(iv, Base64.DEFAULT));
        params.add("destGId", HeyDudeSessionVariables.dest.getId());

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }

    /**
     * Send AES symmetric key to receiver
     * @param key AES key
     */
    public static void sendKey(byte[] key) {
        RequestParams params = new RequestParams();
        params.add("action", "sendKey");
        params.add("key", Base64.encodeToString(key, Base64.DEFAULT));
        params.add("destGId", HeyDudeSessionVariables.dest.getId());

        HeyDudeRestClient.post(HeyDudeRestClient.API, params, new JsonHttpResponseHandler());
    }
}
