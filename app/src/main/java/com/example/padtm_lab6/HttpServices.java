package com.example.padtm_lab6;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpServices extends IntentService {

    public static final int GAMES_LIST = 1;
    public static final int IN_ROW = 2;
    public static final int XO_GAME = 2020;
    public static final int REFRESH = 3;
    public static final int GAME_INFO = 4;
    public static final String URL = "URL";
    public static final String METHOD = "Method";
    public static final String PARAMS = "Params";
    public static final String RETURN = "Return";
    public static final String RESPONSE = "Response";
    public static final String LINES = "http://games.antons.pl/lines/";
    public static final String XO = "http://games.antons.pl/xo/";
    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;


    public HttpServices() {
        super("HTTP calls handler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String urlstr = intent.getStringExtra(HttpServices.URL);
            java.net.URL url = new URL(urlstr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            switch (intent.getIntExtra(HttpServices.METHOD, 1)) {
                case HttpServices.POST:
                    conn.setRequestMethod("POST");
                    break;
                case HttpServices.PUT:
                    conn.setRequestMethod("PUT");
                    break;
                default:
                    conn.setRequestMethod("GET");
            }
            Config conf = new Config(getApplicationContext());
            conn.setRequestProperty("PKEY", conf.getPublic().replace("\n", ""));
            conn.setRequestProperty("SIGN", conf.sign(urlstr).replace("\n", ""));

            String params = intent.getStringExtra(HttpServices.PARAMS);
            if (params != null) {
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(params);
                writer.flush();
                writer.close();
            }
            conn.connect();

            int responseCode = conn.getResponseCode();

            String response = "";
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            }
            conn.disconnect();

            Intent returns = new Intent();
            returns.putExtra(HttpServices.RESPONSE, response);
            PendingIntent reply = intent.getParcelableExtra(HttpServices.RETURN);
            reply.send(this, responseCode, returns);

        } catch (Exception ex) {
            Log.d("CONNERROR", ex.toString());
        }
    }
}