package com.stupidpeople.cuentanos.book;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.stupidpeople.cuentanos.Constants;
import com.stupidpeople.cuentanos.utils.text;

/**
 * Created by Milenko on 04/08/2016.
 */

@ParseClassName("libros")
public class Chapter extends ParseObject {

    public Chapter() {
    }

    public String getAuthor() {
        return getString("autor");
    }

    public String getBookName() {
        return getString("titulo");
    }

    private int getBookId() {
        return getInt("nLibro");
    }

    public int getChapterId() {
        return getInt("nCapitulo");
    }

//    public String getLanguage() {
//        String language = getString("language");
//        if (language == null || language.equals("")) language = "ES";
//
//        return language;
//    }

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
        String texto;

        if (Constants.debugMode) texto = text.shortenText(getString("texto"), 150);
        else texto = getString("texto");

        return texto;
    }

    /**
     * Text prepared to be read, for instance, removing "guiones"
     */
    public String getProcessedText() {
        return text.processForReading(getText());
    }

    private String shortDescription() {
        final int i = 40;

        return shortestDescription() + " - [" +
                text.shortenText(getText(), i).replaceAll("\n", " ") + "]";
    }

    private String shortestDescription() {
        return getBookName() + "(" + getChapterId() + "|" + getBookId() + ")";
    }

    public String getUtterance() {
        //return "(chapter)" + shortDescription(); esto a veces falla
        return Constants.PREFIX_OF_CHAPTER_UTTERANCE + shortestDescription();
    }
}
