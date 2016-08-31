package com.stupidpeople.cuentame;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.session.MediaSession;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_MODE = "mode";
    private static final String PREFS_LAST_CHAPTER = "last chapter";
    private static final String PREFS_LAST_BOOK = "last book";
    private static final String PREFS_FIRST_TIME = "first time";
    private static final String COURTESY = "courtesy";
    private static final String LIKE = "popo";
    private static final String PLAY = "play";
    private static final String NEXT = "next";
    final String JUMPEDIN = "nJumpedIn";
    final String parseClassLeidos = "leidos";
    final private String samsungEngine = "com.samsung.SMT";
    TextToSpeech t1;
    boolean entireBookMode = false;
    private String tag = "mhp";
    private Chapter currentChapter;
    private List<Chapter> chaptersPreLoaded;
    private TextView txtText;
    private TextView txtDesc;
    private Button btnPlayStop;
    private boolean interrupted = false;
    private int iBuffer = 0;
    private int lastBook;
    private int lastChapter;
    private SharedPreferences settings;
    private int nextQueueMode;
    private Button btnNext;
    private Button btnLike;
    private NotificationManager mNotificationManager;
    private MediaSession mMediaSession;
    private BroadcastReceiver eventsReceiver;
    //    private int nChapters = 0;
    private BookSummary currentBook;
    private String tag2 = "ACT";

    public void setCurrentChapter(final Chapter currentChapter) {
        this.currentChapter = currentChapter;

        settings.edit().putInt(PREFS_LAST_BOOK, currentChapter.getBookId()).commit();
        settings.edit().putInt(PREFS_LAST_CHAPTER, currentChapter.getChapterId()).commit();


        this.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        txtText.setText(currentChapter.getText());
                        txtDesc.setText(currentChapter.toString());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(eventsReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myLog.initialize();

        // Restore preferences
        settings = getPreferences(MODE_PRIVATE);

        entireBookMode = settings.getBoolean(PREFS_MODE, false);

        lastBook = settings.getInt(PREFS_LAST_BOOK, 1);
        lastChapter = settings.getInt(PREFS_LAST_CHAPTER, 1);

        myLog.add(tag, "RECUPERANDO: mode entireebook:" + entireBookMode + "lastbook: " + lastBook + " lastchap: " + lastChapter);

        txtText = (TextView) findViewById(R.id.txtCurrentText);
        txtDesc = (TextView) findViewById(R.id.txtCurrentDesc);
        btnPlayStop = (Button) findViewById(R.id.btn_play_stop);
        btnNext = (Button) findViewById(R.id.btn_next);
        btnLike = (Button) findViewById(R.id.btn_like);

// service ///////////////
//        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
//        intent.setAction( MediaPlayerService.ACTION_PLAY );
//        startService( intent );


        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        enginePackageName	String: The package name for the synthesis engine (e.g. "com.svox.pico")

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if (status != TextToSpeech.ERROR) {
                    String engine = getBestEngine();

                    t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
//                            myLog.add(tag, "Status de inicialización:_" + status);
                            if (status != TextToSpeech.ERROR) {
                                t1.setOnUtteranceProgressListener(new uListener());

                                if (settings.getBoolean(PREFS_FIRST_TIME, true)) welcomeMessage();

                                myLog.add(tag2, "empezando la actividad, tramemos 10");

                                if (!entireBookMode) {
                                    getRandomChaptersAndPlay(10);
                                } else {
                                    courtesyMessage("A ver donde me quedé... ");
                                    getChapterAndPlay(lastBook, lastChapter, 10);
                                }

                                //todo poner en español si es necesario

                                //                    t1.setLanguage(Locale.UK);
                                //                    fixed Locale spanish = new Locale("es", "ES"); c.locale = spanish; this works THA
                                //                     Locale spanish = new Locale("es", "ES"); c.locale = spanish; this works THA
                            }
                        }
                    }, engine);


                }
            }
        });

        //Register receiver
        eventsReceiver = new EventsReceiver();
        IntentFilter filter = new IntentFilter(LIKE);
        filter.addAction(NEXT);
        filter.addAction(PLAY);
        this.registerReceiver(eventsReceiver, filter);

