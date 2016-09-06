package com.stupidpeople.cuentame;

import com.parse.ParseClassName;
import com.parse.ParseObject;

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
        String te = getText();
        te = te.replaceAll(" —", ", ");
        te = te.replaceAll("—", "");
        te = te.replaceAll("-", "");//TODo cambiar por expresion regular que comprueba que viene una palabra
        te = te.replaceAll("–¿", "¿");
        te = te.replaceAll("–¡", "¡");
        te = te.replaceAll("No\\.", "No . ");
        te = te.replaceAll("pie", "píe");
        te = te.replaceAll("local", "lokal");
        te = te.replaceAll("normal", " noormal");
        te = te.replaceAll("<<", "");
        te = te.replaceAll(">>", "");

        return te;
    }

    public String shortDescription() {
        final String tt = getText();
        final int i = 40;

        String substring = tt.length() < i ? tt : tt.substring(0, i);

        return shortestDescription() + " - [" + substring.replaceAll("\n", " ") + "]";
    }

    public String shortestDescription() {
        return getBookName() + "(" + getChapterId() + "|" + getBookId() + ")";
    }

}
