package com.stupidpeople.cuentame;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static android.support.v4.app.NotificationCompat.BigTextStyle;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_MODE = "mode";
    private static final String PREFS_MODE_MUSIC = "music";
    private static final String PREFS_LAST_CHAPTER = "last chapter";
    private static final String PREFS_LAST_BOOK = "last book";
    private static final String PREFS_FIRST_TIME = "first time";

    private static final String COURTESY = "courtesy";
    private static final String LIKE = "like";
    private static final String PLAYPAUSE = "playpause";
    private static final String NEXT = "next";
    private static final String STOP = "stop";
    private static final String MUSICBOOK = "musicBook";
    private static final String SHARETEXT = "shareText";
    private static final String SHAREAUDIO = "shareAudio";
    private static final int OFFSET = 2;

    final private String samsungEngine = "com.samsung.SMT";
    TextToSpeech t1;
    boolean entireBookMode = false;
    boolean musicMode = false;
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
    private int nextQueueMode = TextToSpeech.QUEUE_ADD;
    private Button btnNext;
    private Button btnLike;
    private NotificationManager mNotificationManager;
    private BroadcastReceiver eventsReceiver;
    private BookSummary currentBook;
    private String tag2 = "ACT";

    // Shake
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private List<BookSummary> allBandsSum = null;
    private boolean isShowingLyricsNotification = false;
    private String tagW = "WAS";
    //    private String destFileName;
    private TextToSpeech t2;
    private String uttsavingFile = "savingFile";
    private String[] dividedLyrics;
    private int iVersoIni;
    private int iVersoEnd;
    private String versoIni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myLog.initialize();

//        destFileName = (Environment.getExternalStorageDirectory()
//                .getAbsolutePath() + "/Download") + "/" + "tts_file.wav";

        // Restore preferences
        settings = getPreferences(MODE_PRIVATE);
        entireBookMode = settings.getBoolean(PREFS_MODE, false);
        musicMode = settings.getBoolean(PREFS_MODE_MUSIC, false);
        lastBook = settings.getInt(PREFS_LAST_BOOK, 1);
        lastChapter = settings.getInt(PREFS_LAST_CHAPTER, 1);

        myLog.add("RECUPERANDO: mode entirebook: " + entireBookMode + " lastbook: " + lastBook + " lastchap: " + lastChapter, tag);

        txtText = (TextView) findViewById(R.id.txtCurrentText);
        txtDesc = (TextView) findViewById(R.id.txtCurrentDesc);
        btnPlayStop = (Button) findViewById(R.id.btn_play_stop);
        btnNext = (Button) findViewById(R.id.btn_next);
        btnLike = (Button) findViewById(R.id.btn_like);

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                Toast.makeText(MainActivity.this, "shake:" + count, Toast.LENGTH_SHORT).show();
                onClickPlayStop(null);
            }
        });
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);


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
                            if (status != TextToSpeech.ERROR) {
                                setSpeakLanguage("ES", t1);

                                t1.setOnUtteranceProgressListener(new uListener());

                                if (settings.getBoolean(PREFS_FIRST_TIME, true)) welcomeMessage();
                                else courtesyMessage("A ver donde me quedé... ", true);

                                myLog.add("empezando la actividad, tramemos 10", tag2);


                                getBulkAndPlay(lastBook, lastChapter, 10, entireBookMode);

                            }
                        }
                    }, engine);


                }
            }
        });

        //Register receiver o notification player
        eventsReceiver = new EventsReceiver();
        IntentFilter filter = new IntentFilter(LIKE);
        filter.addAction(NEXT);
        filter.addAction(PLAYPAUSE);
        filter.addAction(LIKE);
        filter.addAction(STOP);
