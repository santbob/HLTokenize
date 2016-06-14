package com.santhoshn.hltokenize.service;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.santhoshn.hltokenize.AppController;
import com.santhoshn.hltokenize.service.TLSSocketFactory;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by santhosh on 09/06/16.
 */
public class HLTokenService {

    private static final String TAG = "HLTokenService";

    public interface TokenCallback {
        void onComplete(HLToken response);
    }

    private String mPublicKey;
    private String mApiUrl;

    public HLTokenService(String publicKey) {
        this.mPublicKey = publicKey;
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKey can not be null");
        }

        String[] components = mPublicKey.split("_");

        if (components.length < 3) {
            throw new IllegalArgumentException("publicKey format invalid, please make sure you have set the public key to the constructor");
        }

        String env = components[1].toLowerCase();

        if (env.equals("prod")) {
            mApiUrl = "https://api2.heartlandportico.com/SecureSubmit.v1/api/token";
        } else {
            mApiUrl = "https://cert.api2.heartlandportico.com/Hps.Exchange.PosGateway.Hpf.v1/api/token";
        }

        //TODO: uncomment the below link to test with my server which returns the same json you send to it.
//        mApiUrl = "https://712f09db.ngrok.io/v1/hltokenize";
    }

    public void getToken(HLCard card, TokenCallback callback) {
        TokenAsyncTask asyncTask = new TokenAsyncTask();
        asyncTask.execute(new TokenTaskInput(card, callback));
//        executeWithVolley(card, callback);
    }

    public void executeWithVolley(final HLCard card, final TokenCallback callback) {
        try {
            JSONObject cardObject = new JSONObject();
            cardObject.put("cvc", card.getCvv());
            cardObject.put("exp_month", card.getExpMonth());
            cardObject.put("exp_year", card.getExpYear());
            cardObject.put("number", card.getNumber());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("object", "token");
            jsonObject.put("token_type", "supt");
            jsonObject.put("card", cardObject);

            byte[] creds = String.format("%s:", mPublicKey).getBytes();
            String auth = String.format("Basic %s", Base64.encodeToString(creds, Base64.URL_SAFE));

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Authorization", auth);


            VolleyRequest req = new VolleyRequest(Request.Method.POST, mApiUrl, headers, jsonObject, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response);
                    Gson gson = new Gson();
                    HLToken token = gson.fromJson(response, HLToken.class);
                    if (TextUtils.isEmpty(token.getTokenValue())) {
                        token.setToken_value("dummy token value, but successful return from server.");
                    }
                    callback.onComplete(token);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, error.toString());
                    callback.onComplete(null);
                }
            });
            AppController.getInstance().addToRequestQueue(req, TAG);
        } catch (Exception e) {
            Log.d(TAG, "error " + e.toString());
        }

    }

    private class TokenAsyncTask extends AsyncTask<TokenTaskInput, Void, HLToken> {
        private TokenTaskInput taskInput;

        @Override
        protected HLToken doInBackground(TokenTaskInput... inputParams) {
            this.taskInput = inputParams[0];
            HLToken tokenObject = null;
            try {
                TLSSocketFactory sf = new TLSSocketFactory();
                HttpsURLConnection conn = (HttpsURLConnection) new URL(mApiUrl).openConnection();
                conn.setSSLSocketFactory(sf);

                //converting the publickey to base64 format and adding as Basic Authorization.
                byte[] creds = String.format("%s:", mPublicKey).getBytes();
                String auth = String.format("Basic %s", Base64.encodeToString(creds, Base64.URL_SAFE));


                Gson gson = new Gson();
                String payload = gson.toJson(new HLToken(taskInput.getCard()));
                byte[] bytes = payload.getBytes();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.addRequestProperty("Authorization", auth.trim());
                conn.addRequestProperty("Content-Type", "application/json");
                conn.addRequestProperty("Content-Length", String.format("%s", bytes.length));

                DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream());
                requestStream.write(bytes);
                requestStream.flush();
                requestStream.close();

                try {
                    InputStreamReader responseStream = new InputStreamReader(conn.getInputStream());
                    tokenObject = gson.fromJson(responseStream, HLToken.class);
                    responseStream.close();
                } catch (IOException e) {
                    if (conn.getResponseCode() == 400) {
                        InputStreamReader errorStream = new InputStreamReader(conn.getErrorStream());
                        tokenObject = gson.fromJson(errorStream, HLToken.class);
                        errorStream.close();
                    } else {
                        Log.d(TAG, "IOException occured " + e.toString());
                        throw new IOException(e);
                    }
                }

            } catch (Exception e) {
                Log.d(TAG, "Exception occured " + e.toString());
            }

            return tokenObject;
        }

        @Override
        protected void onPostExecute(HLToken tokenObject) {
            TokenCallback callback = taskInput.getCallback();
            callback.onComplete(tokenObject);
        }
    }

    public class TokenTaskInput {
        private HLCard card;
        private TokenCallback callback;

        public TokenTaskInput(HLCard card, TokenCallback callback) {
            this.card = card;
            this.callback = callback;
        }

        public HLCard getCard() {
            return card;
        }

        public TokenCallback getCallback() {
            return callback;
        }
    }
}
