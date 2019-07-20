package com.stupidpeople.cuentanos.book;

import com.stupidpeople.cuentanos.Lector.GenericTaskInterface;

public interface BookInterface {
    Memoria MEMORIA = null;

    // Getters

    String getBookName();

    String getBookNameFake();

    String getBookAuthor();

    String getBookAuthorFake();

    int getBookId();

    Chapter getCurrentChapter();

    int getCurrentChapterId();

    BookSummary getBookSummary();


    // Actions

    /**
     * Se encarga de traer de BBDD a la memoria los capitulos desde este chapterId
     *
     * @param bookId
     * @param chapterId
     */
    void prepareToRead(int bookId, int chapterId, GenericTaskInterface genericTaskInterface);

    void loadEntireBook(GenericTaskInterface genericTaskInterface);

    void loadChapters(int ini, int nChapters);


    void speakChapter(boolean interrupt);

//    void speakCurrentChapter(boolean interrupting);

    //void speakNextChapter();
}
