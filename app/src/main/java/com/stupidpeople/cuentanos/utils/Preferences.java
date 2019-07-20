package com.stupidpeople.cuentanos.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.stupidpeople.cuentanos.MainActivity;
import com.stupidpeople.cuentanos.book.Book;
import com.stupidpeople.cuentanos.book.BookSummary;

public class Preferences {
    private static final String PREFS_READING_CHAPTER_N      = "last chapter";
    private static final String PREFS_READING_BOOK_ID        = "last book";
    private static final String PREFS_LAN                    = "Language";
    private static final String PREFS_FIRST_TIME             = "first time";
    private static final String PREFS_STARTED_FROM_BEGINNING = "started from beginning";
    private static final String PREFS_IS_LOCAL_STORAGE       = "is local storage";
    private static final String PREFS_NCHAPS_PLAYED_IN_WEB   = "n chapters played from web";

    private SharedPreferences settings;

    public Preferences(MainActivity mainActivity) {
        settings = mainActivity.getPreferences(Context.MODE_PRIVATE);
        //isLocalStorage = settings.getBoolean(PREFS_IS_LOCAL_STORAGE, false);

        myLog.add("RECUPERANDO: from beginnig: " + settings.getBoolean(PREFS_STARTED_FROM_BEGINNING, false) +
                " lastbook: " + settings.getInt(PREFS_READING_BOOK_ID, 1) + " lastchap: " + settings.getInt(PREFS_READING_CHAPTER_N, 1), "PREFS");
    }

    public int getReadingChapterId() {
        return settings.getInt(PREFS_READING_CHAPTER_N, 1);
    }

    public void setReadingChapterId(int chapterId) {
        settings.edit().putInt(PREFS_READING_CHAPTER_N, chapterId).apply();
    }

    public int getReadingBookId() {
        return settings.getInt(PREFS_READING_BOOK_ID, 1);
    }

    public void setReadingBookId(int bookId) {
        settings.edit().putInt(PREFS_READING_BOOK_ID, bookId).apply();
    }

    public String getLanguage() {
        return settings.getString(PREFS_LAN, "ES");
    }

    public void setLanguage(String lan) {
        settings.edit().putString(PREFS_LAN, lan).apply();
    }

    public boolean getFirstTime() {
        return settings.getBoolean(PREFS_FIRST_TIME, true);
    }

    public void setFirstTime(boolean b) {
        settings.edit().putBoolean(PREFS_FIRST_TIME, b).apply();
    }

    public boolean getStartedFromBeginning() {
        return settings.getBoolean(PREFS_STARTED_FROM_BEGINNING, false);
    }

    public void setStartedFromBeginning(boolean b) {
        settings.edit().putBoolean(PREFS_STARTED_FROM_BEGINNING, b).apply();
    }

    public void setIsLocalStorage(boolean b) {
        settings.edit().putBoolean(PREFS_IS_LOCAL_STORAGE, b).apply();
        if (!b) settings.edit().putInt(PREFS_NCHAPS_PLAYED_IN_WEB, 0).apply();
    }

    public boolean isLocalStorage() {
        return settings.getBoolean(PREFS_IS_LOCAL_STORAGE, false);
    }

    public void setNReadedFromWeb(int i) {
        settings.edit().putInt(PREFS_NCHAPS_PLAYED_IN_WEB, i).apply();
    }

    public void update(Book book) {
        BookSummary bookSummary = book.getBookSummary();
        setReadingBookId(bookSummary.getId());
        setReadingChapterId(book.getCurrentChapterId());
        setLanguage(bookSummary.getLanguage());
    }

    public void nPlayedFromWebAddOneTo() {
        int j = settings.getInt(PREFS_NCHAPS_PLAYED_IN_WEB, 1);
        settings.edit().putInt(PREFS_NCHAPS_PLAYED_IN_WEB, j + 1).apply();
    }

    public int nPlayedFromWebGet() {
        return settings.getInt(PREFS_NCHAPS_PLAYED_IN_WEB, 0);
    }

    public void nPlayedFromWebReset() {
        settings.edit().putInt(PREFS_NCHAPS_PLAYED_IN_WEB, 0).apply();
    }
}
