package com.stupidpeople.cuentanos.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.stupidpeople.cuentanos.MainActivity;
import com.stupidpeople.cuentanos.book.Book;
import com.stupidpeople.cuentanos.book.BookSummary;

import java.util.HashSet;
import java.util.Set;

public class Preferences {
    private static final String PREFS_READING_CHAPTER_N      = "last chapter";
    private static final String PREFS_READING_BOOK_ID        = "last book";
    private static final String PREFS_LAN                    = "Language";
    private static final String PREFS_FIRST_TIME             = "first time";
    private static final String PREFS_STARTED_FROM_BEGINNING = "started from beginning";
    private static final String PREFS_IS_LOCAL_STORAGE       = "is local storage";
    private static final String PREFS_NCHAPS_PLAYED_IN_WEB   = "n chapters played from web";
    private static final String PREFS_MAX_CHAPTERS           = "max_chapter";
    private static final int    N_READED_WEB                 = 6;
    public static final  String PREFS_HATED_LIST             = "hated_books";
    private static final String PREFS_ENDED_LIST             = "ended_books";

    private SharedPreferences settings;
    private String            TAG = "PREF";

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
        //cambio de libro
        if (getReadingBookId() != bookId) {
            nPlayedFromWebReset();
        }
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

    public String getStorageType() {
        return isLocalStorage() ? "LOCAL" : "WEB";
    }

    private void setNReadedFromWeb(int i) {
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

    private int nPlayedFromWebGet() {
        int anInt = settings.getInt(PREFS_NCHAPS_PLAYED_IN_WEB, 0);
        myLog.add("reproducidos en web" + anInt, TAG);
        return anInt;
    }

    public boolean nPlayedFromWebSuperaUmbral() {
        boolean b = nPlayedFromWebGet() > N_READED_WEB;
        if (b) myLog.add("********superado el umbral de n web", TAG);
        return b;
    }

    public void nPlayedFromWebReset() {
        settings.edit().putInt(PREFS_NCHAPS_PLAYED_IN_WEB, 0).apply();
    }

    public void setBookParams(int bookId, int chapterId, int maxChapters, boolean isLocalStorage) {
        setReadingBookId(bookId);
        setReadingChapterId(chapterId);
        setMaxChapters(maxChapters);
        setIsLocalStorage(isLocalStorage);
    }

    private void setMaxChapters(int maxChapters) {
        settings.edit().putInt(PREFS_MAX_CHAPTERS, maxChapters).apply();
    }

    public void addHated(int bookId) {

        String s = String.valueOf(bookId);

        //Retrieve the values
        Set<String> set = getHated();

        Log.i(TAG, "addHated: hay en la lista de odiados:" + set.size());

        if (set.contains(s)) {
            myLog.add("Este ya estaba entre los odiados: id=" + bookId, TAG);
            Log.i(TAG, "addHated: " + "borramos toda la lista y empezamos de nuevo." +
                    "Recomiendo cargar más libros");
        } else {
            set.add(s);
            settings.edit().putStringSet(PREFS_HATED_LIST, set).apply();
        }
    }

    public void addEnded(int bookId) {

        String s = String.valueOf(bookId);

        //Retrieve the values
        Set<String> set = getEnded();

        Log.i(TAG, "addEnded: hay en la lista de terminados:" + set.size());

        if (set.contains(s)) {
            myLog.add("Este ya estaba entre los leidos: id=" + bookId, TAG);
            Log.i(TAG, "addHated: " + "borramos toda la lista y empezamos de nuevo." +
                    "Recomiendo cargar más libros");
            resetSkipeables();
        } else {
            set.add(s);
            settings.edit().putStringSet(PREFS_ENDED_LIST, set).apply();
        }
    }

    @NonNull
    public Set<String> getHated() {
        return settings.getStringSet(PREFS_HATED_LIST, new HashSet<String>());
    }

    public Set<String> getEnded() {
        return settings.getStringSet(PREFS_ENDED_LIST, new HashSet<String>());
    }

    public Set<String> getSkipeables() {
        Set<String> uno = getHated();
        Set<String> dos = getEnded();

        uno.addAll(dos);
        return uno;
    }

    public void resetSkipeables() {
        Set<String> set = new HashSet<>();
        settings.edit().putStringSet(PREFS_ENDED_LIST, set).apply();
        settings.edit().putStringSet(PREFS_HATED_LIST, set).apply();
    }
}
