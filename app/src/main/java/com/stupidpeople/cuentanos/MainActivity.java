package com.stupidpeople.cuentanos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidpeople.cuentanos.Lector.Lector;
import com.stupidpeople.cuentanos.Lector.ReaderEvents;
import com.stupidpeople.cuentanos.book.Book;
import com.stupidpeople.cuentanos.ui.ActionsInterface;
import com.stupidpeople.cuentanos.ui.UiGeneric;
import com.stupidpeople.cuentanos.utils.myLog;

public class MainActivity extends AppCompatActivity {

    private static final int                       MEDIA_BUTTON_INTENT_EMPIRICAL_PRIORITY_VALUE = 10000;
    //NotificationManager mNotificationManager;
    //    private BroadcastReceiver         eventsReceiver;
    private              MediaButtonIntentReceiver mMediaButtonReceiver;
    private              boolean                   interrupted                                  = false;
    private              int                       iBuffer                                      = 0;
    private              boolean                   mlocal;
    private              AudioManager              manager;
    private              Preferences               prefs;
    private              Lector                    lector;
    private              String                    tag                                          = "MAI";
    // private              NotificationHelper        notificacionHelper;
    private              DevUi                     devUi;

    @NonNull
    private static IntentFilter getIntentFilterLector() {
        IntentFilter intentFilter = new IntentFilter(Oreja.ACTION_STARTED_READING_CHAPTER);
        intentFilter.addAction(Oreja.ACTION_ENDED_READING_CHAPTER);
        intentFilter.addAction(Oreja.ACTION_OBTIENE_SUMMARY);
        intentFilter.addAction(Oreja.ACTION_NEW_CHAPTER_LOADED);
        intentFilter.addAction(Oreja.ACTION_STOPPED_READING_CHAP);
        intentFilter.addAction(Oreja.ACTION_CHANGE_STORAGE_MODE);

        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myLog.initialize();
        prefs = new Preferences(this);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

        //getmediaButtons();//Bluetooth

        // Eventos
        Oreja oreja = new Oreja();
        LocalBroadcastManager.getInstance(this).registerReceiver(oreja, getIntentFilterLector());

        ActionsInterface myUi = new ActionsInterface() {

            @Override
            public void apretadoLikeOrBack() {
                lector.setLikedCurrentBook(true);
                lector.leeDesdePrincipio();
            }

            @Override
            public void apretadoNextOrHate() {
                lector.setLikedCurrentBook(false);
                lector.accionCambiaDeLibro();
            }

            @Override
            public void apretadoPause() {
                lector.accionStopReading();
            }

            @Override
            public void apretadoPlay() {
                lector.accionLeeLoQueToca();
            }
        };

        //UI dev
        devUi = new DevUi(myUi);

        //UI notificaciones
        //TODO hacer notificaciones

//        notificacionHelper = new NotificationHelper(myUi,
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE),
//                this);

        ReaderEvents readerEvents = new ReaderEvents() {

            public void bookAndVoiceReady() {
//                devUi.updateBecauseNewBookLoaded();
//                lector.resumeReadingNewSession();
            }

            @Override
            public void voiceStartedSpeakChapter() {

            }

            @Override
            public void voiceInterrupted() {

            }

            @Override
            public void voiceEndedReadingChapter() {

            }

        /*    @Override
            public void onStartedSpeaking(String utteranceId) {
                myLog.add("on startspeaking", tag);
                devUi.updateBecauseStarted();
            }


            @Override
            protected void onStartedSpeakingChapter(int chapterId, boolean isLocalStorage, String texto) {
                myLog.add("onstartedspeakingchapter", tag);
                devUi.updateWithNewChapter(chapterId, isLocalStorage, texto);
//                                  notificacionHelper.updateWithNewChapter(chapterId);
            }

            @Override
            protected void onInterruptionOfReading() {
                myLog.add("onstpedspeaking)", tag);
                devUi.updateBecauseStopped();
                //                                notificacionHelper.updateBecauseStopped();
            }

            @Override
            public void OnBookChanged() {
                devUi.updateBecauseNewBookLoaded(lector.getBook());
            }*/
        };

