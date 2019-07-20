package com.stupidpeople.cuentanos.Lector;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.ParseException;
import com.stupidpeople.cuentanos.MainActivity;
import com.stupidpeople.cuentanos.book.Book;
import com.stupidpeople.cuentanos.book.BookCallIdback;
import com.stupidpeople.cuentanos.book.ParseHelper;
import com.stupidpeople.cuentanos.utils.Preferences;
import com.stupidpeople.cuentanos.utils.myLog;

public class Lector implements LectorInterface {
    private final Context     mContext;
    private final Preferences prefs;
    LocalBroadcastManager localBroadcastManager;
    private ReaderEvents readerEvents;
    private Voice        voice;
    private String       tag = "LEC";
    //    private       BookContability mContabilidad;
    private Book         book;

    public Lector(Context context, final Preferences myPrefs) {

        mContext = context;
        prefs = myPrefs;
        localBroadcastManager = LocalBroadcastManager.getInstance(context);

        readerEvents = new ReaderEvents() {
            @Override
            public void bookAndVoiceReady() {
                if (prefs.getFirstTime()) {
                    welcomeMessage();
                    prefs.setFirstTime(false);
                }

                localBroadcastManager.sendBroadcast(new Intent(MainActivity.Oreja.ACTION_OBTIENE_SUMMARY));
                resumeReadingNewSession();
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
                book.avanzaChapter();
                myPrefs.setReadingChapterId(book.getCurrentChapterId());
                accionLeeLoQueToca();
            }
        };

        book = new Book(myPrefs, readerEvents);
        voice = new Voice(prefs.getLanguage(), context, readerEvents);
    }

    public void welcomeMessage() {
        voice.predefinedPhrases(TipoFrase.WELCOME, true);
    }

    @Override
    public void resumeReadingNewSession() {
        voice.predefinedPhrases(TipoFrase.RETOMEMOS, true);
        accionLeeLoQueToca();
    }


    @Override
    public void accionLeeLoQueToca() {
        myLog.add("Leeloquetoca", tag);

        if (book.memoryIsEmpty()) {
            book.recargaMemoria(new GenericTaskInterface() {
                @Override
                public void onDone() {
                    accionLeeLoQueToca();
                }

                @Override
                public void onError(String text, ParseException e) {

                }
            });
        } else {
            voice.speakChapter(book.getCurrentChapter(), false);
        }
    }

    @Override
    public void accionStopReading() {
        boolean speakingChapter = voice.isSpeakingChapter();

        myLog.add("me mandan a stopreading. estámhablando chapter?" + speakingChapter, tag);
        if (speakingChapter) {
            voice.shutUp();
        }
    }

    private void ChapterReadedEvent() {
        if (!book.isLastChapter()) {
            prefs.setReadingChapterId(book.getCurrentChapterId());
            if (!prefs.isLocalStorage()) {
                if (prefs.nPlayedFromWebGet() > 30) {
                    myLog.add("Como llevas 30 chaps en modeo web, pasamos al modo local", tag);
                    ParseHelper.importWholeBook(book.getBookId(), new GenericTaskInterface() {
                        @Override
                        public void onDone() {
                            changeStorageMode(true);
                            book.avanzaChapter();
                            accionLeeLoQueToca();
                        }

                        @Override
                        public void onError(String text, ParseException e) {
                            speakDeveloper("Error importando el libro a local por haber escuchado muchoen web ");
                        }
                    });
                }
            }
            book.avanzaChapter();
            accionLeeLoQueToca();
        } else {
            lastChapterEvent();
        }
    }

    private void speakDeveloper(String s) {
        voice.speakDeveloperMsg(s);
    }

    private void lastChapterEvent() {
        if (prefs.getStartedFromBeginning()) {
            voice.predefinedPhrases(TipoFrase.FINALIZADO_LIBRO_ENTERO, true);
            ParseHelper.setBookAsReaded(book);

            accionCambiaDeLibro();
        } else {
            startFromBeginning();
        }
    }

    private void startFromBeginning() {
        voice.predefinedPhrases(TipoFrase.FINALIZADO_LIBRO_REEMPEZAR, true);
        prefs.setStartedFromBeginning(true);
    }

    public void shutUp() {
        voice.shutUp();
    }

    @Override
    public void shutdownVoice() {
        voice.shutdown();
    }

    @Override
    public boolean isReading() {
        return voice.isSpeakingChapter();
    }

    public void leeDesdePrincipio() {
        voice.shutUp();
        voice.predefinedPhrases(TipoFrase.IR_A_PRINCIPIO, true);
        book.setCurrentChapterId(1);

        if (book.isLocalStorage()) {
            accionLeeLoQueToca();
        } else {
            book.loadEntireBook(new GenericTaskInterface() {
                                    @Override
                                    public void onDone() {
                                        changeStorageMode(true);
                                        accionLeeLoQueToca();
                                    }

                                    @Override
                                    public void onError(String text, ParseException e) {

                                    }
                                }
            );
        }

    }

    public void accionCambiaDeLibro() {
        voice.predefinedPhrases(TipoFrase.A_OTRO_LIBRO, true);
        //  mContabilidad.marcaHated(book.getBookId());

        ParseHelper.getRandomBookIdAllowed(prefs, new BookCallIdback() {
                    @Override
                    public void onDone(int bookId) {
                        ponLibroYlee(bookId, -1, false);
                    }
                }
        );
    }

    public void accionSaltaACapitulo(int bookId, final int chapId) {
        voice.predefinedPhrases(TipoFrase.VAMOS_ALLA, true);

        // Es mismo libro?
        if (bookId == book.getBookId()) {
            if (prefs.isLocalStorage()) {
                book.setCurrentChapterId(chapId);
                accionLeeLoQueToca();
            } else {
                book.loadEntireBook(new GenericTaskInterface() {
                    @Override
                    public void onDone() {
                        changeStorageMode(true);
                        book.setCurrentChapterId(chapId);
                        accionLeeLoQueToca();
                    }

                    @Override
                    public void onError(String text, ParseException e) {
                        speakDeveloper("no pude saltar a capitulo del mismo libro, error al cargar en local" +
                                text);
                    }
                });
            }
            // Es otro libro
        } else {
            ponLibroYlee(bookId, chapId, false);
        }
    }

    private void changeStorageMode(boolean toLocalMode) {
        prefs.setIsLocalStorage(toLocalMode);
        localBroadcastManager.sendBroadcast(new Intent(MainActivity.Oreja.ACTION_CHANGE_STORAGE_MODE));
    }

    private void ponLibroYlee(int bookId, int chapId, boolean localStorage) {
        prefs.nPlayedFromWebReset();
        book = new Book(bookId, chapId, localStorage, prefs, new GenericTaskInterface() {
            @Override
            public void onDone() {
                localBroadcastManager.sendBroadcast(new Intent(MainActivity.Oreja.ACTION_OBTIENE_SUMMARY));
                accionLeeLoQueToca();
            }

            @Override
            public void onError(String text, ParseException e) {
                speakDeveloper("No pude cambiar a otro libro " + text);
            }
        });
    }

    public String getStorageType() {
        return prefs.isLocalStorage() ? "LOCAL" : "WEB";
    }

    public void setLikedCurrentBook(boolean b) {
        book.setLiked(b);
    }

    public Book getBook() {
        return book;
    }
}
