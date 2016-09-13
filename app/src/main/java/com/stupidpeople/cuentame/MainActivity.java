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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static android.support.v4.app.NotificationCompat.BigTextStyle;

public class MainActivity extends AppCompatActivity {
    private static final String[] PLANETS = new String[]{"Mercury", "Venus", "Earth", "Mars", "Jupiter", "Uranus", "Neptune", "Pluto"};

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
    private int nextQueueMode;
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
    private String destFileName;
    private TextToSpeech t2;
    private String uttsavingFile = "savingFile";
    private String[] dividedLyrics;
    private int iVersoIni;
    private int iVersoEnd;
    private String versoIni;


    //todo se jode al girar la pantalla

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myLog.initialize();

        destFileName = (Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/Download") + "/" + "tts_file.wav";//TODO remove audio file

        // Restore preferences
        settings = getPreferences(MODE_PRIVATE);

        entireBookMode = settings.getBoolean(PREFS_MODE, false);
        musicMode = settings.getBoolean(PREFS_MODE_MUSIC, false);

        lastBook = settings.getInt(PREFS_LAST_BOOK, 1);
        lastChapter = settings.getInt(PREFS_LAST_CHAPTER, 1);

        myLog.add(tag2, "RECUPERANDO: mode entirebook: " + entireBookMode + " lastbook: " + lastBook + " lastchap: " + lastChapter);

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
                    //TODO remove
                    myLog.printTTSinfo(t1);

                    String engine = getBestEngine();

                    t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                t1.setOnUtteranceProgressListener(new uListener());

                                if (settings.getBoolean(PREFS_FIRST_TIME, true)) welcomeMessage();

                                myLog.add(tag2, "empezando la actividad, tramemos 10");

                                if (!entireBookMode) {
                                    getRandomChaptersAndPlay(10);
                                } else {
                                    courtesyMessage("A ver donde me quedé... ");
                                    getChapterAndPlay(lastBook, lastChapter, 10, true);
                                }

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
        filter.addAction(PLAYPAUSE);
        filter.addAction(STOP);
        filter.addAction(MUSICBOOK);
        filter.addAction(SHARETEXT);
        filter.addAction(SHAREAUDIO);
        this.registerReceiver(eventsReceiver, filter);

        //TODO ver otros sintentizadores buenos
        //poner un dialog en vez de n toast

    }

    public void shareLyricWhatsapp(final boolean audio) {
        StringCallback cb = new StringCallback() {
            @Override
            public void onDone(String[] versos) {

                if (audio) {

                    interrupted = true;
                    t1.stop();

                    String text = Chapter.processForReading(Chapter.joinVersos(versos, ". "));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Toast.makeText(MainActivity.this, "Preparando la voz para enviar...", Toast.LENGTH_SHORT).show();

                        File fileTTS = new File(destFileName);
                        t1.synthesizeToFile(text, null, fileTTS, uttsavingFile);

                    } else {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uttsavingFile);
                        t1.synthesizeToFile(text, map, destFileName);
                    }


                } else { //TEXTO
                    final String text = "_" + Chapter.joinVersos(versos, "_\n_") + "_____\n"
                            + currentBook.getAuthor() + "\nSent by *Metal Poetry*";

                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setPackage("com.whatsapp");
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, text);

                    sendWhatsappIntent(whatsappIntent);

                }
            }
        };

        chooseVersos(cb);

    }

    private void sendWhatsappIntent(Intent whatsappIntent) {
        myLog.add(tagW, "sending wasap intent");


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
            //todo dibujos para botones de notif
            Toast.makeText(this, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            myLog.add(tagW, "error lanzando la actividad wasap: " + e.getLocalizedMessage());
        }


    }

    private void setSpeakLanguage(String lan, TextToSpeech t) {
        myLog.add(tag, "Setting Language:" + lan);
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
                            myLog.add(tag, "voice set to: " + voice.toString());
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
        myLog.add(tag2, "*********Resumuinf");


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

        int iconPlayPause = t1.isSpeaking() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
//        int iconMusicBook = musicMode ? R.drawable.ic_book : R.drawable.ic_music_note_white_24dp;
        int iconMusicBook = R.drawable.ic_book;

        String title = currentChapter.isSong() ? currentChapter.getBookName() : currentBook.fakeTitle();
        String content = currentChapter.isSong() ? currentChapter.getAuthor() : currentBook.fakeAuthor();

        Notification notification = new NotificationCompat.Builder(this)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(android.R.drawable.ic_media_play)
                        // Add media control buttons that invoke intents in your media service
                .addAction(android.R.drawable.ic_media_rew, "Previous", likePendingIntent) // #0
                .addAction(iconPlayPause, "Pause", playPendingIntent)  // #1
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)     // #2
                .addAction(iconMusicBook, "MusicBook", musicBookPendingIntent)     // #3
