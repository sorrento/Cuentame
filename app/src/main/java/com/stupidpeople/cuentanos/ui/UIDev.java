package com.stupidpeople.cuentanos.ui;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.stupidpeople.cuentanos.MainActivity;
import com.stupidpeople.cuentanos.R;
import com.stupidpeople.cuentanos.book.Book;
import com.stupidpeople.cuentanos.book.BookSummary;
import com.stupidpeople.cuentanos.book.Chapter;
import com.stupidpeople.cuentanos.utils.Preferences;
import com.stupidpeople.cuentanos.utils.myLog;

public class UIDev extends UiGeneric {

    private final Preferences      prefs;
    private       MainActivity     mainActivity;
    private       TextView         txtText;
    private       TextView         txtDesc;
    private       TextView         txtLocal;
    private       Button           btnPlayStop;
    /*   private Button   btnNext;
       private Button   btnLike;
       private Button   btnGo;*/
    private       EditText         edtChapter;
    private       EditText         edtBook;
    private       String           tag = "DEVUI";


    public UIDev(MainActivity mainActivity, ActionsInterface actionsInterface, Preferences prefs) {
        super(actionsInterface);
        this.mainActivity = mainActivity;
        this.prefs = prefs;
        getUiComponents();
    }

    @Override
    public void updateWithNewChapter(Chapter currentChapter, final Book book) {
        myLog.add("UI updatenewchapter: " + book.getCurrentChapterId() + " book:" +
                book.getBookName() + "(" + book.getBookId() + ")", tag);
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                edtChapter.setHint(Integer.toString(book.getCurrentChapterId()));
                txtLocal.setText(prefs.getStorageType());
                txtText.setText(book.getCurrentChapter().getText());
            }
        });
    }

    @Override
    public void updateBecauseStopped() {
        myLog.add("UI updated because stopped (cambiar el boton)", tag);
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myLog.add("UI updated because stopped (cambiar el boton). Ahora dentro de RUNUI", tag);
                btnPlayStop.setText("PLAY");
            }
        });
    }

    @Override
    public void updateBecauseStarted() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnPlayStop.setText("STOP");
            }
        });
    }

    public void updateBecauseNewBookLoaded(BookSummary bookSummary, final Book book) {

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtText.setText("...Waiting for chapter...");
                txtDesc.setText(book.getBookSummary().toString());
                txtLocal.setText(prefs.getStorageType());
                edtChapter.setHint(Integer.toString(prefs.getReadingChapterId()));
                edtBook.setHint(Integer.toString(prefs.getReadingBookId()));
            }
        });
    }

    private void getUiComponents() {
        txtText = mainActivity.findViewById(R.id.txtCurrentText);
        txtDesc = mainActivity.findViewById(R.id.txtCurrentDesc);
        txtLocal = mainActivity.findViewById(R.id.txtFromLocal);
        btnPlayStop = mainActivity.findViewById(R.id.btn_play_stop);
        /*btnNext = (Button) mainActivity.findViewById(R.id.btn_next);
        btnLike = (Button) mainActivity.findViewById(R.id.btn_like);
        btnGo = (Button) mainActivity.findViewById(R.id.btn_go);*/
        edtChapter = mainActivity.findViewById(R.id.etChapter);
        edtBook = mainActivity.findViewById(R.id.etbook);
    }

    public void apretadoGo() {
        int    nChap = Integer.parseInt(edtChapter.getText().toString());
        int    nBook;
        String s     = edtBook.getText().toString();
        nBook = Integer.parseInt(s.equals("") ? edtBook.getHint().toString() : edtBook.getText().toString());

        actionsInterface.apretadoGo(nBook, nChap);
    }

    public void updateStorageMode(final String storageType) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtLocal.setText(storageType);
            }
        });
    }
}
