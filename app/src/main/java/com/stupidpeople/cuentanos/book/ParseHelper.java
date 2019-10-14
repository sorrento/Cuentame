package com.stupidpeople.cuentanos.book;

import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.stupidpeople.cuentanos.Callbacks.BookSumCallback;
import com.stupidpeople.cuentanos.Lector.GenericTaskInterface;
import com.stupidpeople.cuentanos.utils.myLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by Milenko on 30/08/2016.
 */
public class ParseHelper {
    final private static String tag     = "PARSE";
    private static       String PINBOOK = "pinBook";

    public static void getRandomAllowedBookId(final Set<String> forbittenIds, final BookCallIdback cb) {

        myLog.add("*****Getting random, except the hated: " + forbittenIds, tag);
        ParseQuery<BookSummary> q = ParseQuery.getQuery(BookSummary.class);

        // get number of books
        q.whereNotEqualTo("isMusic", true);
        q.orderByDescending("libroId");
        q.getFirstInBackground(new GetCallback<BookSummary>() {
            @Override
            public void done(BookSummary bookSummary, ParseException e) {
                if (e == null) {
                    final int nBooks = bookSummary.getInt("libroId");
                    final int iBook  = randomNumber(forbittenIds, nBooks);

                    myLog.add("RANDOM: elegido el libro:" + iBook + "/" + nBooks, "get");
                    cb.onDone(iBook);
                } else {
                    myLog.add("EEROR en getting the maximun book" + e.getLocalizedMessage(), tag);
                }
            }
        });
    }