//                        // Apply the media style template
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

    private void showLyricsNotification() {

        Intent shareText = new Intent(SHARETEXT);
        PendingIntent pendingIntentText = PendingIntent.getBroadcast(this, 1, shareText, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent shareAudio = new Intent(SHAREAUDIO);
        PendingIntent pendingIntentAudio = PendingIntent.getBroadcast(this, 1, shareAudio, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(android.R.drawable.ic_media_play) //TODO poner icono de musica
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

    private void courtesyMessage(String s) {
        myLog.add(tag, "Courtesy: " + s);
        setSpeakLanguage("ES", t1);

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

    //Play

    /**
     * Elige (on line) un summary book, o una banda
     *
     * @param musicMode si es modo musica, elege una banda
     * @param cb
     */
    private void getRandomBookSummary(boolean musicMode, final BookSumCallback cb) {

        ParseQuery<BookSummary> q = ParseQuery.getQuery(BookSummary.class);

        if (musicMode) { // get all bands

            // If already loaded
            if (allBandsSum != null) {
                getRandomBand(cb);

            } else {
                q.whereEqualTo("isMusic", true);
//                q.whereNotContainedIn("libroId", parseHelper.getHatedBandsIds()); todo
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
//            q.whereNotContainedIn("libroId", parseHelper.getHatedBooksIds()); todo
            q.orderByDescending("libroId");

            q.getFirstInBackground(new GetCallback<BookSummary>() {
                @Override
                public void done(BookSummary bookSummary, ParseException e) {
                    if (e == null) {

                        final int nBooks = bookSummary.getInt("libroId");
                        final int iBook = new Random().nextInt(nBooks + 1);

                        myLog.add(tag2, "RANDOM: elegido el libro:" + iBook + "/" + nBooks);
                        parseHelper.getBookSummary(iBook, cb);

                    } else {
                        myLog.add(tag, "EEROR en getting the maximun book" + e.getLocalizedMessage());
                    }
                }
            });

        }
    }

    private void getRandomBand(BookSumCallback cb) {
        Random random = new Random();
        cb.onReceived(allBandsSum.get(random.nextInt(allBandsSum.size())));
    }

    private void getRandomChaptersAndPlay(final int chapters) {

        if (isOnline()) {

            BookSumCallback cb = new BookSumCallback() {
                @Override
                public void onReceived(BookSummary bookSummary) {
                    //Get the chapters( or songs):

                    currentBook = bookSummary;

                    final int iChapter = new Random().nextInt(bookSummary.nChapters() + 1);

//                    BookContability.incrementJumpedInBook(bookSummary); TODO MARCAR libro por visita

                    getChapterAndPlay(bookSummary.getId(), iChapter, chapters, false);
                }

                @Override
                public void onError(String text, ParseException e) {
                    myLog.error(text, e);
                }
            };

            getRandomBookSummary(musicMode, cb);


        } else {
            courtesyMessage("Tengo un problema, no puedo recordar más historias. Si conectas internet, seguro que me refresca la memoria.");
        }
    }

    /**
     * Lee de parse (local o no) y guarda en buffer (array) nchpater
     *
     * @param iBook
     * @param iChapter
     * @param bufferSize
     */
    private void getChapterAndPlay(final int iBook, final int iChapter, final int bufferSize, final boolean local) {

        BookSumCallback cb = new BookSumCallback() {
            @Override
            public void onReceived(BookSummary bookSummary) {
                currentBook = bookSummary;

                parseHelper.getChapters(iBook, iChapter, bufferSize, local, new FindCallback<Chapter>() {
                    @Override
                    public void done(List<Chapter> chapters, ParseException e) {
                        if (e == null) {
                            myLog.add(tag2, "--- Traidos capitulos:" + chapters.size() + "desde local?" + local);

                            if (chapters.size() > 0) {
                                setCurrentChapter(chapters.get(0));
                                iBuffer = 0;
                                chaptersPreLoaded = chapters;
                                playCurrentChapter();

                            } else {
                                //Hemos llegado al final
                                if (currentBook.nChapters() == currentChapter.getChapterId()) {
                                    courtesyMessage("Fin. Espero que te haya gustado tanto como a mi.");
                                    if (entireBookMode) {
                                        BookContability.setFinishedBook(currentBook);
                                    } else {
                                        courtesyMessage("Ah, pero no lo habías oído desde el principio.");
                                        onClickLike(null);
                                    }
                                } else {
                                    myLog.add(tag, "como no hemos cargado chaps, ponemos random");
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
                myLog.error(text, e);
            }
        };

        parseHelper.getBookSummary(iBook, local, cb);
    }

    /**
     * Empieza el siguiente del buffer, y se si ha acabado, trae los siguients
     */
    private void playNext() {
        myLog.add(tag, "en playNext, ibffer = " + iBuffer);

        if (chaptersPreLoaded == null || iBuffer + 1 == chaptersPreLoaded.size()) { //Traer los siguientes
            if (currentChapter == null) {
                getRandomChaptersAndPlay(10);
            } else {
                getChapterAndPlay(currentChapter.getBookId(), currentChapter.getChapterId() + 1, 10, entireBookMode);
            }

        } else {

            iBuffer++;
            final Chapter chapter = chaptersPreLoaded.get(iBuffer);
            myLog.add(tag2, "Ahora ibffer=" + iBuffer + " y el chaper es" + chapter.shortDescription());

            setCurrentChapter(chapter);
            playCurrentChapter();
        }


    }

    private void playCurrentChapter() {

        if (currentChapter == null) {
            myLog.add(tag, "..Playnext porque play current pero no hay ninguno");
            playNext();

        } else {
            myLog.add(tag, "----> MANDADO: " + currentChapter.shortestDescription());

            //TODO cambiar sólo si es distinto
            setSpeakLanguage(currentChapter.getLanguage(), t1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                myLog.add("TTS", "next chapter speaking with voice:" + t1.getVoice().toString());
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
            courtesyMessage("Instale un sintenizador bueno, la calidad no será aceptable. Por ejemplo, el sintenizador de Samsung");
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
        entireBookMode = false;
        myLog.add(tag, "*********PRESSED NEXT");

        //block button 2 secons
        btnNext.setEnabled(false);
        btnNext.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnNext.setEnabled(true);
            }
        }, 2000);

        myLog.add(tag, "..Playnext porque cluckedNext");

        if (!btnLike.isEnabled()) btnLike.setEnabled(true);

        if (musicMode) {
            //avanzamos en el buffer y luego saltanos
            playNext();
        } else {
            courtesyMessage("Vaya, no te ha gustado. Veamos otra cosa...");
            getRandomChaptersAndPlay(10);
        }


    }

    public void onClickLike(View view) {

//        myLog.add(tag, "*********PRESSED LIKE");
//
//        btnLike.setEnabled(false);
//
//
//        if (isOnline()) {
//            final int bookId = currentChapter.getBookId();
//            courtesyMessage("Bueno, ya que te gusta el relato, veamo si recuerdo cómo empezaba...");
//            interrupted = true;
//            getChapterAndPlay(bookId, 1, 10, false);
//
//
//            parseHelper.importWholeBook(bookId, new TaskDoneCallback2() {
//                @Override
//                public void onDone() {
//                    myLog.add(tag, "DONE. book " + bookId + " loaded in internal storage");
//                    entireBookMode = true;
//                }
//
//                @Override
//                public void onError(String text, ParseException e) {
//                    myLog.error("fallo en importar los libros |" + text, e);
//                }
//            });
//
//        } else {
//            courtesyMessage("Te lo contaría desde el principio, pero necesitamos conección a internet para ayudarme a recordar. Seguiré por donde iba...");
//            playCurrentChapter();
//        }
    }

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
                myLog.add(tag, "[Dialog]selectedIndex: " + selectedIndex + ", item: " + item);
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
                myLog.add(tag, "[Dialog]selectedIndex: " + selectedIndex + ", item: " + item);
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
        myLog.add(tag, "*********PRESSED CHANGE MODE TO MUSIC:" + musicMode);

        getRandomChaptersAndPlay(10);

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

    private void removeLyricNotification() {
        mNotificationManager.cancel(2);
    }

    private interface StringCallback {
        void onDone(String[] versos);
    }

    class uListener extends UtteranceProgressListener {

        @Override
        public void onStart(String utteranceId) {
            myLog.add(tag, "----> START SPEAKING: " + utteranceId);
//                                        Toast.makeText(MainActivity.this, utteranceId, Toast.LENGTH_SHORT).show();

//            if (utteranceId.equals(uttsavingFile)) myLog.add(tagW, " onstart uttery");

            showMediaNotification();
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
            myLog.add(tag, "----> END SPEAKING: " + utteranceId + " forced?" + interrupted);
            // if (utteranceId.equals(msgs)) return;

            //Quitar la notificaciónde lyrics si es que
            if (isShowingLyricsNotification) {
                removeLyricNotification();
            }


            if (utteranceId.equals(COURTESY)) {
                nextQueueMode = TextToSpeech.QUEUE_FLUSH;

            } else if (utteranceId.equals(uttsavingFile)) {
                myLog.add(tagW, "terminado de guardar el archivo, vamos a mandar el intent");
                // enviar el file to whatsapp

                // Todo convert to mp3 before sending
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setPackage("com.whatsapp");

                whatsappIntent.setType("audio/*");
                Uri uri = Uri.parse(destFileName);

                whatsappIntent.putExtra(Intent.EXTRA_STREAM, uri);

                sendWhatsappIntent(whatsappIntent);

            } else {
                if (interrupted) {
                    myLog.add(tag, "se ha interrumpido, no ponemos otro");
                    showMediaNotification();
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
                myLog.add(tag, "action received: " + action);
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
                    case MUSICBOOK:
                        onClickMusicBook();
                        break;
                    case STOP:
                        t1.stop();
                        finish();
                        break;
                    case SHARETEXT:
                        //TODO select the text to share
                        shareLyricWhatsapp(false);
                        break;
                    case SHAREAUDIO:
                        shareLyricWhatsapp(true);
                        break;
                }
            } catch (Exception e) {
                myLog.error("on receive broadcast", e);
            }
        }

    }

}
