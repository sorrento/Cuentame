package com.stupidpeople.cuentame;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Milenko on 30/08/2016.
 */
public class parseHelper {

    final private static String tag = "PARSE";
    static String PINBOOK = "pinBook";


    public static void getIdsforbittenBooks(FindCallback<BookSummary> forbittenBooks) {
        ParseQuery<BookSummary> q = ParseQuery.getQuery(BookSummary.class);
        q.whereEqualTo("like", false);
        q.fromPin(PINBOOK);
        q.findInBackground(forbittenBooks);
    }

    public static void getBookSummary(final int iBook, final boolean local, final BookSumCallback bookCallback) {

        ParseQuery<BookSummary> q = ParseQuery.getQuery(BookSummary.class);
        q.whereEqualTo("libroId", iBook);
//        if (local) q.fromPin("pinBook");
        if (local) q.fromPin(PINBOOK);
        q.getFirstInBackground(new GetCallback<BookSummary>() {
            @Override
            public void done(BookSummary bookSummary, ParseException e) {

                if (e == null) {
                    bookCallback.onReceived(bookSummary);

                } else {

                    myLog.add("--- SUMMARY-- NO SE PUDO CARGAR DESDE LOCAL; CARGAREMOS DESDE WEB " + e.getLocalizedMessage(), parseHelper.tag);

                    getBookSummary(iBook, false, bookCallback); //todo momentaneo, porque parece que no lo guarmamos en local
//                    bookCallback.onError("Getting summary book:" + iBook + " local: " + local, e);
                }
            }
        });
    }

    public static void getChapters(final int iBook, int iChapter, int nChapters, boolean local, FindCallback<Chapter> cb) {
        final String fi = "nCapitulo";

        ParseQuery<Chapter> q = ParseQuery.getQuery(Chapter.class);
        q.whereEqualTo("nLibro", iBook);
        q.whereGreaterThanOrEqualTo(fi, iChapter);
        q.whereLessThanOrEqualTo(fi, iChapter + nChapters);
        q.setLimit(nChapters);
        q.orderByAscending(fi);
        if (local) {
            q.fromPin(PINBOOK);
            myLog.add("leido desde LOCAL", tag);
        } else {
            myLog.add("leido desde WEB", tag);
        }
        q.findInBackground(cb);
    }

    private static void getAndPinChapters(final int iBook, int iChapter, int nChapters, final TaskDoneCallback2 taskDoneCallback) {

        FindCallback<Chapter> cb = new FindCallback<Chapter>() {
            @Override
            public void done(List<Chapter> books, ParseException e) {
                if (e == null) {
                    myLog.add("--- Importados capitulos:" + books.size(), tag);

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
                    ParseObject.pinAllInBackground(PINBOOK, books, scb);

                } else {
                    taskDoneCallback.onError("getting lot of chapters ", e);
                }
            }
        };

        getChapters(iBook, iChapter, nChapters, false, cb);
    }

    public static void getChapter(int iBook, final int iChapter, final boolean local, final ChapterCB2 chapterCB) {

        ParseQuery<Chapter> q = ParseQuery.getQuery(Chapter.class);
        q.whereEqualTo("nLibro", iBook);
        q.whereEqualTo("nCapitulo", iChapter);
        if (local) q.fromPin(PINBOOK);
        q.getFirstInBackground(new GetCallback<Chapter>() {
            @Override
            public void done(Chapter chapter, ParseException e) {
                if (e == null) {
                    chapterCB.onDone(chapter);
                } else {
                    chapterCB.onError("Getting chapter " + iChapter + " from local: " + local, e);
                }

            }
        });
    }

    /**
     * Trata de traer el sumario desde local, luego desde web
     *
     * @param iBook
     * @param cb
     */
    public static void getBookSummary(final int iBook, final BookSumCallback cb) {
        getBookSummary(iBook, true, new BookSumCallback() {
            @Override
            public void onReceived(BookSummary bookSummary) {
                cb.onReceived(bookSummary);
            }

            @Override
            public void onError(String text, ParseException e) {
                //try online
                getBookSummary(iBook, false, new BookSumCallback() {
                    @Override
                    public void onReceived(BookSummary bookSummary) {
                        cb.onReceived(bookSummary);
                    }

                    @Override
                    public void onError(String text, ParseException e) {
                        cb.onError(text, e);
                    }
                });
            }
        });

    }

    /**
     * Guarda en local el libro completo
     */
    public static void importWholeBook(final int bookId, final TaskDoneCallback2 task) {

        removeBooksInInternalStorage(new TaskDoneCallback2() {
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
                                    int nChapters = bookSummary.nChapters();
                                    final int chapsPerPart = 500;
                                    final int nParts = (nChapters / chapsPerPart) + 1;
                                    final int[] iPinnedParts = {0};

                                    myLog.add("Libro " + bookId + " tiene " + nChapters + " que dividemos en pedazos de " + chapsPerPart +
                                            ", quedando " + nParts + " partes.", tag);

                                    for (int i = 0; i < nParts; i++) {
                                        final int iniChap = i * chapsPerPart;
                                        getAndPinChapters(bookId, iniChap, chapsPerPart, new TaskDoneCallback2() {
                                            @Override
                                            public void onDone() {
                                                myLog.add("DONE. Pinneados del libro " + bookId + " desde " + iniChap + " (+ " + chapsPerPart + ")", tag);
                                                iPinnedParts[0]++;
                                                if (iPinnedParts[0] == nParts) task.onDone();
                                            }

                                            @Override
                                            public void onError(String text, ParseException e) {
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
            public void onError(String text, ParseException e) {
                myLog.error(text, e);
            }
        });


    }

    private static void removeBooksInInternalStorage(final TaskDoneCallback2 task) {
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

    // Music

    /**
     * obtiene los ids de las bandas que no quiere oir
     *
     * @return
     */
    static ArrayList<Integer> getHatedBandsIds() {
        ArrayList<Integer> arr = new ArrayList();
        ParseQuery<BookContability> q = ParseQuery.getQuery(BookContability.class);
        q.whereEqualTo(BookContability.colIsHated, true);
        q.whereEqualTo(BookContability.colIsMusic, true);
        q.fromLocalDatastore();
        try {
            List<BookContability> sol = q.find();
            myLog.add("numero de bandas odiadas:" + sol.size(), tag);
            for (BookContability bookContability : sol) {
                myLog.add("  ej:" + bookContability.getBookId(), tag);
                arr.add(bookContability.getBookId());
            }
        } catch (ParseException e) {
            myLog.error("trayendo del local las bandas odiadas", e);
        }
        return arr;
    }

    /**
     * obtiene los ids de las bandas que no quiere oir
     *
     * @return
     */
    static ArrayList<Integer> getHatedBooksIds() {
        ArrayList<Integer> arr = new ArrayList();
        ParseQuery<BookContability> q = ParseQuery.getQuery(BookContability.class);
        q.whereEqualTo(BookContability.colIsMusic, false);
        q.whereEqualTo(BookContability.colIsHated, true);
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

}