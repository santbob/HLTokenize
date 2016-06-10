package com.santhoshn.hltokenize.service;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by santhosh on 09/06/16.
 */
public class HLTokenService {
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
    }

    public void getToken(HLCard card, TokenCallback callback) {
        TokenAsyncTask asyncTask = new TokenAsyncTask();
        asyncTask.execute(new TokenTaskInput(card, callback));
    }

    private class TokenAsyncTask extends AsyncTask<TokenTaskInput, Void, HLToken> {
        private TokenTaskInput taskInput;

        @Override
        protected HLToken doInBackground(TokenTaskInput... inputParams) {
            this.taskInput = inputParams[0];
            HLToken tokenObject = null;
            try {
                HttpsURLConnection conn = (HttpsURLConnection) new URL(mApiUrl).openConnection();

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
                        Log.d("HeartLand", "IOException occured " + e.toString());
                        throw new IOException(e);
                    }
                }

            } catch (Exception e) {
                Log.d("HeartLand", "Exception occured " + e.toString());
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