//TODO ver otros sintentizadores buenos
        //oner un dialog en vez de n toast

    }

    private void showNotification() {
        Intent like = new Intent(LIKE);
        PendingIntent likePendingIntent = PendingIntent.getBroadcast(this, 1, like, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent play = new Intent(PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 1, play, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent next = new Intent(NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 1, next, PendingIntent.FLAG_UPDATE_CURRENT);

        int iconPlayPause = t1.isSpeaking() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        Notification notification = new NotificationCompat.Builder(this)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(android.R.drawable.ic_media_play)
                // Add media control buttons that invoke intents in your media service
                .addAction(android.R.drawable.ic_media_previous, "Previous", likePendingIntent) // #0

                .addAction(iconPlayPause, "Pause", playPendingIntent)  // #1
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)     // #2
//                        // Apply the media style template
                .setStyle(new NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(1 /* #1: pause button */)
//                        .setMediaSession( mMediaSession.getSessionToken()))
                )
                .setContentTitle(currentBook.fakeTitle())
                .setContentText(currentBook.fakeAuthor())
                .setSubText(currentChapter.getChapterId() + "/" + currentBook.nChapters())
                .setProgress(currentBook.nChapters(), currentChapter.getChapterId(), false)
                .setLargeIcon(currentBook.getImageBitmap())
                .setTicker(currentBook.fakeTitle() + "\n" + currentBook.fakeAuthor())
                .build();
// mId allows you to update the notification later on.
        mNotificationManager.notify(1, notification);
    }

    private void courtesyMessage(String s) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            t1.speak(s, TextToSpeech.QUEUE_FLUSH, null, COURTESY);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, COURTESY);
            t1.speak(s, TextToSpeech.QUEUE_FLUSH, map);
        }
        nextQueueMode = TextToSpeech.QUEUE_ADD;
    }

    private void welcomeMessage() {
        settings.edit().putBoolean(PREFS_FIRST_TIME, false).commit();
        courtesyMessage("Hola, te voy a contar algunas de las historias que más me gustan. Espero que a ti también." +
                " Si no te gusta como suena mi voz, instala un sintetizador nuevo. Busca en el google play poniendo TTS." +
                " Vamos a ver...");
    }

    private void getRandomChaptersAndPlay(final int chapters) {

        if (isOnline()) {

            // get number of books
            ParseQuery<ParseObject> q = ParseQuery.getQuery("librosSum");
            q.orderByDescending("libroId");
            q.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {

                        final int nBooks = object.getInt("libroId");
                        final int iBook = new Random().nextInt(nBooks + 1);

                        BookSumCallback callback = new BookSumCallback() {
                            @Override
                            public void onReceived(BookSummary bookSummary) {
                                final int iChapter = new Random().nextInt(bookSummary.nChapters() + 1);

                                BookContability.incrementJumpedInBook(bookSummary);

                                getChapterAndPlay(iBook, iChapter, chapters);

                            }

                            @Override
                            public void onError(String text, ParseException e) {

                            }
                        };

                        getBook(iBook, callback);


                    } else {
                        myLog.add(tag, "EEROR en getting the maximun book" + e.getLocalizedMessage());
                    }
                }
            });

        } else {
            courtesyMessage("Tengo un problema, no puedo recordar más historias. Si conectas internet, seguro que me refresca la memoria.");
        }
    }

    private void getBook(final int iBook, final BookSumCallback cb) {
        ParseQuery<BookSummary> q2 = ParseQuery.getQuery(BookSummary.class);
        q2.whereEqualTo("libroId", iBook);
        q2.getFirstInBackground(new GetCallback<BookSummary>() {
            @Override
            public void done(BookSummary book, ParseException e) {
                if (e == null) {
                    currentBook = book;
                    cb.onReceived(book);
//                    nChapters = object.getInt("nCapitulos");
//                    myLog.add(tag, "    TIENE CHAPTERS:" + nChapters);

                } else {
                    myLog.add(tag, "___ERRROR getting the maximum chapter  for book:" + iBook + " | " + e.getLocalizedMessage());
                }
            }
        });
    }

    /**
     * Si está en modo lectura de todo el libro, lo busca en local, sino, en internet
     *
     * @param iBook
     * @param iChapter
     * @param nChapters
     */
    private void getChapterAndPlay(final int iBook, final int iChapter, final int nChapters) {
        final String fi = "nCapitulo";

        BookSumCallback cb = new BookSumCallback() {
            @Override
            public void onReceived(BookSummary book) {
                ParseQuery<Chapter> q = ParseQuery.getQuery(Chapter.class);
                q.whereEqualTo("nLibro", iBook);
                q.whereGreaterThanOrEqualTo(fi, iChapter);
                q.whereLessThan(fi, iChapter + nChapters);
                if (entireBookMode) q.fromLocalDatastore();
                q.orderByAscending(fi);
                q.findInBackground(new FindCallback<Chapter>() {
                    @Override
                    public void done(List<Chapter> books, ParseException e) {
                        if (e == null) {
                            myLog.add(tag2, "--- Traidos capitulos:" + books.size() + "desde local?" + entireBookMode);

                            if (books.size() > 0) {
                                setCurrentChapter(books.get(0));
                                iBuffer = 0;
                                chaptersPreLoaded = books;
                                playCurrentChapter();

                            } else { //Hemos llegado al final
                                if (entireBookMode) {
                                    courtesyMessage("...fin. Espero que te haya gustado tanto como a mi.");
                                    BookContability.setFinishedBook(currentBook);
                                } else {
                                    getRandomChaptersAndPlay(10);
                                }
                            }

                        } else {
                            myLog.add(tag, "errer---" + e.getLocalizedMessage());
                        }
                    }
                });
            }

            @Override
            public void onError(String text, ParseException e) {

            }
        };

        getBook(iBook, cb);

    }

    /**
     * Empieza el siguiente del buffer, y se si ha acabado, trae los siguients
     */

    private void playNext() {
        //TODO traer chapters antes de que acabe de leer
        //        boolean inMemory = false;
        myLog.add(tag, "en playNext, ibffer=" + iBuffer);

        if (chaptersPreLoaded == null || iBuffer + 1 == chaptersPreLoaded.size()) { //Traer los siguientes
            getChapterAndPlay(currentChapter.getBookId(), currentChapter.getChapterId() + 1, 10);

        } else {

            iBuffer++;
            final Chapter chapter = chaptersPreLoaded.get(iBuffer);
            myLog.add(tag2, "Ahora ibffer=" + iBuffer + " y el chaper es" + chapter.shortDescription());

            setCurrentChapter(chapter);
            playCurrentChapter();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        myLog.add(tag2, "*********Resumuinf");
        if (!t1.isSpeaking() && currentChapter != null) {
            playCurrentChapter();
        }
    }

    private void playCurrentChapter() {

        if (currentChapter == null) {
            myLog.add(tag, "..Playnext porque play current pero no hay ninguno");
            playNext();

        } else {
            myLog.add(tag, "\n" + currentChapter.shortestDescription() + " MANDADO");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                t1.speak(currentChapter.getProcessedText(), nextQueueMode, null, currentChapter.shortDescription());
            } else {
                HashMap<String, String> map = new HashMap<>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, currentChapter.shortDescription());
                t1.speak(currentChapter.getProcessedText(), nextQueueMode, map);
            }
        }
    }

    ////////////////////


    private String getBestEngine() {
        String engine;
        boolean isgood = false;
        List<TextToSpeech.EngineInfo> engines = t1.getEngines();

        for (TextToSpeech.EngineInfo engineinfo : engines) {
            if (engineinfo.name.equals(samsungEngine)) {
                isgood = true;
                break;
            }
        }

        if (isgood) {
            Toast.makeText(MainActivity.this, "Detectado tts samsung", Toast.LENGTH_SHORT).show();
            engine = samsungEngine;
        } else {
            Toast.makeText(MainActivity.this, "Instale un sintenizador bueno, la calidad no será aceptable. Por ejemplo, tts samsung", Toast.LENGTH_SHORT).show();
            engine = t1.getDefaultEngine();
        }
        return engine;
    }


    public void onClickPlayStop(View view) {

        final boolean speaking = t1.isSpeaking();


        myLog.add(tag, "*********PRESSED Play/Stop . Speaking?" + speaking);

        //STOP
        if (speaking) {
            btnPlayStop.setText("PLAY");
            interrupted = true;
            t1.stop();
            myLog.add(tag, "        ***after pressing. Speaking?" + t1.isSpeaking());

            //PLAY
        } else {
            btnPlayStop.setText("STOP");

            playCurrentChapter();
        }
    }

    public void onClickNext(View view) {
        interrupted = true;
        myLog.add(tag, "*********PRESSED NEXT");
        courtesyMessage("Vaya, no te ha gustado. Veamos otra cosa...");

        if (!btnLike.isEnabled()) btnLike.setEnabled(true);

        //block button 2 secons
        btnNext.setEnabled(false);
        btnNext.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnNext.setEnabled(true);
            }
        }, 2000);

        entireBookMode = false;

        myLog.add(tag, "..Playnext porque cluckedNext");
        getRandomChaptersAndPlay(10);
    }

    public void onClickLike(View view) {
        myLog.add(tag, "*********PRESSED LIKE");

        btnLike.setEnabled(false);


        if (isOnline()) {
            final int bookId = currentChapter.getBookId();
            courtesyMessage("Bueno, ya que te gusta el relato, veamo si recuerdo cómo empezaba...");
            getChapterAndPlay(bookId, 1, 10);
            entireBookMode = true;
            interrupted = true;

            parseHelper.importWholeBook(bookId, new TaskDoneCallback2() {
                @Override
                public void onDone() {
                    myLog.add(tag, "DONE. book " + bookId + " load in internal storage");
                }

                @Override
                public void onError(String text, ParseException e) {
                    myLog.error("fallo en importar los libros |" + text, e);
                }
            });

        } else {
            courtesyMessage("Te lo contaría desde el principio, pero necesitamos conección a internet para ayudarme a recordar. Seguiré por donde iba...");
            playCurrentChapter();
        }
    }


    /**
     * Checks if we have internet connection     *
     *
     * @return
     */
    public boolean isOnline() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean b = netInfo != null && netInfo.isConnectedOrConnecting();
        myLog.add(tag, "Checking connectivity: " + b);

        return b;
    }



    class uListener extends UtteranceProgressListener {
        @Override
        public void onStart(String utteranceId) {
            myLog.add(tag, utteranceId + ": START SPEAKING");
//                                        Toast.makeText(MainActivity.this, utteranceId, Toast.LENGTH_SHORT).show();

            showNotification();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    btnPlayStop.setText("STOP");
                }
            });
        }

        @Override
        public void onDone(String utteranceId) {
            myLog.add(tag, utteranceId + ": END. forced?" + interrupted);
            // if (utteranceId.equals(msgs)) return;

            if (utteranceId.equals(COURTESY)) {
                nextQueueMode = TextToSpeech.QUEUE_FLUSH;
            } else {
                if (interrupted) {
                    myLog.add(tag, "se ha interrumpido, no ponemos otro");
                    showNotification();
                    interrupted = false;

                    //termino de contar la el chapter
                } else {
                    myLog.add(tag, "Ha finalizado, " + utteranceId + " por lo que ponemos el siguiente");
                    playNext();
                }
            }
        }

        @Override
        public void onError(String utteranceId) {
            myLog.add(tag, "***ERRORen utterance: id = " + utteranceId);

        }
    }

    private class EventsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action;
            try {
                action = intent.getAction();
                //Refresh
                switch (action) {
                    case LIKE:
//                    boolean forced = intent.getBooleanExtra("forced", true);
                        myLog.add(tag, "apretado el botoónprv");
                        onClickLike(null);
                        break;
                    case PLAY:
                        onClickPlayStop(null);
                        break;
                    case NEXT:
                        onClickNext(null);
                        break;
                }
            } catch (Exception e) {
            }
        }

    }

}