        lector = new Lector(getApplicationContext(), prefs);
    }

    private void getmediaButtons() {
        // Bluetooth controls
        // manager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mMediaButtonReceiver = new MediaButtonIntentReceiver();
        IntentFilter mediaFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        mediaFilter.setPriority(MEDIA_BUTTON_INTENT_EMPIRICAL_PRIORITY_VALUE);
        registerReceiver(mMediaButtonReceiver, mediaFilter);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        notificacionHelper.unregisterReceiver(this);
//        this.unregisterReceiver(mMediaButtonReceiver);
        //prefs.setEntireBookMode(prefs.entireBookMode);
        lector.shutdownVoice();
    }


    // De la interfaz Dev
    public void onClickGo(View view) {
        myLog.add("onclickgo", tag);
        devUi.apretadoGo();
    }

    public void onClickPlayStop(View view) {
        myLog.add("onclickplaystop", tag);
        if (lector.isReading()) {
            Toast.makeText(this, "Apretado stop", Toast.LENGTH_SHORT).show();
            myLog.add(">>>>>>>>CLICK STOP", tag);
            devUi.apretadoPause();
        } else {
            Toast.makeText(this, "Apretado PLAY", Toast.LENGTH_SHORT).show();
            myLog.add(">>>>>>>>>CLICK PLAY", tag);
            devUi.apretadoPlay();
        }
        ;
    }

    public void onClickNext(View view) {
        myLog.add(">>>>>>>>>CLICK NEXT", tag);
        devUi.apretadoNextOrHate();
    }

    public void onClickLike(View view) {
        myLog.add(">>>>>>>>>CLICK LIKE", tag);
        devUi.apretadoLikeOrBack();
    }


    /**
     * Del Bluetooth
     */
    private class MediaButtonIntentReceiver extends BroadcastReceiver {

        public MediaButtonIntentReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                return;
            }
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return;
            }
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                //Toast.makeText(context, "apretado bluetot", Toast.LENGTH_SHORT).show();
                //onClickPlayStop(null);
            }
            abortBroadcast();
        }

        boolean onKeyDown(int keyCode, KeyEvent event) {
            //AudibleReadyPlayer abc;
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    // code for fast forward
                    return true;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    // code for next
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    // code for play/pause
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    // code for previous
                    return true;
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    // code for rewind
                    return true;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    // code for stop
                    return true;
            }
            return false;
        }
    }

    public class DevUi extends UiGeneric implements ActionsInterface {

        ActionsInterface actionsInterface;
        private TextView txtText;
        private TextView txtDesc;
        private TextView txtLocal;
        private Button   btnPlayStop;
        private Button   btnNext;
        private Button   btnLike;
        private Button   btnGo;
        private EditText edtChapter;
        private EditText edtBook;


        public DevUi(ActionsInterface actionsInterface) {
            super(actionsInterface);
            //this.actionsInterface = actionsInterface;
            getUiComponents();
        }

        @Override
        public void updateWithNewChapter() {
            final Book book = lector.getBook();
            myLog.add("UI updatenewchapter", tag);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    edtChapter.setHint(Integer.toString(book.getCurrentChapterId()));
                    txtLocal.setText(book.getStorageType());
                    txtText.setText(book.getCurrentChapter().getText());
                }
            });
        }

        @Override
        public void updateBecauseStopped() {
            myLog.add("UI updated because stopped (cambiar el boton)", tag);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myLog.add("UI updated because stopped (cambiar el boton). Ahora dentro de RUNUI", tag);
                    btnPlayStop.setText("PLAY");
                }
            });
        }

        @Override
        public void updateBecauseStarted() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnPlayStop.setText("STOP");
                }
            });
        }

        public void updateBecauseNewBookLoaded() {
            final Book book = lector.getBook();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //txtText.setText(book.getCurrentChapter().getText());
                    txtDesc.setText(book.getBookSummary().toString());
                    txtLocal.setText(book.getStorageType());
                    edtChapter.setHint(Integer.toString(book.getCurrentChapterId()));
                    edtBook.setHint(Integer.toString(book.getBookId()));
                }
            });
        }

        private void getUiComponents() {
            txtText = (TextView) findViewById(R.id.txtCurrentText);
            txtDesc = (TextView) findViewById(R.id.txtCurrentDesc);
            txtLocal = (TextView) findViewById(R.id.txtFromLocal);
            btnPlayStop = (Button) findViewById(R.id.btn_play_stop);
            btnNext = (Button) findViewById(R.id.btn_next);
            btnLike = (Button) findViewById(R.id.btn_like);
            btnGo = (Button) findViewById(R.id.btn_go);
            edtChapter = (EditText) findViewById(R.id.etChapter);
            edtBook = (EditText) findViewById(R.id.etbook);
        }

        @Override
        public void apretadoLikeOrBack() {
            lector.setLikedCurrentBook(true);
            lector.leeDesdePrincipio();
        }

        @Override
        public void apretadoNextOrHate() {
            lector.accionStopReading(); //todo esto podría estar en la interfaz, doros los apretado next debería hacer lomismo (clase abstract?)
            lector.setLikedCurrentBook(false);
            lector.accionCambiaDeLibro();
        }

        @Override
        public void apretadoPause() {
            lector.accionStopReading();
        }

        @Override
        public void apretadoPlay() {
            lector.accionLeeLoQueToca();
        }

        public void apretadoGo() {
            int    nChap = Integer.parseInt(edtChapter.getText().toString());
            int    nBook;
            String s     = edtBook.getText().toString();
            nBook = Integer.parseInt(s.equals("") ? edtBook.getHint().toString() : edtBook.getText().toString());

            lector.shutUp();
            lector.accionSaltaACapitulo(nBook, nChap);
        }

        public void updateStorageMode() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtLocal.setText(lector.getStorageType());
                }
            });
        }
    }

    public class Oreja extends BroadcastReceiver {
        public static final String ACTION_STARTED_READING_CHAPTER = "started_reading_chapter";
        public static final String ACTION_NEW_CHAPTER_LOADED      = "new_chapter_loaded";
        public static final String ACTION_ENDED_READING_CHAPTER   = "ended_reading_chapter";
        public static final String ACTION_OBTIENE_SUMMARY         = "obtiene_summary";
        public static final String ACTION_STOPPED_READING_CHAP    = "stopped_reading";
        public static final String ACTION_CHANGE_STORAGE_MODE     = "change_sotrage_mode";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(tag, "onReceive Oreja: " + action);

            switch (action) {
                case ACTION_NEW_CHAPTER_LOADED:
                    //devUi.updateWithNewChapter();
                    break;
                case ACTION_STARTED_READING_CHAPTER:
                    //aa
                    devUi.updateWithNewChapter();
                    devUi.updateBecauseStarted();
                    break;
                case ACTION_OBTIENE_SUMMARY:
                    devUi.updateBecauseNewBookLoaded();
                    break;
                case ACTION_ENDED_READING_CHAPTER:
                    //complete
                    break;
                case ACTION_STOPPED_READING_CHAP:
                    devUi.updateBecauseStopped();
                    break;
                case ACTION_CHANGE_STORAGE_MODE:
                    devUi.updateStorageMode();
                    break;
                default:
                    //dd
                    break;
            }
        }
    }
}