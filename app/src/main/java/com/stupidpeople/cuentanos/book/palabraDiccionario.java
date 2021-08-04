package com.stupidpeople.cuentanos.book;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("diccionario")
public class palabraDiccionario extends ParseObject {
    public palabraDiccionario() {
    }

    public String getWord() {
        return getString("word");
    }

    public Double getScore() {
        return getDouble("r");
    }
}
