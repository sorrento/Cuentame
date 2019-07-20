package com.stupidpeople.cuentanos.book;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.stupidpeople.cuentanos.Callbacks.BookSumCallback;
import com.stupidpeople.cuentanos.Lector.GenericTaskInterface;
import com.stupidpeople.cuentanos.Lector.ReaderEvents;
import com.stupidpeople.cuentanos.utils.Preferences;
import com.stupidpeople.cuentanos.utils.myLog;

import java.util.List;
import java.util.Random;

public class Book implements BookInterface {

    public static final int         N_READED_WEB = 6;
    private final       Preferences myPrefs;
    private             BookSummary bookSummary  = null;
    private             int         bookId;
    private             int         chapterId;
    private             String      language;

    private int     mNChapters;
    private Memoria memoria;
    private boolean isLocalStorage;
    private String  tag = "BOOK";

    /**
     * @param bookId
     * @param chapterId    si se pone -1 se le asignará un capitulo random
     * @param localStorage
     */
    public Book(final int bookId, final int chapterId, boolean localStorage, final Preferences myPrefs, final GenericTaskInterface genericTaskInterface) {
        this.bookId = bookId;
        this.chapterId = chapterId;
        this.isLocalStorage = localStorage;
        this.myPrefs = myPrefs;


        BookSumCallback cbSummary = new BookSumCallback() {
            @Override
            public void onReceived(BookSummary bookSum) {
                bookSummary = bookSum;

                // si no tenía chapter id, ponemos uno random
                if (chapterId == -1) {
                    int chapterId1 = (new Random().nextInt(bookSummary.getNChapters() - 10)) + 1;
                    myLog.add("el chapter elegido ramdom es:" + chapterId1, tag);
                    Book.this.chapterId = chapterId1;
                }

                memoria = new Memoria(Book.this.chapterId, bookId, isLocalStorage);
                saveParamsInPrefs(myPrefs);
                genericTaskInterface.onDone();
            }

            @Override
            public void onError(String text, ParseException e) {
                genericTaskInterface.onError(text, e);
            }
        };

        ParseHelper.getBookSummary(bookId, localStorage, cbSummary);
    }

    public Book(Preferences myPrefs, final ReaderEvents readerEvents) {

        this(myPrefs.getReadingBookId(), myPrefs.getReadingChapterId(), myPrefs.isLocalStorage(),
                myPrefs, new GenericTaskInterface() {
                    @Override
                    public void onDone() {
                        readerEvents.bookReady();
                    }

                    @Override
                    public void onError(String text, ParseException e) {

                    }
                });
    }

    private void saveParamsInPrefs(Preferences myPrefs) {
        myPrefs.setReadingBookId(bookId);
        myPrefs.setReadingChapterId(chapterId);
        myPrefs.setLanguage(bookSummary.getLanguage());
        myPrefs.setIsLocalStorage(isLocalStorage);
    }


    private void loadChapters(int nChapters, int mIni, final GenericTaskInterface genericTaskInterface) {
        ParseHelper.getChapters(bookId, mIni, nChapters, isLocalStorage, new FindCallback<Chapter>() {
            @Override
            public void done(List<Chapter> objects, ParseException e) {
                memoria = new Memoria(objects, bookId, isLocalStorage);
                genericTaskInterface.onDone();
            }
        });
    }

    public boolean isLastChapter() {
        return getCurrentChapter().getChapterId() == getNChapters();
    }

    private int getNChapters() {
        return mNChapters;
    }

    public void avanzaChapter() {
        chapterId += 1;
        memoria.removeOne();
        if (!isLocalStorage) {
            myPrefs.nPlayedFromWebAddOneTo();
            if (myPrefs.nPlayedFromWebGet() > N_READED_WEB) {
                loadEntireBook(new GenericTaskInterface() {
                    @Override
                    public void onDone() {
                        myPrefs.setIsLocalStorage(true);
                    }

                    @Override
                    public void onError(String text, ParseException e) {

                    }
                });
            }
        }
    }

    public String getStorageType() {
        return isLocalStorage ? "LOCAL" : "WEB";
    }

    public void setLiked(boolean b) {
        // todo ponerlo en la contabilidad local
    }

    public void recargaMemoria(GenericTaskInterface genericTaskInterface) {
        memoria.recargaCola(genericTaskInterface);
    }

    public boolean memoryIsEmpty() {
        return memoria.isEmpty();
    }

    @Override
    public String getBookName() {
        return bookSummary.getTitle();
    }

    @Override
    public String getBookNameFake() {
        return bookSummary.fakeTitle();
    }

    @Override
    public String getBookAuthor() {
        return bookSummary.getAuthor();
    }

    @Override
    public String getBookAuthorFake() {
        return bookSummary.getFakeAuthor();
    }

    @Override
    public int getBookId() {
        return bookId;
    }

    @Override
    public Chapter getCurrentChapter() {
        return memoria.getOneWithoutRemove();
    }

    @Override
    public int getCurrentChapterId() {
        /*int chapterId;
        if (!memoryIsEmpty()) {
            chapterId = getCurrentChapter().getChapterId();
        } else {
            chapterId = this.chapterId;
        }*/
        return chapterId;
    }

    public void setCurrentChapterId(int i) {
        chapterId = i;
        memoria = new Memoria(chapterId, bookId, isLocalStorage);
    }

    @Override
    public void loadEntireBook(GenericTaskInterface genericTaskInterface) {
        memoria.setLocalStorage(true);
        isLocalStorage = true;
        ParseHelper.importWholeBook(bookSummary.getId(), genericTaskInterface);
    }

    @Override
    public void loadChapters(int ini, int nChapters) {
        ParseHelper.getChapters(bookId, ini, nChapters, isLocalStorage, new FindCallback<Chapter>() {
            @Override
            public void done(List<Chapter> objects, ParseException e) {
                memoria.addList(objects);
            }
        });
    }

    @Override
    public void speakChapter(boolean interrupt) {
        Chapter ch;
        try {
            ch = memoria.getOneWithoutRemove();
            speakChapter(ch, interrupt);
        } catch (Exception e) {
            //TODO hacer que suba la exceptcion
        }
    }

    @Override
    public BookSummary getBookSummary() {
        return bookSummary;
    }

    @Override
    public void prepareToRead(final int bookId, int chapterId, final GenericTaskInterface genericTaskInterface) {
        ParseHelper.getChapters(bookId, chapterId, 10, isLocalStorage, new FindCallback<Chapter>() {
            @Override
            public void done(List<Chapter> objects, ParseException e) {
                memoria = new Memoria(objects, bookId, isLocalStorage);
                genericTaskInterface.onDone();
            }
        });

    }

    private void speakChapter(Chapter chapter, boolean interrupting) {
        //  Speak.speak(chapter.getText(), interrupting, chapter.getUtterance(), t1);
    }

    public boolean isLocalStorage() {
        return isLocalStorage;
    }
}
