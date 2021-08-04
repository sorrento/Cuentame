package com.stupidpeople.cuentanos.diccionario;

import com.stupidpeople.cuentanos.book.ArrayCallback;
import com.stupidpeople.cuentanos.utils.myLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Definator {
    private static final int                NUMBER_OF_WORDS    = 7;
    private final        String             language;
    private final        ArrayCallback      arrayCallback;
    private              DefinatorInterface definatorInterface = null;
    private              ArrayList<String>  definiciones;
    private              Queue<String>      palabrasCola;
    private              String             tag                = "DEFI";

    public Definator(String parrafo, String idioma, ArrayCallback arrayCallback) {
        language = idioma;
        definiciones = new ArrayList<>();
        this.arrayCallback = arrayCallback;
        DiccionarioUtils.topPalabrasRaras(parrafo, new ArrayCallback() {
            @Override
            public void onDone(List<String> arr, List<Double> bestScores) {
                definePalabras(arr.subList(0, NUMBER_OF_WORDS), bestScores);
            }
        });
    }

    /*public Definator(String parrafo, String language, final DefinatorInterface definatorInterface) {
        this.language = language;
        this.definatorInterface = definatorInterface;
        arrayCallback = null;

        DiccionarioUtils.topPalabrasRaras(parrafo, new ArrayCallback() {
            @Override
            public void onDone(List<String> arr, List<Double> bestScores) {
                List<String> palabras = arr.subList(0, NUMBER_OF_WORDS);
                definatorInterface.palabrasADefinir(palabras);
                definePalabras(palabras, bestScores);
            }
        });

    }*/

    private void definePalabras(List<String> palabras, List<Double> bestScores) {
        palabrasCola = new LinkedList<>(palabras);
        myLog.add("Vamos a definir las palabras:" + palabrasCola, tag);
        myLog.add("scores:" + bestScores, tag);
        procesaProximaPalabra();
    }

    private void procesaProximaPalabra() {
        final String palabra = palabrasCola.poll();
        myLog.add("trabajando con la palabra:" + palabra + " y quedan " + palabrasCola, tag);

        if (palabra == null) { // ha terminado
            List<Double> bestScores = null; //TODO arreglar;
            arrayCallback.onDone(definiciones, bestScores);
        } else {
            DiccionarioUtils.definePalabra(palabra, language, new fetchUrl.fetchingResults() {
                @Override
                public void onReceive(Definicion response) {
                    definicionRecibida(DiccionarioUtils.toParrafo(response));
                }

                @Override
                public void onError(IOException e) {
                    // definicionRecibida("Tuve un error en la definición");
                    myLog.error("error en la definicionde " + palabra, e);
                }

                @Override
                public void onEmptyAnswer(IOException exception) {
                    String s = "No tengo definicion  para " + palabra
                            + ": " + exception;
                    myLog.add(s, tag);
                    definicionRecibida(s);
                }
            });
        }

    }

    private void definicionRecibida(String s) {
        definiciones.add(s);
        procesaProximaPalabra();
    }
}
