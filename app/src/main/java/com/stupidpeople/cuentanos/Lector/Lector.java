package com.stupidpeople.cuentanos.Lector;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.stupidpeople.cuentanos.MainActivity;
import com.stupidpeople.cuentanos.book.ArrayCallback;
import com.stupidpeople.cuentanos.book.Book;
import com.stupidpeople.cuentanos.book.BookCallIdback;
import com.stupidpeople.cuentanos.book.ParseHelper;
import com.stupidpeople.cuentanos.diccionario.Definator;
import com.stupidpeople.cuentanos.utils.Preferences;
import com.stupidpeople.cuentanos.utils.myLog;

import java.util.List;

public class Lector {
    private final Preferences           prefs;
    private       LocalBroadcastManager localBroadcastManager;
    private       ReaderEvents          readerEvents;
    private       Voice                 voice;
    private       String                tag = "LEC";
    private       Book                  book;

    public Lector(Context context, final Preferences myPrefs) {

        prefs = myPrefs;
        localBroadcastManager = LocalBroadcastManager.getInstance(context);

        readerEvents = new ReaderEvents() {
            @Override
            public void bookAndVoiceReady() {
                if (prefs.getFirstTime()) {
                    welcomeMessage();
                    prefs.setFirstTime(false);
                }

                String newLanguage = book.getLanguage();
                if (!prefs.getLanguage().equals(newLanguage)) {
                    voice.setLanguage(newLanguage);
                    prefs.setLanguage(newLanguage);
                }

                localBroadcastManager.sendBroadcast(new Intent(MainActivity.Oreja.ACTION_OBTIENE_SUMMARY));
                voice.predefinedPhrases(TipoFrase.RETOMEMOS, true);
                speakCurrentChapter();
            }

            @Override
            public void voiceStartedSpeakChapter() {
                localBroadcastManager.sendBroadcast(new Intent(MainActivity.Oreja.ACTION_STARTED_READING_CHAPTER));
            }

            @Override
            public void voiceInterrupted() {
                localBroadcastManager.sendBroadcast(new Intent(MainActivity.Oreja.ACTION_STOPPED_READING_CHAP));
            }

            @Override
            public void voiceEndedReadingChapter() {
                //book.speakNextChapter();
                voice.speakChapter(book.getNextChapter(), false);
            }

            @Override
            public void bookEnded() {
                voice.predefinedPhrases(TipoFrase.FINALIZADO_LIBRO_ENTERO, true);
                prefs.addEnded(book.getBookId());
                accionCambiaDeLibro();
            }

            @Override
            public void error(String text, Exception e) {
                speakDeveloper(text);
                myLog.error(text, e);
            }
        };

        book = new Book(myPrefs.getReadingBookId(), myPrefs.getReadingChapterId(), myPrefs.isLocalStorage(),
                myPrefs, readerEvents);
        voice = new Voice(prefs.getLanguage(), context, readerEvents);
    }

    public void speakCurrentChapter() {
        voice.speakChapter(book.getCurrentChapter(), false);
    }

    private void welcomeMessage() {
        voice.predefinedPhrases(TipoFrase.WELCOME, true);
    }

    public void accionStopReading() {
        boolean speakingChapter = voice.isSpeakingChapter();

        myLog.add("me mandan a stopreading. estámhablando chapter?" + speakingChapter, tag);
        if (speakingChapter) {
            voice.shutUp();
        }
    }

    private void speakDeveloper(String s) {
        voice.speakDeveloperMsg(s);
    }

    public void leeDesdePrincipio() {
        voice.shutUp();
        voice.predefinedPhrases(TipoFrase.IR_A_PRINCIPIO, true);

        book.setCurrentChapterId(1);
    }

    public void shutUp() {
        voice.shutUp();
    }

    public void shutdownVoice() {
        voice.shutdown();
    }

    public boolean isReading() {
        return voice.isSpeakingChapter();
    }

    public void accionSaltaACapitulo(int bookId, final int chapId) {
        voice.predefinedPhrases(TipoFrase.VAMOS_ALLA, true);

        // Es mismo libro?
        if (bookId == book.getBookId()) {
            book.setCurrentChapterId(chapId);
            // Es otro libro
        } else {
            book = new Book(bookId, chapId, false, prefs, readerEvents);
        }
    }

    public void accionCambiaDeLibro() {
        voice.predefinedPhrases(TipoFrase.A_OTRO_LIBRO, true);
        ParseHelper.getRandomAllowedBookId(prefs.getSkipeables(), new BookCallIdback() {
            @Override
            public void onDone(int bookId, int nDisponibles) {
                prefs.setDisponibles(nDisponibles);
                book = new Book(bookId, -1, false, prefs, readerEvents);
            }
        });
    }

    public void speakMensaje(String s, Exception e) {
        voice.speakDeveloperMsg(s);
        myLog.add(s, tag);
    }

    public void definePalabrasDelChapter() {
        String text = getCurrentChapterText();

        Definator definator = new Definator(text, "ES", new ArrayCallback() {
            @Override
            public void onDone(List<String> arr, List<Double> bestScores) {
                myLog.add("llegaron definiciones:" + arr, tag);
                for (String s : arr) {
                    voice.speakDefinition(s);
                }
            }
        });
    }

    ////////////////////// OLD

    public String getStorageType() {
        return prefs.isLocalStorage() ? "LOCAL" : "WEB";
    }

    public void setLikedCurrentBook(boolean b) {
        if (!b) {
            prefs.addHated(book.getBookId());
        }
    }

    public Book getBook() {
        return book;
    }

    public String getCurrentChapterText() {
        return book.getCurrentChapter().getText();
    }
}
