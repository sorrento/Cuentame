package com.stupidpeople.cuentame;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Arrays;

/**
 * Created by Milenko on 04/08/2016.
 */

@ParseClassName("libros")
public class Chapter extends ParseObject {
    private String autor;
    private String titulo;
    private int nLibro;
    private int nCapitulo;
    private String texto;

    //TODO para qué tener estas variables globales?
    public Chapter() {
    }

    static String processForReading(String text) {
        text = text.replaceAll(" —", ", ");
        text = text.replaceAll("—", "");
        text = text.replaceAll("-", "");//TODo cambiar por expresion regular que comprueba que viene una palabra
        text = text.replaceAll("–¿", "¿");
        text = text.replaceAll("–¡", "¡");
        text = text.replaceAll("No\\.", "No . ");
        text = text.replaceAll("pie", "píe");
        text = text.replaceAll("local", "lokal");
        text = text.replaceAll("normal", " noormal");
        text = text.replaceAll("<<", "");
        text = text.replaceAll(">>", "");
        text = text.replaceAll("\\.\\.\\.\\.", "...");
        text = text.replaceAll("\\.\\.", ".");
        text = text.replaceAll(":\\.", ".");
        return text;
    }

    public static String joinVersos(String[] versos, String sep) {

        StringBuilder builder = new StringBuilder();

        for (String s : Arrays.asList(versos)) {
            myLog.add("div", "agregando: " + s);
            builder.append(s).append(sep);
        }

        final String s2 = builder.toString();
        myLog.add("div", "afeter pegar de nuevo, queda:_" + s2);
        return s2;

    }

    public String getAuthor() {
        autor = getString("autor");
        return autor;
    }

    public String getBookName() {
        titulo = getString("titulo");
        return titulo;
    }

    public int getBookId() {
        nLibro = getInt("nLibro");
        return nLibro;
    }

    public int getChapterId() {
        nCapitulo = getInt("nCapitulo");
        return nCapitulo;
    }

    public String getLanguage() {
        String language = getString("language");
        if (language == null || language == "") language = "ES";

        return language;
    }

    public boolean isSong() {
        return getBoolean("isSong");
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "autor='" + getAuthor() + '\'' +
                ", titulo='" + getBookName() + '\'' +
                ", nLibro=" + getBookId() +
                ", nCapitulo=" + getChapterId() +
//                ", texto='" + texto + '\'' +
                '}';
    }

    public String getText() {
        texto = getString("texto");
        return texto;
    }

    /**
     * Text prepared to be read, for instance, removing "guiones"
     *
     * @return
     */
    public String getProcessedText() {
        return processForReading(getText());
    }

    public String shortDescription() {
        final String tt = getText();
        final int i = 40;

        return shortestDescription() + " - [" + myUtil.shortenText(tt, i).replaceAll("\n", " ") + "]";
    }

    public String shortestDescription() {
        return getBookName() + "(" + getChapterId() + "|" + getBookId() + ")";
    }

    /**
     * pensado para mostrar en la ruleta selectora
     *
     * @return
     */
    public String[] getDividedLyrics() {

        //TODO ojo procesar el que se mande como audio

        //        myLog.add("div", " \n parts= " + popo.length + " : " + popo.toString());

        return getText().split("\\. ");
    }

    public String[] getVersos(int iVersoIni, int iVersoEnd) {
        return Arrays.copyOfRange(getDividedLyrics(), iVersoIni, iVersoEnd);
    }
}