//        filter.addAction(MUSICBOOK);
//        filter.addAction(SHARETEXT);
//        filter.addAction(SHAREAUDIO);
        this.registerReceiver(eventsReceiver, filter);
    }


    private void setSpeakLanguage(String lan, TextToSpeech t) {
        myLog.add("Setting Language:" + lan, tag);
        //todo elegir el motor y las voces una sola vez, al inicio

        switch (lan) {
            case "ES":
                Locale spanish = new Locale("es", "ES");
                t.setLanguage(spanish);
                break;

            case "EN":
                t.setLanguage(Locale.US);

                //Select voice
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Set<Voice> voices = t.getVoices();
                    for (Voice voice : voices) {
                        // if (voice.getName().equals("en-US-SMTl01")) {
                        if (voice.getQuality() == 400 && voice.getLocale() == Locale.US) {
                            myLog.add("voice set to: " + voice.toString(), tag);
                            t.setVoice(voice);
                            break;
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myLog.add("*********Resumuinf", tag2);


//        if (!t1.isSpeaking() && currentChapter != null) {
//            playCurrentChapter();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(eventsReceiver);
        settings.edit().putBoolean(PREFS_MODE, entireBookMode).commit();
        settings.edit().putBoolean(PREFS_MODE_MUSIC, musicMode).commit();

        removeLyricNotification();
        t1.shutdown();
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
    }

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

    private void showMediaNotification() {

        Intent like = new Intent(LIKE);
        PendingIntent likePendingIntent = PendingIntent.getBroadcast(this, 1, like, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent play = new Intent(PLAYPAUSE);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 1, play, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent next = new Intent(NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 1, next, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stop = new Intent(STOP);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 1, stop, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent musicBook = new Intent(MUSICBOOK);
        PendingIntent musicBookPendingIntent = PendingIntent.getBroadcast(this, 1, musicBook, PendingIntent.FLAG_UPDATE_CURRENT);

        int iconPlayPause = t1.isSpeaking() ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
//        int iconMusicBook = musicMode ? R.drawable.ic_book : R.drawable.ic_music_note_white_24dp;
//        int iconMusicBook = R.drawable.ic_book;

        String title = currentChapter.isSong() ? currentChapter.getBookName() : currentBook.fakeTitle();
        String content = currentChapter.isSong() ? currentChapter.getAuthor() : currentBook.fakeAuthor();

        Notification notification = new NotificationCompat.Builder(this)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_logo_white)
                        // Add media control buttons that invoke intents in your media service
                .addAction(R.drawable.ic_skip_previous_white_24dp, "Previous", likePendingIntent) // #0
                .addAction(iconPlayPause, "Pause", playPendingIntent)  // #1
                .addAction(R.drawable.ic_skip_next_white_24dp, "Next", nextPendingIntent)     // #2
//                .addAction(iconMusicBook, "MusicBook", musicBookPendingIntent)     // #3
////                        // Apply the media style template
                .setStyle(new NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(1 /* #1: pause button */)
//                        .setMediaSession( mMediaSession.getSessionToken()))
                )
                .setShowWhen(false)
                .setDeleteIntent(stopPendingIntent)
                .setContentTitle(title)
                .setContentText(content)
                .setSubText(currentChapter.getChapterId() + "/" + currentBook.nChapters())
                .setProgress(currentBook.nChapters(), currentChapter.getChapterId(), false)
                .setLargeIcon(currentBook.getImageBitmap())
                .setTicker(currentBook.fakeTitle() + "\n" + currentBook.fakeAuthor())

                .build();
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, notification);
    }

    private void courtesyMessage(String s, boolean interrupt) {
        myLog.add("Courtesy: " + s, tag);
        speak(s, interrupt, COURTESY);

    }

    private void welcomeMessage() {
        settings.edit().putBoolean(PREFS_FIRST_TIME, false).commit();
        courtesyMessage("Hola, te voy a contar algunas de las historias que más me gustan. Espero que a ti también." +
                " Si no te gusta como suena mi voz, instala un sintetizador nuevo. Busca en el google play poniendo TTS." +
                " Vamos a ver...", true);
    }

    //Play

    /**
     * Elige (on line) un summary book, o una banda
     *
     * @param musicMode si es modo musica, elege una banda
     * @param hatedIds
     * @param cb
     */
    private void getRandomBookSummary(boolean musicMode, ArrayList<Integer> hatedIds, final BookSumCallback cb) {

        myLog.add("*****Getting random, except the hated: " + hatedIds, tag);
        ParseQuery<BookSummary> q = ParseQuery.getQuery(BookSummary.class);

        if (musicMode) { // get all bands

            // If already loaded
            if (allBandsSum != null) {
                getRandomBand(cb);

            } else {
                q.whereEqualTo("isMusic", true);
                q.findInBackground(new FindCallback<BookSummary>() {
                    @Override
                    public void done(List<BookSummary> bandsSum, ParseException e) {
                        if (e == null) {
                            allBandsSum = bandsSum;

                            getRandomBand(cb);

                        } else {
                            cb.onError("trayendo los summ de bandas", e);
                        }
                    }
                });
            }


        } else { //not music, but book

            // get number of books
            q.whereNotEqualTo("isMusic", true);
            q.whereNotContainedIn("libroId", hatedIds);
            q.orderByDescending("libroId");

            q.getFirstInBackground(new GetCallback<BookSummary>() {
                @Override
                public void done(BookSummary bookSummary, ParseException e) {
                    if (e == null) {

                        final int nBooks = bookSummary.getInt("libroId");
                        final int iBook = new Random().nextInt(nBooks + 1);

                        myLog.add("RANDOM: elegido el libro:" + iBook + "/" + nBooks, tag2);
                        parseHelper.getBookSummary(iBook, cb);

                    } else {
                        myLog.add("EEROR en getting the maximun book" + e.getLocalizedMessage(), tag);
                    }
                }
            });

        }
    }


    private void getRandomChaptersAndPlay(final int chapters, ArrayList<Integer> hatedIds) {

        entireBookMode = false;
        if (isOnline()) {

            BookSumCallback cb = new BookSumCallback() {
                @Override
                public void onReceived(BookSummary bookSummary) {
                    //Get the chapters( or songs):

                    currentBook = bookSummary;

                    final int iChapter = new Random().nextInt(bookSummary.nChapters() + 1);

//                    BookContability.incrementJumpedInBook(bookSummary); TODO MARCAR libro por visita

                    getBulkAndPlay(bookSummary.getId(), iChapter, chapters, false);
                }

                @Override
                public void onError(String text, ParseException e) {
                    myLog.error(text, e);
                }
            };

            getRandomBookSummary(musicMode, hatedIds, cb);


        } else {
            courtesyMessage("Tengo un problema, no puedo recordar más historias. Si conectas internet, seguro que me refresca la memoria.", false);
        }
    }

    /**
     * Lee de parse (local o no) y guarda en buffer (array) nchpater
     *
     * @param iBook
     * @param iChapter
     * @param bufferSize
     */
    private void getBulkAndPlay(final int iBook, final int iChapter, final int bufferSize, final boolean local) {

        BookSumCallback cb = new BookSumCallback() {
            @Override
            public void onReceived(BookSummary bookSummary) {
                currentBook = bookSummary;

                parseHelper.getChapters(iBook, iChapter, bufferSize, local, new FindCallback<Chapter>() {
                    @Override
                    public void done(List<Chapter> chapters, ParseException e) {
                        if (e == null) {
                            myLog.add("--- Traidos capitulos: " + chapters.size() + " desde local?" + local, tag);

                            if (chapters.size() > 0) {
                                setCurrentChapter(chapters.get(0));
                                iBuffer = 0;
                                chaptersPreLoaded = chapters;
                                myLog.add("==Play current by: getbugkandplay", tag);
                                playCurrentChapter();

                            } else {
                                if (currentChapter == null) {
                                    //algun error así que cargamos el libro de nuevo
                                    myLog.add(tag, "no hay current chapter, cargamos desde web el libro");

                                    getBulkAndPlay(iBook, iChapter, 10, false);
                                } else {
                                    //Hemos llegado al final
                                    if (currentBook.nChapters() == currentChapter.getChapterId()) {
                                        courtesyMessage("Fin. Espero que te haya gustado tanto como a mi.", false);
                                        if (entireBookMode) {

                                            entireBookMode = false;
//                                        BookContability.setFinishedBook(currentBook); lo ponenoms como "Lo odio"
                                            marcarLeidoYponerNuevoRandom();

                                        } else {
                                            courtesyMessage("Ah, pero no lo habías oído desde el principio.", false);
                                            onClickLike(null);
                                        }
                                    } else {
                                        myLog.add("como no hemos cargado chaps, ponemos random", tag);
                                        getRandomChaptersAndPlay(10, new ArrayList<Integer>());
                                    }
                                }


                            }

                        } else {
                            myLog.add("errer---" + e.getLocalizedMessage(), tag);
                        }
                    }
                });

            }

            @Override
            public void onError(String text, ParseException e) {
                myLog.error(text, e);
            }
        };

        parseHelper.getBookSummary(iBook, local, cb);
    }

    /**
     * Empieza el siguiente del buffer, y se si ha acabado, trae los siguients
     */
    private void playNext() {

        if (chaptersPreLoaded == null || iBuffer + 1 == chaptersPreLoaded.size()) { //Traer los siguientes
            if (currentChapter == null) {
                getRandomChaptersAndPlay(10, new ArrayList<Integer>());
            } else {
                getBulkAndPlay(currentChapter.getBookId(), currentChapter.getChapterId() + 1, 10, entireBookMode);
            }


        } else {

            iBuffer++;
            final Chapter chapter = chaptersPreLoaded.get(iBuffer);
            setCurrentChapter(chapter);
            myLog.add("==Play current by: buffer+1 (playnext)", "mhp");
            playCurrentChapter();
        }
    }

    private void playCurrentChapter() {

        if (currentChapter == null) {
            myLog.add("..Playnext porque play current pero no hay ninguno", tag);
            playNext();

        } else {

            myLog.add("----> MANDADO: " + iBuffer + " | " + currentChapter.shortestDescription(), tag);
            speak(currentChapter.getProcessedText(), false, currentChapter.shortDescription());
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
            courtesyMessage("Instale un sintenizador bueno, la calidad no será aceptable. Por ejemplo, el sintenizador de Samsung", false);
            engine = t1.getDefaultEngine();
        }
        return engine;
    }

    public void onClickPlayStop(View view) {

        final boolean speaking = t1.isSpeaking();


        myLog.add("*********PRESSED Play/Stop . Speaking?" + speaking, tag);

        //STOP
        if (speaking) {
            btnPlayStop.setText("PLAY");
            interrupted = true;
            t1.stop();
            myLog.add("        ***after pressing. Speaking?" + t1.isSpeaking(), tag);

            //PLAY
        } else {
            btnPlayStop.setText("STOP");

            myLog.add("==Play current by: pressed PLAY", "mhp");
            playCurrentChapter();
        }
    }

    public void onClickNext(View view) {
//        interrupted = true;
        entireBookMode = false;
        myLog.add("*********PRESSED NEXT", tag);

        //block button 2 secons
        btnNext.setEnabled(false);
        btnNext.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnNext.setEnabled(true);
            }
        }, 2000);

        myLog.add("..Playnext porque cluckedNext", tag);

        if (!btnLike.isEnabled()) btnLike.setEnabled(true);

        if (musicMode) {
            //avanzamos en el buffer y luego saltanos
            playNext();
        } else {
            courtesyMessage("Vaya, no te ha gustado. Veamos otra cosa...", true);

            marcarLeidoYponerNuevoRandom();

        }


    }

    private void marcarLeidoYponerNuevoRandom() {
        currentBook.setLike(false);
        currentBook.pinInBackground(parseHelper.PINBOOK, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.add("Marcado el libro lo odio:" + currentBook.getTitle(), tag);
                } else {
                    myLog.error("poniendo no me gusta el libro", e);
                }

            }
        });


        parseHelper.getIdsforbittenBooks(new FindCallback<BookSummary>() {
            @Override
            public void done(List<BookSummary> books, ParseException e) {
                ArrayList<Integer> ids = new ArrayList<>();
                for (BookSummary book : books) ids.add(book.getId());

                getRandomChaptersAndPlay(10, ids);

            }
        });
    }

    public void onClickLike(View view) {

        myLog.add("*********PRESSED LIKE", tag);

        btnLike.setEnabled(false);


        if (isOnline()) {
            final int bookId = currentChapter.getBookId();
//            nextQueueMode = TextToSpeech.QUEUE_FLUSH;
//            interrupted = true;
            courtesyMessage("Bueno, ya que te gusta el relato, veamo si recuerdo cómo empezaba...", true);
//            nextQueueMode = TextToSpeech.QUEUE_ADD;
            getBulkAndPlay(bookId, 1, 10, false);


            parseHelper.importWholeBook(bookId, new TaskDoneCallback2() {
                @Override
                public void onDone() {
                    myLog.add("DONE. book " + bookId + " loaded in internal storage", tag);
                    entireBookMode = true;
                }

                @Override
                public void onError(String text, ParseException e) {
                    myLog.error("fallo en importar los libros |" + text, e);
                }
            });

        } else {
            courtesyMessage("Te lo contaría desde el principio, pero necesitamos conección a internet para ayudarme a recordar. Seguiré por donde iba...", false);
            myLog.add("==Play current by: pressedlike, but not online", "mhp");
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
        myLog.add("Checking connectivity: " + b, tag);

        return b;
    }

    private void removeLyricNotification() {
        mNotificationManager.cancel(2);
    }

    private void speak(String s, boolean interrupting, String utterance) {

        int queueMode = interrupting ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
        interrupted = interrupting;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            t1.speak(s, queueMode, null, utterance);

        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterance);
            t1.speak(s, queueMode, map);
        }

    }

    private void getRandomBand(BookSumCallback cb) {
        Random random = new Random();
        cb.onReceived(allBandsSum.get(random.nextInt(allBandsSum.size())));
    }

    private void showLyricsNotification() {

        Intent shareText = new Intent(SHARETEXT);
        PendingIntent pendingIntentText = PendingIntent.getBroadcast(this, 1, shareText, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent shareAudio = new Intent(SHAREAUDIO);
        PendingIntent pendingIntentAudio = PendingIntent.getBroadcast(this, 1, shareAudio, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(android.R.drawable.ic_media_play)
                        // Add media control buttons that invoke intents in your media service
//                .addAction(android.R.drawable.ic_media_rew, "Previous", likePendingIntent) //todo poner boton paa wasap

                .setStyle(new BigTextStyle()
                        .bigText(currentChapter.getText())
                        .setBigContentTitle(currentBook.getTitle()))
                .setShowWhen(false)
//                .setDeleteIntent(stopPendingIntent)
                .setContentTitle(currentChapter.getBookName())
                .setContentText(currentChapter.getText())
//                .setSubText(currentChapter.getChapterId() + "/" + currentBook.nChapters())
//                .setLargeIcon(currentBook.getImageBitmap())
                .setTicker(currentBook.getTitle() + "\n" + currentBook.fakeAuthor())
                        // actions
                .addAction(R.drawable.ic_book, "TEXT", pendingIntentText)
                .addAction(R.drawable.ic_book, "AUDIO", pendingIntentAudio)
                .build();
        // mId allows you to update the notification later on.
        mNotificationManager.notify(2, notification);
    }

    private void sendWhatsappIntent(Intent whatsappIntent) {
        myLog.add("sending wasap intent", tagW);


//        //Quitamos el lock
//        Window w = this.getWindow();
//        w.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
////        w.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//
        // cerramos las notificaciones
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        MainActivity.this.sendBroadcast(it);

        // ponemos la actividad en el frente
        Intent intentHome = new Intent(getApplicationContext(), MainActivity.class);
        intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentHome);

        //enviar a Whatsapp
        try {
            this.startActivity(whatsappIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            myLog.add("error lanzando la actividad wasap: " + e.getLocalizedMessage(), tagW);
        }


    }

    ///////// // OLD

    private void chooseVersos(final StringCallback cb) {
        dividedLyrics = currentChapter.getDividedLyrics();

        // TEST on wheel
        View outerView = LayoutInflater.from(this).inflate(R.layout.wheel_view, null);
        WheelView wv = (WheelView) outerView.findViewById(R.id.wheel_view_wv);
        wv.setOffset(2);
        wv.setItems(Arrays.asList(dividedLyrics));
        wv.setSelection(3);
        wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                myLog.add("[Dialog]selectedIndex: " + selectedIndex + ", item: " + item, tag);
                iVersoIni = selectedIndex;
                versoIni = item;
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Verso inicial")
                .setView(outerView)
                .setPositiveButton("OK", null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        choseLastVerso(cb);
                    }
                })
                .show();
    }

    private void choseLastVerso(final StringCallback cb) {
        String[] newArray = Arrays.copyOfRange(dividedLyrics, iVersoIni, dividedLyrics.length);

        // TEST on wheel
        View outerView = LayoutInflater.from(this).inflate(R.layout.wheel_view, null);
        WheelView wv = (WheelView) outerView.findViewById(R.id.wheel_view_wv);
        wv.setOffset(OFFSET);
        wv.setItems(Arrays.asList(newArray));
        wv.setSelection(3);
        wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                myLog.add("[Dialog]selectedIndex: " + selectedIndex + ", item: " + item, tag);
                iVersoEnd = selectedIndex + iVersoIni;
            }
        });

        new AlertDialog.Builder(this)
                .setTitle(versoIni + "...")
                .setView(outerView)
                .setPositiveButton("OK", null).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                String[] versos = currentChapter.getVersos(iVersoIni - OFFSET, iVersoEnd - OFFSET + 1);
                cb.onDone(versos);

            }
        })
                .show();
    }

    private void onClickMusicBook() {
        musicMode = !musicMode;

//        onClickNext(null);
        interrupted = true;
        entireBookMode = false;
        myLog.add("*********PRESSED CHANGE MODE TO MUSIC:" + musicMode, tag);

        getRandomChaptersAndPlay(10, new ArrayList<Integer>());

    }

    private interface StringCallback {
        void onDone(String[] versos);
    }

    class uListener extends UtteranceProgressListener {

        @Override
        public void onStart(String utteranceId) {
            myLog.add("----> START SPEAKING: " + utteranceId, tag);
//                                        Toast.makeText(MainActivity.this, utteranceId, Toast.LENGTH_SHORT).show();

//            if (utteranceId.equals(uttsavingFile)) myLog.add(tagW, " onstart uttery");

            if (!utteranceId.equals(COURTESY)) showMediaNotification();

            //notificación con la letra
            if (musicMode && !utteranceId.equals(COURTESY)) {
                showLyricsNotification();
                isShowingLyricsNotification = true;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnPlayStop.setText("STOP");
                }
            });
        }

        @Override
        public void onDone(String utteranceId) {
            myLog.add("----> END SPEAKING: " + utteranceId + " forced?" + interrupted, tag);
            // if (utteranceId.equals(msgs)) return;

            //Quitar la notificaciónde lyrics si es que
//            if (isShowingLyricsNotification) removeLyricNotification();

            if (utteranceId.equals(COURTESY)) {
                nextQueueMode = TextToSpeech.QUEUE_FLUSH;

            } else if (utteranceId.equals(uttsavingFile)) {
//                myLog.add(tagW, "terminado de guardar el archivo, vamos a mandar el intent");
//                // enviar el file to whatsapp
//
//                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
//                whatsappIntent.setPackage("com.whatsapp");
//
//                whatsappIntent.setType("audio/*");
//                Uri uri = Uri.parse(destFileName);
//
//                whatsappIntent.putExtra(Intent.EXTRA_STREAM, uri);
//
//                sendWhatsappIntent(whatsappIntent);

            } else {
                if (interrupted) {
                    myLog.add("se ha interrumpido, no ponemos otro", tag);
                    showMediaNotification();
                    interrupted = false;

                    //termino de contar la el chapter
                } else {
                    myLog.add("Ha finalizado, " + utteranceId + " por lo que ponemos el siguiente", tag);
                    playNext();
                }
            }
        }

        @Override
        public void onError(String utteranceId) {
            myLog.add("***ERROR en utterance: id = " + utteranceId, tag);

        }

    }

    private class EventsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action;
            try {
                action = intent.getAction();
                myLog.add("action received: " + action, tag);
                switch (action) {
                    case LIKE:
                        onClickLike(null);
                        break;
                    case PLAYPAUSE:
                        onClickPlayStop(null);
                        break;
                    case NEXT:
                        onClickNext(null);
                        break;
//                    case MUSICBOOK:
//                        onClickMusicBook();
//                        break;
                    case STOP:
                        t1.stop();
                        finish();
                        break;
//                    case SHARETEXT:
//                        shareLyricWhatsapp(false);
//                        break;
//                    case SHAREAUDIO:
//                        shareLyricWhatsapp(true);
//                        break;
                }
            } catch (Exception e) {
                myLog.error("on receive broadcast", e);
            }
        }

    }

//    public void shareLyricWhatsapp(final boolean audio) {
//        StringCallback cb = new StringCallback() {
//            @Override
//            public void onDone(String[] versos) {
//
//                if (audio) {
//
//                    interrupted = true;
//                    t1.stop();
//
//                    String text = Chapter.processForReading(Chapter.joinVersos(versos, ". "));
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        Toast.makeText(MainActivity.this, "Preparando la voz para enviar...", Toast.LENGTH_SHORT).show();
//
//                        File fileTTS = new File(destFileName);
//                        t1.synthesizeToFile(text, null, fileTTS, uttsavingFile);
//
//                    } else {
//                        HashMap<String, String> map = new HashMap<>();
//                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uttsavingFile);
//                        t1.synthesizeToFile(text, map, destFileName);
//                    }
//
//
//                } else { //TEXTO
//                    final String text = "_" + Chapter.joinVersos(versos, "_\n_") + "_____\n"
//                            + currentBook.getAuthor() + "\nSent by *Metal Poetry*";
//
//                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
//                    whatsappIntent.setPackage("com.whatsapp");
//                    whatsappIntent.setType("text/plain");
//                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, text);
//
//                    sendWhatsappIntent(whatsappIntent);
//
//                }
//            }
//        };
//
//
//    }


}
