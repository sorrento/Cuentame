package com.stupidpeople.cuentanos.diccionario;

import java.util.List;

public interface DefinatorInterface {
    void onDone(String text);

    void palabrasADefinir(List<String> palabras);
}
