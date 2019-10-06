package com.stupidpeople.cuentanos.diccionario;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Definicion {

    private final String                    word;
    private final ArrayList<TipoDefinicion> arrFuncionGramatical;
    private       IOException               exception = null;

    Definicion(String word) {
        this.word = word;
        this.arrFuncionGramatical = new ArrayList<>();
        //idioma=
    }


    public Definicion(IOException e, String url) {
        word = url;
        exception = e;
        arrFuncionGramatical = new ArrayList<>();
    }

    boolean isEmpty() {
        return arrFuncionGramatical.size() == 0;
    }

    public String getWord() {
        return word;
    }

    void agregaFuncionGramatical(TipoDefinicion tipoDefinicion) {
        arrFuncionGramatical.add(tipoDefinicion);
    }

    public TipoDefinicion getOneFuncionGramatical(int i) {
        return arrFuncionGramatical.get(i);
    }

    public int getNumberOfFuncionesGramaticales() {
        return arrFuncionGramatical.size();
    }

    public ArrayList<TipoDefinicion> getFuncionesGramaticales() {
        return arrFuncionGramatical;
    }

    public IOException getException() {
        return exception;
    }

    public static class unaDefinicion implements Parcelable { //TODO puedo quitar esto de parcelable?
        public static final Creator<unaDefinicion> CREATOR = new Creator<unaDefinicion>() {
            @Override
            public unaDefinicion createFromParcel(Parcel in) {
                return new unaDefinicion(in);
            }

            @Override
            public unaDefinicion[] newArray(int size) {
                return new unaDefinicion[size];
            }
        };
        String defi;
        String ejemplo = null;

        unaDefinicion(JSONObject meaning) throws JSONException {
            defi = meaning.getString("definition");
            if (meaning.has("example")) {
                ejemplo = meaning.getString("example");
            }
        }

        unaDefinicion(Parcel in) {
            defi = in.readString();
            ejemplo = in.readString();
        }

        public String getDefi() {
            return defi;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(defi);
            dest.writeString(ejemplo);
        }

        public String getEjemplo() {
            return ejemplo;
        }

        public boolean hasExample() {
            return ejemplo != null;
        }
    }

    public static class TipoDefinicion {

        private final String                   funcionGramatical;
        private final ArrayList<unaDefinicion> arr;

        TipoDefinicion(String name) {
            funcionGramatical = name;
            arr = new ArrayList<>();
        }

        public String getFuncionGramatical() {
            return funcionGramatical;
        }

        void agregaDefinicionYEjemplo(unaDefinicion unaDefinicion) {
            arr.add(unaDefinicion);
        }

        public unaDefinicion getOneDefinition(int i) {
            return arr.get(i);
        }

        public ArrayList<unaDefinicion> getDefinitions() {
            return arr;
        }
    }
}
