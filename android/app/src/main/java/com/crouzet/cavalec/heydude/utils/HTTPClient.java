package com.crouzet.cavalec.heydude.utils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by Johan on 19/02/2015.
 */

public class HTTPClient {
    private int     statusCode;
    private boolean hasError;
    private int     timeout;

    public String doHttpRequest(String address) {

        this.hasError = false;
        this.statusCode = 0;

        String content = null;

        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, this.timeout);
        HttpConnectionParams.setSoTimeout(httpParams, this.timeout);
        DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
        HttpGet httpget = new HttpGet(address);
        HttpResponse response = null;
        try {
            response = httpclient.execute(httpget);
            this.statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            content = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
        } catch (IOException e) {
            this.hasError = true;
        } finally {
            if (response != null) {
                try {
                    response.getEntity().consumeContent();
                } catch (IOException e) {
                    this.hasError = true;
                }
            }
        }
        return content;
    }

    public int getLastStatusCode() {
        return this.statusCode;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean terminateWithError() {
        return this.hasError;
    }
}

