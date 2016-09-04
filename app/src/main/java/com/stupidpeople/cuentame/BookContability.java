package com.stupidpeople.cuentame;

import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

interface BookContabilityCallBack {

    void onDone(BookContability bookContability);

    void onError(String s, ParseException e);
}

/**
 * Created by halatm on 31/08/2016.
 * lleva la contabilidad del usuario, se le gusta el libro, si lo terminó, si ha caido en él varias
 * veces por random (pensado para internal storage)
 */


@ParseClassName("bookContability")
public class BookContability extends ParseObject {

    public static String colIsMusic = "isMusic";
    static String colNJumpedIn = "nJumpedIn";
    static String colIsHated = "isHated";
    private static String tag = "BC";
    private static String colBookId = "bookId";
    private static String colIsFinished = "IsFinished";

    public BookContability() {
    }

    /**
     * save internally that we have finished the book
     *
     * @param bookSummary
     */
    static void setFinishedBook(final BookSummary bookSummary) {
        getOrCreateBookContability(bookSummary.getId(), bookSummary.isMusic(), new BookContabilityCallBack() {
            @Override
            public void onDone(BookContability bookContability) {
                bookContability.setFinished(true);
                bookContability.pinInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            myLog.add(tag, "pinneado book contability (book finished");
                        }
                    }
                });
            }

            @Override
            public void onError(String s, ParseException e) {
                myLog.error("Adding to finished books", e);
            }
        });


        /*checkIfLeidoExists(bookSummary.getId(), new GetCallback<BookContability>() {
            @Override
            public void done(BookContability object, ParseException e) {
                ifobject.setFinishedBook();
            }
        })*/

    }

    static void setHatedBook(final BookSummary bookSummary) {
        getOrCreateBookContability(bookSummary.getId(), bookSummary.isMusic(), new BookContabilityCallBack() {
            @Override
            public void onDone(BookContability bookContability) {
                bookContability.setHated(true);
                bookContability.pinInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            myLog.add(tag, "pinneado book contability (book hated");
                        }
                    }
                });
            }

            @Override
            public void onError(String s, ParseException e) {
                myLog.error("Adding to hated books", e);
            }
        });
    }

    static void incrementJumpedInBook(final BookSummary bookSummary) {
        getOrCreateBookContability(bookSummary.getId(), bookSummary.isMusic(), new BookContabilityCallBack() {
            @Override
            public void onDone(BookContability bookContability) {
                bookContability.incrementJumpedIn();

                if (bookContability.getJumpedIn() > 3) {
                    bookContability.setHated(true);
                }

                bookContability.pinInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            myLog.add(tag, "pinneado book contability (JumpedIn");
                        }
                    }
                });
            }

            @Override
            public void onError(String s, ParseException e) {
                myLog.error("incrementing jumping in: " + s, e);
            }
        });
    }

    // si está en local, lo trae, sino , lo crea
    private static void getOrCreateBookContability(final int bookId, final boolean isMusic, final BookContabilityCallBack cb) {
        // veamos si ya está
        ParseQuery<BookContability> q = ParseQuery.getQuery(BookContability.class);
        q.whereEqualTo("bookId", bookId);
        q.fromLocalDatastore();
        q.getFirstInBackground(new GetCallback<BookContability>() {
            @Override
            public void done(BookContability object, ParseException e) {
                if (e == null) {
                    cb.onDone(object);
                } else {
                    cb.onError("getting book contability. let create it", e);

                    final BookContability po = new BookContability();
//                    final BookContability po = BookContability.create(BookContability.class);

                    po.put(colBookId, bookId);
                    po.put(colNJumpedIn, 1);
                    po.put(colIsFinished, false);
                    po.put(colIsHated, false);
                    po.put(colIsMusic, isMusic);


                    po.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                myLog.add(tag, "created and pinned a leidos object");
                                cb.onDone(po);
                            } else {
                                cb.onError("creating a leidos object", e);
                            }
                        }
                    });

                }
            }
        });


    }

    private int getJumpedIn() {
        return getInt(colNJumpedIn);
    }

    private void incrementJumpedIn() {
        increment(colNJumpedIn);
    }

    private void setHated(boolean b) {
        myLog.add(tag, "set hate this book=" + b);
        put(colIsHated, b);
    }

    private void setFinished(boolean b) {
        put(colIsFinished, b);
    }

    public Integer getBookId() {
        return getInt(colBookId);
    }
}