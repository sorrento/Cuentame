package com.stupidpeople.cuentanos.diccionario;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;


public class fetchUrl extends AsyncTask<Void, Void, Definicion> {
    private final fetchingResults callback;
    private final String          url;


    fetchUrl(String fetchingUrl, fetchingResults callback) {
        this.callback = callback;
        this.url = fetchingUrl;
    }

    private static Definicion procesaRespuesta(Connection.Response response) {
        Definicion definicion = null;

        try {
            final JSONObject json = new JSONObject(response.body());

            try {
                definicion = new Definicion(json.getString("word"));

                JSONArray names = json.getJSONObject("meaning").names();// si tiene varias funciones como nombre femenino

                for (int i = 0; i < names.length(); i++) {
                    String    name     = (String) names.get(i);
                    JSONArray meanings = json.getJSONObject("meaning").getJSONArray(name);

                    Definicion.TipoDefinicion tipoDefinicion = new Definicion.TipoDefinicion(name);
                    for (int j = 0; j < meanings.length(); j++) {
                        tipoDefinicion.agregaDefinicionYEjemplo(new Definicion.unaDefinicion(meanings.getJSONObject(j)));
                    }

                    definicion.agregaFuncionGramatical(tipoDefinicion);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.d("pp", "onReceive: " + e);
        }

        return definicion;
    }

    @Override
    protected Definicion doInBackground(Void... params) {
        Connection.Response response = null;
        try {
            response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .execute();
            return procesaRespuesta(response);
        } catch (IOException e) {
            return new Definicion(e, url);
            //callback.onError(e);
        }

    }

    @Override
    protected void onPostExecute(Definicion definicion) {
        super.onPostExecute(definicion);

        if (!definicion.isEmpty()) {
            callback.onReceive(definicion);
        } else {
            callback.onEmptyAnswer(definicion.getException());
        }
    }

    public interface fetchingResults {
        void onReceive(Definicion response);

        void onError(IOException e);

        void onEmptyAnswer(IOException exception);

    }
}