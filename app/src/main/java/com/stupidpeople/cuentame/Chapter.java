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
        te = te.replaceAll("No\\.", "No . ");
        te = te.replaceAll("pie", "píe");
//        Log.d("mhp", "")
        return te;
    }

    public String shortDescription() {
        return getBookName() + "(" + getBookId() + "|" + getChapterId() + ")- [" + getText().substring(0, 40).replaceAll("\n", " ") + "]";
    }

    public String shortestDescription() {
        return getBookName() + "(" + getBookId() + "|" + getChapterId() + ")";
    }

}
