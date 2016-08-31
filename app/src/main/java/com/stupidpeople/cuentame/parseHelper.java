package com.stupidpeople.cuentame;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by Milenko on 30/08/2016.
 */
public class parseHelper {

    final private static String tag = "PARSE";

    /**
     * Retrieve and stores in local DB
     *
     * @param iBook
     * @param iChapter
     * @param nChapters
     */
    static void importChapters(final int iBook, final int iChapter, final int nChapters, final TaskDoneCallback2 taskDoneCallback) {


        final TaskDoneCallback2 taskGetAndPinSummary = new TaskDoneCallback2() {
            @Override
            public void onDone() {
                getAndPinChapters(iBook, iChapter, nChapters, taskDoneCallback);
            }

            @Override
            public void onError(String text, ParseException e) {
                taskDoneCallback.onError(text, e);
            }
        };

        getAndPinSummary(iBook, taskGetAndPinSummary);


    }

    private static void errorInParse(String s, ParseException e) {
        myLog.add(tag, s + " " + e.getLocalizedMessage());
        //TODO informar al usuario y ver qué hacer
    }

    public static void getBookSummary(final int iBook, final boolean local, final BookSumCallback bookCallback) {

        ParseQuery<BookSummary> q = ParseQuery.getQuery(BookSummary.class);
        q.whereEqualTo("libroId", iBook);
        if (local) q.fromLocalDatastore();
        q.getFirstInBackground(new GetCallback<BookSummary>() {
            @Override
            public void done(BookSummary bookSummary, ParseException e) {

                if (e == null) {
                    bookCallback.onReceived(bookSummary);

                } else {
                    bookCallback.onError("Getting summary book:" + iBook + " local: " + local, e);
                }
            }
        });
    }

    private static void getAndPinSummary(final int iBook, final TaskDoneCallback2 taskDoneCallback) {
        getBookSummary(iBook, false, new BookSumCallback() {
            @Override
            public void onReceived(BookSummary bookSummary) {
                //now pin
                bookSummary.pinInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            taskDoneCallback.onDone();
                        } else {
                            taskDoneCallback.onError("pinning sumamry iBook" + iBook, e);
                        }

                    }
                });
            }

            @Override
            public void onError(String text, ParseException e) {
                taskDoneCallback.onError(text, e);
            }
        });
    }

    private static void getAndPinChapters(final int iBook, int iChapter, int nChapters, final TaskDoneCallback2 taskDoneCallback) {
        final String fi = "nCapitulo";

        ParseQuery<Chapter> q = ParseQuery.getQuery(Chapter.class);
        q.whereEqualTo("nLibro", iBook);
        q.whereGreaterThanOrEqualTo(fi, iChapter);
        q.whereLessThan(fi, iChapter + nChapters);
        q.setLimit(nChapters);
        q.orderByAscending(fi);
        q.findInBackground(new FindCallback<Chapter>() {
                               @Override
                               public void done(List<Chapter> books, ParseException e) {
                                   if (e == null) {
                                       myLog.add(tag, "--- Importados capitulos:" + books.size());

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
                                       ParseObject.pinAllInBackground(books, scb);

                                   } else {
                                       taskDoneCallback.onError("getting lot of chapters ", e);
                                   }
                               }
                           }

        );

    }

    public static void getChapter(int iBook, final int iChapter, final boolean local, final ChapterCB2 chapterCB) {

        ParseQuery<Chapter> q = ParseQuery.getQuery(Chapter.class);
        q.whereEqualTo("nLibro", iBook);
        q.whereEqualTo("nCapitulo", iChapter);
        if (local) q.fromLocalDatastore();
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

                        getAndPinSummary(bookId, new TaskDoneCallback2() {
                            @Override
                            public void onDone() {
                                myLog.add(tag, "DONE. Summary book pinned " + bookId);

                                // Dividimos en partes, para cargar independientemente
                                int nChapters = bookSummary.nChapters();
                                final int chapsPerPart = 500;
                                final int nParts = (int) Math.ceil(nChapters / chapsPerPart);
                                final int[] iPinnedParts = {0};

                                myLog.add(tag, "Libro " + bookId + " tiene " + nChapters + " que dividemos en pedazos de " + chapsPerPart +
                                        ", quedando " + nParts + " partes.");

                                for (int i = 0; i < nParts; i++) {
                                    final int iniChap = i * chapsPerPart;
                                    getAndPinChapters(bookId, iniChap, chapsPerPart, new TaskDoneCallback2() {
                                        @Override
                                        public void onDone() {
                                            myLog.add(tag, "DONE. Pinneados del libro " + bookId + " desde " + iniChap + " (+ " + chapsPerPart + ")");
                                            iPinnedParts[0]++;
                                            if (iPinnedParts[0] == nParts) task.onDone();
                                        }

                                        @Override
                                        public void onError(String text, ParseException e) {
                                            task.onError("pinneando chapters desde " + iniChap + " (+ " + chapsPerPart + ")", e);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onError(String text, ParseException e) {
                                task.onError(text, e);
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
        ParseObject.unpinAllInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    task.onDone();
                } else {
                    task.onError("Removing internal storage books", e);
                }
            }
        });
    }
}