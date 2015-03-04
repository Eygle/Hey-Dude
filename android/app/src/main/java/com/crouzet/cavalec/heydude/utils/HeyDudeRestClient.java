package com.crouzet.cavalec.heydude.utils;
import com.crouzet.cavalec.heydude.HeyDudeApplication;
import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import android.os.Looper;

/**
 * Created by Johan on 19/02/2015.
 */
public class HeyDudeRestClient {

    public final static String HOST = HeyDudeApplication.host;
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
     */
    public static RequestHandle get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, int timeout) {

        params.put("id", HeyDudeSessionVariables.uid);

        final AsyncHttpClient httpClient = getClient();
        httpClient.setTimeout(timeout);
        return httpClient.get(url, params, responseHandler);
    }


    /**
     * POST Request
     */
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        params.put("id", HeyDudeSessionVariables.uid);
        getClient().post(url, params, responseHandler);
    }


    /**
     * Build a get request
     */
    public static String buildGetRequest(String url, String params){
        return url + "?v=1&os=android" + (params.length() > 0 ? "&" + params : "");
    }

}

