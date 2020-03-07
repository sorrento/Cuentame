package com.stupidpeople.cuentanos.book;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.stupidpeople.cuentanos.Lector.GenericTaskInterface;
import com.stupidpeople.cuentanos.utils.Preferences;
import com.stupidpeople.cuentanos.utils.myLog;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

class ChapterService {
    private static final int      BATCH_SIZE = 10;
    private final        CSEvents mCsEvents;
    private              int      bookId, chapterId, maxChapters, lastIdInBuffer;
    private boolean        isLocalStorage;
    private Preferences    preferences;
    private Queue<Chapter> buffer;
    private String         tag = "CHSER";

    ChapterService(int bookId, int chapterId, int maxChapters, boolean isLocalStorage,
                   Preferences preferences, final CSEvents csEvents) {
        this.bookId = bookId;
        this.chapterId = chapterId;
        this.maxChapters = maxChapters;
        this.isLocalStorage = isLocalStorage;
        this.preferences = preferences;
        mCsEvents = csEvents;

        preferences.setBookParams(bookId, chapterId, maxChapters, isLocalStorage);

        if (chapterId == -1) {
            forceRandomChapter();
        } else {
            forceChapterId(chapterId);
        }
    }

    Chapter giveMeSame() {
        if (!buffer.isEmpty()) {
            Chapter chapter = buffer.peek();
            if (chapter.getChapterId() != chapterId)
                mCsEvents.error("No coincide el chapter aidí. " +
                        "el del chapter: " + chapter.getChapterId() +
                        " y el field del service es " + chapterId, null);

            return chapter;

        } else {
            mCsEvents.error("Buffer Estaba Vacio", null);
            return null;
        }
    }

    Chapter giveMeNext() {
        removeOne();
        if (chapterId == maxChapters) {
            mCsEvents.bookEnded();
            return null;
        }
        chapterId += 1;
        preferences.setReadingChapterId(chapterId);

        if (!isLocalStorage) {
            preferences.nPlayedFromWebAddOneTo();
            if (preferences.nPlayedFromWebSuperaUmbral()) {
                preferences.nPlayedFromWebReset();

                ParseHelper.importWholeBook(bookId, new GenericTaskInterface() {
                    @Override
                    public void onDone() {
                        isLocalStorage = true;
                        preferences.setIsLocalStorage(true);
                    }

                    @Override
                    public void onError(String text, Exception e) {
                        mCsEvents.error("Error importando el libro entero. Lo importaba por superar " +
                                "el umbral", e);
                    }
                });
            }
        }

        return giveMeSame();
    }

    void forceChapterId(final int chapterId) {
        this.chapterId = chapterId;
        preferences.setReadingChapterId(chapterId);

        cargaBufferNuevo();
    }

    private void forceRandomChapter() {
        chapterId = (new Random().nextInt(maxChapters - 10)) + 1;
        forceChapterId(chapterId);
    }

    private void cargaBufferNuevo() {
        reloadBuffer(chapterId, false, new GenericTaskInterface() {
            @Override
            public void onDone() {
                mCsEvents.serviceReady();
            }

            @Override
            public void onError(String text, Exception e) {
                mCsEvents.error("error al cargar un nuevo buffer" + text, e);
            }
        });
    }

    private void removeOne() {
        int size = buffer.size();
        myLog.add("remove one. antes teníamos " + size, tag);
        buffer.remove();
        if (size < 3 & !recargadoHastaElFinal()) {
            reloadBuffer();
        }


    }

    private boolean recargadoHastaElFinal() {
        return lastIdInBuffer == maxChapters;
    }

    private void reloadBuffer() {
        reloadBuffer(lastIdInBuffer + 1, true, new GenericTaskInterface() {
            @Override
            public void onDone() {
                //nada
            }

            @Override
            public void onError(String text, Exception e) {

            }
        });
    }

    private void reloadBuffer(final int chapterId, final boolean append, final GenericTaskInterface genericTaskInterface) {
        myLog.add("reloadBuffer, append? " + append, tag);

        ParseHelper.getChapters(bookId, chapterId, BATCH_SIZE, isLocalStorage, new FindCallback<Chapter>() {
            @Override
            public void done(List<Chapter> lista, ParseException e) {
                if (e == null) {
                    int size = lista.size();
                    myLog.add("traidos de parse local?" + isLocalStorage + " cuanots? " + size, tag);

                    if (size != 0) {
                        if (append) {
                            buffer.addAll(lista);
                        } else {
                            buffer = new LinkedList<>(lista);
                        }
                        lastIdInBuffer = size == BATCH_SIZE ? chapterId + BATCH_SIZE - 1 : maxChapters;
                        myLog.add("ahora la lista tiene" + buffer.size() +
                                " y el id del ultimodel buff es " + lastIdInBuffer, tag);
                        genericTaskInterface.onDone();
                    } else {
                        mCsEvents.error("Traté de capitulos de parse y no pude. ¿Era local? " + isLocalStorage, null);
                    }

                } else {
                    mCsEvents.error("Fallo en recargar el báfer", e);
                }
            }
        });
    }

    int getCurrentChapterId() {
        return chapterId;
    }
}
