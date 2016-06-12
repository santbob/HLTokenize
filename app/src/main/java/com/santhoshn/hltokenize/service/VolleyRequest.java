package com.santhoshn.hltokenize.service;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by santhosh on 10/06/16.
 */
public class VolleyRequest extends StringRequest {

    private Map<String, String> mHeaders;
    private JSONObject mRequestBody;

    protected static final String PROTOCOL_CHARSET = "utf-8";
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    public VolleyRequest(int method, String url, Map<String, String> headers, JSONObject requestBody, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.mHeaders = headers;
        this.mRequestBody = requestBody;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.putAll(mHeaders);
        headers.put("Content-Type", "application/json");
        headers.put("Content-Length", "" + getBody().length);
        return headers;
    }

    @Override
    public byte[] getPostBody() {
        return getBody();
    }

    @Override
    public byte[] getBody() {
        try {
            String jsonStr = mRequestBody == null ? null : mRequestBody.toString();
            if (jsonStr != null) {
                return jsonStr.getBytes(PROTOCOL_CHARSET);
            }
            return null;
        } catch (UnsupportedEncodingException uee) {
            Log.d("VolleyRequest", "Unsupported Encoding while trying to get the bytes of " + mRequestBody + " using utf-8");
            return null;
        }
    }

    @Override
    public String getPostBodyContentType() {
        return getBodyContentType();
    }


    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }
}
