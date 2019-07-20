package com.stupidpeople.cuentanos;

import android.support.annotation.NonNull;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.stupidpeople.cuentanos.Lector.GenericTaskInterface;
import com.stupidpeople.cuentanos.book.ParseHelper;
import com.stupidpeople.cuentanos.utils.myLog;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Memoria {

    private final int            bookId;
    private       int            mLastChapterIdLoaded;
    private       Queue<Chapter> buffer;
    private       boolean        recargadoHastaElFinal = false;
    private       boolean        isLocalStorage;
    private       int            batchSize             = 10;
    private       String         tag                   = "MEM";

    public Memoria(List<Chapter> lista, int iBook, boolean isLocalStorage) {
        buffer = new LinkedList<>(lista);
        bookId = iBook;
        this.isLocalStorage = isLocalStorage;
        setLastChapterId(lista);
    }

    public Memoria(int chapterId, int bookId, boolean isLocalStorage) {
        buffer = new LinkedList<>();
        this.bookId = bookId;
        mLastChapterIdLoaded = chapterId - 1;
        this.isLocalStorage = isLocalStorage;
    }

    private void setLastChapterId(@NonNull List<Chapter> lista) {
        Chapter chapter = lista.get(lista.size() - 1);
        mLastChapterIdLoaded = chapter.getChapterId();
    }

    public Chapter getOneWithoutRemove() {
        if (buffer.size() == 0) {
            recargaCola();
        }
        return buffer.peek();
    }

    public void addList(List<Chapter> lista) {
        buffer.addAll(lista);
        setLastChapterId(lista);
    }

    public void removeOne() {
        int size = buffer.size();
        myLog.add("remove one. antes teníamos " + size + "recargado hasta el  final:" +
                recargadoHastaElFinal, tag);
        buffer.remove();
        if (size < 3 & !recargadoHastaElFinal) {
            recargaCola();
        }
    }

    private void recargaCola() {
        recargaCola(new GenericTaskInterface() {
            @Override
            public void onDone() {

            }

            @Override
            public void onError(String text, ParseException e) {

            }
        });
    }

    public void recargaCola(final GenericTaskInterface genericTaskInterface) {
        //completamos la cola con otra carga

        myLog.add("recargacola", tag);
        FindCallback<Chapter> cb = new FindCallback<Chapter>() {
            @Override
            public void done(List<Chapter> objects, ParseException e) {
                if (e == null) {
                    int size = objects.size();
                    myLog.add("traidos de parse local?" + isLocalStorage + " cuanots? " + size, tag);
                    if (size > 0) {
                        if (size < batchSize) recargadoHastaElFinal = true;
                        addList(objects);
                        myLog.add("ahora la lista tiene" + buffer.size(), tag);
                        genericTaskInterface.onDone();
                    } else {
                        recargadoHastaElFinal = true;
                    }
                } else {
                    ExceptionManager.ExceptionManager("Error al cargar capitulos", e);
                }
            }
        };

        ParseHelper.getChapters(bookId, mLastChapterIdLoaded + 1, batchSize, isLocalStorage,
                cb);
    }

    public boolean isEmpty() {
        return buffer.size() == 0;
    }

    public void resetea() {
        buffer.clear();
    }

    public void setLocalStorage(boolean b) {
        isLocalStorage = b;
    }
}
