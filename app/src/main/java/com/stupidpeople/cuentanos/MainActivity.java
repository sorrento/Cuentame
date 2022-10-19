package com.stupidpeople.cuentanos;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stupidpeople.cuentanos.Lector.Lector;
import com.stupidpeople.cuentanos.ui.ActionsInterface;
import com.stupidpeople.cuentanos.ui.UIDev;
import com.stupidpeople.cuentanos.ui.UINotification;
import com.stupidpeople.cuentanos.ui.UiGeneric;
import com.stupidpeople.cuentanos.utils.Preferences;
import com.stupidpeople.cuentanos.utils.myLog;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

public class MainActivity extends AppCompatActivity {

    private static final int MEDIA_BUTTON_INTENT_EMPIRICAL_PRIORITY_VALUE = 10000;
    //NotificationManager mNotificationManager;
    //    private BroadcastReceiver         eventsReceiver;
    private MediaButtonIntentReceiver mMediaButtonReceiver;
    private boolean interrupted = false;
    private int iBuffer = 0;
    private boolean mlocal;
    private AudioManager manager;
    private Preferences prefs;
    private Lector lector;
    private String tag = "MAI";
    Oreja oreja;
    private UIDev devUi;
    private UINotification notificationUI;

    @NonNull
    private static IntentFilter getIntentFilterLector() {
        IntentFilter intentFilter = new IntentFilter(Oreja.ACTION_STARTED_READING_CHAPTER);
        intentFilter.addAction(Oreja.ACTION_ENDED_READING_CHAPTER);
        intentFilter.addAction(Oreja.ACTION_OBTIENE_SUMMARY);
        intentFilter.addAction(Oreja.ACTION_NEW_CHAPTER_LOADED);
        intentFilter.addAction(Oreja.ACTION_STOPPED_READING_CHAP);
        intentFilter.addAction(Oreja.ACTION_CHANGE_STORAGE_MODE);
        intentFilter.addAction(Oreja.ACTION_MP3FILEWRITTEN);

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

        /*//////PRUEBA
        String text = DiccionarioUtils.getSampleChapterText();
        Definator definator = new Definator(text, "ES", new ArrayCallback() {
            @Override
            public void onDone(List<String> arr, List<Double> bestScores) {

            }
        });
        //////PRUEBA*/


        // Eventos
        oreja = new Oreja();
        LocalBroadcastManager.getInstance(this).registerReceiver(oreja, getIntentFilterLector());

        ActionsInterface myUi = new ActionsInterface() {

            @Override
            public void apretadoLikeOrBack() {
                lector.setLikedCurrentBook(true);
                lector.leeDesdePrincipio();
            }

            @Override
            public void apretadoNextOrHate() {
                lector.accionStopReading();
                lector.setLikedCurrentBook(false);
                lector.accionCambiaDeLibro(true);
            }

            @Override
            public void apretadoPause() {
                lector.accionStopReading();
            }

            @Override
            public void apretadoPlay() {
                lector.speakCurrentChapter();
            }

            @Override
            public void apretadoDiccionario() {
                lector.definePalabrasDelChapter();
            }

            @Override
            public void apretadoStop() {
                MainActivity.this.finish();
            }

            @Override
            public void apretadoGo(int nBook, int nChap) {
             /*   int    nChap = Integer.parseInt(edtChapter.getText().toString());
                int    nBook;
                String s     = edtBook.getText().toString();
                nBook = Integer.parseInt(s.equals("") ? edtBook.getHint().toString() : edtBook.getText().toString());
*/
                lector.shutUp();
                lector.accionSaltaACapitulo(nBook, nChap);

            }
        };

        //UI dev
        devUi = new UIDev(this, myUi, prefs);

        //UI notificaciones
        notificationUI = new UINotification(myUi,
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE), this);


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

        LocalBroadcastManager.getInstance(this).unregisterReceiver(oreja);
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
            devUi.apretado(UiGeneric.TipoBoton.PAUSE);
        } else {
            Toast.makeText(this, "Apretado PLAY", Toast.LENGTH_SHORT).show();
            myLog.add(">>>>>>>>>CLICK PLAY", tag);
            devUi.apretado(UiGeneric.TipoBoton.PLAY);
        }
        ;
    }

    public void onClickNext(View view) {
        myLog.add(">>>>>>>>>CLICK NEXT", tag);
        devUi.apretado(UiGeneric.TipoBoton.NEXT);
    }

    public void onClickLike(View view) {
        myLog.add(">>>>>>>>>CLICK LIKE", tag);
        devUi.apretado(UiGeneric.TipoBoton.PREV);
    }

    public void onClickDicc(View view) {
        myLog.add(">>>>>>>>>CLICK DICCIONARIO", tag);
        devUi.apretado(UiGeneric.TipoBoton.DICCIONARIO);
    }

    public void onClickWav(View view) {
        //Converter wav mp3
        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "cargado el converter", Toast.LENGTH_SHORT).show();

                File[] Dirs = ContextCompat.getExternalFilesDirs(MainActivity.this, null);
                String path = Dirs[1].getAbsolutePath();

                lector.createMp3sLibroEntero(path);
                // lector.createMp3s(10, 13, path);
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(MainActivity.this, "// FFmpeg is not supported by device", Toast.LENGTH_SHORT).show();
            }
        });


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

    public class Oreja extends BroadcastReceiver {
        public static final String ACTION_STARTED_READING_CHAPTER = "started_reading_chapter";
        public static final String ACTION_NEW_CHAPTER_LOADED = "new_chapter_loaded";
        public static final String ACTION_ENDED_READING_CHAPTER = "ended_reading_chapter";
        public static final String ACTION_OBTIENE_SUMMARY = "obtiene_summary";
        public static final String ACTION_STOPPED_READING_CHAP = "stopped_reading";
        public static final String ACTION_CHANGE_STORAGE_MODE = "change_sotrage_mode";
        public static final String ACTION_MP3FILEWRITTEN = "mp3_file_written";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(tag, "onReceive Oreja: " + action);

            switch (action) {
                case ACTION_NEW_CHAPTER_LOADED:
                    //devUi.updateWithNewChapter();
                    break;
                case ACTION_STARTED_READING_CHAPTER:
                    devUi.updateWithNewChapter(lector.getBook().getCurrentChapter(), lector.getBook());
                    notificationUI.updateWithNewChapter(lector.getBook().getCurrentChapter(), lector.getBook());
                    devUi.updateBecauseStarted();
                    notificationUI.updateBecauseStarted();
                    break;
                case ACTION_OBTIENE_SUMMARY:
                    devUi.updateBecauseNewBookLoaded(lector.getBook().getBookSummary(), lector.getBook(),
                            prefs.getDisponibles());
                    notificationUI.updateBecauseNewBookLoaded(lector.getBook().getBookSummary(),
                            lector.getBook(), prefs.getDisponibles());
                    break;
                case ACTION_ENDED_READING_CHAPTER:
                    //TODO complete
                    break;
                case ACTION_STOPPED_READING_CHAP:
                    devUi.updateBecauseStopped();
                    notificationUI.updateBecauseStopped();
                    break;
                case ACTION_CHANGE_STORAGE_MODE:
                    devUi.updateStorageMode(lector.getStorageType());
                    break;
                case ACTION_MP3FILEWRITTEN:
                    int i = intent.getIntExtra("chapter", 1);
                    int total = intent.getIntExtra("total", 1000);
                    notificationUI.showmp3status(i, total);
                    break;
                default:
                    break;
            }
        }
    }
}