    private static void getAndPinChapters(final int iBook, int iChapter, int nChapters, final GenericTaskInterface taskDoneCallback) {

        FindCallback<Chapter> cb = new FindCallback<Chapter>() {
            @Override
            public void done(List<Chapter> chapters, ParseException e) {
                if (e == null) {
                    myLog.add("--- Importados capitulos:" + chapters.size(), tag);

                    SaveCallback scb = new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                taskDoneCallback.onDone();
                            } else {
                                taskDoneCallback.onError("Pinning lot of chapters", e);
                            }
                        }
                    };

                    // PIN them
                    ParseObject.pinAllInBackground(PINBOOK, chapters, scb);

                } else {
                    taskDoneCallback.onError("getting lot of chapters ", e);
                }
            }
        };

        getChapters(iBook, iChapter, nChapters, false, cb);
    }


    /**
     * Guarda en local el libro completo
     */
    public static void importWholeBook(final int bookId, final GenericTaskInterface task) {

        removeBooksInInternalStorage(new GenericTaskInterface() {
            @Override
            public void onDone() {

                // vemos cuantos capítulos tiene , para cargarlo por partes
                getBookSummary(bookId, false, new BookSumCallback() {
                    @Override
                    public void onReceived(final BookSummary bookSummary) {

                        //pin book summary
                        bookSummary.pinInBackground(PINBOOK, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    myLog.add("pinneado  summary", tag);

                                    // Dividimos en partes, para cargar independientemente
                                    int         nChapters    = bookSummary.getNChapters();
                                    final int   chapsPerPart = 500;
                                    final int   nParts       = (nChapters / chapsPerPart) + 1;
                                    final int[] iPinnedParts = {0};

                                    myLog.add("Libro " + bookId + " tiene " + nChapters + " que dividemos en pedazos de " + chapsPerPart +
                                            ", quedando " + nParts + " partes.", tag);

                                    for (int i = 0; i < nParts; i++) {
                                        final int iniChap = i * chapsPerPart;
                                        getAndPinChapters(bookId, iniChap, chapsPerPart, new GenericTaskInterface() {
                                            @Override
                                            public void onDone() {
                                                myLog.add("DONE. Pinneados del libro " + bookId + " desde " + iniChap + " (+ " + chapsPerPart + ")", tag);
                                                iPinnedParts[0]++;
                                                if (iPinnedParts[0] == nParts) task.onDone();
                                            }

                                            @Override
                                            public void onError(String text, Exception e) {
                                                task.onError("pinneando chapters desde " + iniChap + " (+ " + chapsPerPart + ")", e);
                                            }
                                        });
                                    }

                                } else {
                                    myLog.error("pinning sumamry iBook" + bookId, e);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String text, ParseException e) {
                        myLog.error(text, e);
                    }
                });

            }

            @Override
            public void onError(String text, Exception e) {
                myLog.error(text, e);
            }
        });
    }

    static void getBookSummary(final int iBook, final boolean local, final BookSumCallback bookSumCallback) {

        ParseQuery<BookSummary> q = ParseQuery.getQuery(BookSummary.class);
        q.whereEqualTo("libroId", iBook);
        if (local) q.fromPin(PINBOOK);
        q.getFirstInBackground(new GetCallback<BookSummary>() {
            @Override
            public void done(BookSummary bookSummary, ParseException e) {

                if (e == null) {
                    bookSumCallback.onReceived(bookSummary);
                } else {
                    myLog.add("libro" + iBook + "local=" + local + " Error obteniendo book summary:" + e.getLocalizedMessage(), ParseHelper.tag);
                    if (local) {
                        bookSumCallback.onError("No se pudo cargar el summary desde local", e);
                        //getBookSummary(iBook, false, bookSumCallback); //todo momentaneo, porque parece que no lo guarmamos en local
//                    bookSumCallback.onError("Getting summary book:" + iBook + " local: " + local, e);
                    }
                }
            }
        });
    }

    private static void removeBooksInInternalStorage(final GenericTaskInterface task) {
        ParseObject.unpinAllInBackground(PINBOOK, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.add("Unpinned all local books", tag);
                    task.onDone();
                } else {
                    task.onError("Removing internal storage books", e);
                }
            }
        });
    }

    private static int randomNumber(Set<String> hatedIds, int max) {
        ArrayList<String> sec = new ArrayList<>();
        int               res;

        for (int i = 1; i <= max; i++) {
            sec.add(String.valueOf(i));
        }
        sec.removeAll(hatedIds);

        if (sec.size() != 0) {
            Collections.shuffle(sec);
            res = Integer.parseInt(sec.get(0));
        } else {
            Log.i(tag, "randomNumber: Ya hemos agotado todos los libros, empezamos de nuevo");
            res = new Random().nextInt(max);
        }

        return res;
    }

    static void getChapters(final int iBook, int iChapter, int nChapters, boolean local, FindCallback<Chapter> cb) {
        final String fi          = "nCapitulo";
        int          finalChapId = iChapter + nChapters;

        myLog.add("Pidiendo capitulos desde el " + iChapter + " hasta el " + finalChapId, tag);

        ParseQuery<Chapter> q = ParseQuery.getQuery(Chapter.class);
        q.whereEqualTo("nLibro", iBook);
        q.whereGreaterThanOrEqualTo(fi, iChapter);
        q.whereLessThanOrEqualTo(fi, finalChapId);
        q.setLimit(nChapters);
        q.orderByAscending(fi);
        if (local) {
            q.fromPin(PINBOOK);
            myLog.add("leyendo  desde LOCAL", tag);
            //todo  Si falla local avisar por voz y
        } else {
            myLog.add("leyendo desde WEB", tag);
        }
        q.findInBackground(cb);
    }

    /////////// OLD
    private static ArrayList<Integer> getHatedOrFinishedBooksId() {
        ArrayList<Integer>          arr = new ArrayList<>();
        ParseQuery<BookContability> q   = ParseQuery.getQuery(BookContability.class);
        q.whereEqualTo(BookContability.colIsMusic, false);
        q.whereEqualTo(BookContability.colIsHated, true);
        q.whereEqualTo(BookContability.colIsFinished, true);
        q.fromLocalDatastore();
        try {
            List<BookContability> sol = q.find();
            myLog.add("numero de libros  odiadas:" + sol.size(), tag);
            for (BookContability bookContability : sol) {
                myLog.add("  ej:" + bookContability.getBookId(), tag);
                arr.add(bookContability.getBookId());
            }
        } catch (ParseException e) {
            myLog.error("trayendo del local los libros odiados", e);
        }
        return arr;
    }

    public static void Diccionario(ArrayList<String> palabras, FindCallback<palabraDiccionario> err) {

        ParseQuery<palabraDiccionario> q = ParseQuery.getQuery(palabraDiccionario.class);
        q.whereContainedIn("word", palabras);
        q.orderByAscending("r");
        q.findInBackground(err);
    }
}