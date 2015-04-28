package com.crouzet.cavalec.heydude.http;

import android.os.Looper;
import android.util.Log;

import com.crouzet.cavalec.heydude.HeyDudeApplication;
import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

/**
 * Created by Johan on 19/02/2015.
 * Client used to send commands to server
 */
public class HeyDudeRestClient {
    private static final String TAG = HeyDudeRestClient.class.getSimpleName();

    // Server host
    public final static String HOST = HeyDudeApplication.host;
    // Server API
    public final static String API = HOST + "/heydude/api.php";

    private static AsyncHttpClient asyncHttpClient = new AsyncHttpClient(){{
        setTimeout(30000);
    }};

    private static SyncHttpClient syncHttpClient = new SyncHttpClient(){{
        setTimeout(30000);
    }};

    /**
     * @return an async client when calling from the main thread, otherwise a sync client.
     */
    private static AsyncHttpClient getClient()
    {
        // Return the synchronous HTTP client when the thread is not prepared
        if (Looper.myLooper() == null)
            return syncHttpClient;
        return asyncHttpClient;
    }

    /**
     * GET Request
     * @param url url to reach
     * @param params get parameters
     * @param responseHandler callback for answer
     * @param timeout request timeout
     * @return request handler
     */
    public static RequestHandle get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, int timeout) {
        params.put("gId", HeyDudeSessionVariables.me.getId());

        if (HeyDudeApplication.mock) {
            params.add("mock", "true");
        }

        Log.v(TAG, "Send GET Request: " + HeyDudeRestClient.API + "?" + params.toString());

        final AsyncHttpClient httpClient = getClient();
        httpClient.setTimeout(timeout);
        return httpClient.get(url, params, responseHandler);
    }


    /**
     * Send POST request
     * @param url url to reach
     * @param params post parameters
     * @param responseHandler callback for answer
     * @param httpClient client used to send request
     */
    private static void doPost(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, AsyncHttpClient httpClient) {
        params.put("gId", HeyDudeSessionVariables.me.getId());

        if (HeyDudeApplication.mock) {
            params.add("mock", "true");
        }

        Log.v(TAG, "Send POST Request: " + HeyDudeRestClient.API + "?" + params.toString());
        httpClient.post(url, params, responseHandler);
    }


    /**
     * Prepare POST request
     * @param url url to reach
     * @param params post parameters
     * @param responseHandler callback for answer
     */
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        doPost(url, params, responseHandler, getClient());
    }


    /**
     * Prepare POST request
     * @param url url to reach
     * @param params post parameters
     * @param responseHandler callback for answer
     * @param timeout request timeout
     */
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, int timeout) {
        AsyncHttpClient httpClient = getClient();
        httpClient.setTimeout(timeout);
        doPost(url, params, responseHandler, httpClient);
    }
}

