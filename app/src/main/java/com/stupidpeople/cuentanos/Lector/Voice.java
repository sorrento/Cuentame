package com.stupidpeople.cuentanos.Lector;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.stupidpeople.cuentanos.Constants;
import com.stupidpeople.cuentanos.Speak;
import com.stupidpeople.cuentanos.book.Chapter;
import com.stupidpeople.cuentanos.utils.myLog;
import com.stupidpeople.cuentanos.utils.text;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import cafe.adriel.androidaudioconverter.callback.IConvertCallback;

import static com.stupidpeople.cuentanos.Lector.AudioUtils.wav2mp3;

public class Voice implements VoiceInterface {

    private static final String             UTTSAVEPREFIX     = "SAVE";
    private final        String             mCurrentLanguage;
    private final        Context            mContext;
    private final        ReaderEvents       readerEvents;
    final private        String             samsungEngine     = "com.samsung.SMT";
    private              TextToSpeech       tts;
    private              boolean            isSpeakingChapter = false;
    private              String             tag               = "VOICE";
    private              boolean            forzadoACallar    = false;
    private              String             mWavPath;
    private              LinkedList<String> wavFilesCola;
    private              boolean            isConvetingMp3    = false;

    Voice(String currentLaguage, Context context, ReaderEvents re) {

        mCurrentLanguage = currentLaguage;
        mContext = context;
        readerEvents = re;
        //this.voiceEventInterface = voiceEventInterface;
        getBestTTS();

        wavFilesCola = new LinkedList<>();
    }

    /**
     * Primero se inicializa para obtener los posibles motores disponibles
     */
    private void getBestTTS() {

        TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    Speak.setSpeakLanguage(tts, mCurrentLanguage);
                    tts.setOnUtteranceProgressListener(new uListener());
                    readerEvents.voiceReady();
                }
            }
        };

        //tts = new TextToSpeech(mContext, onInitListener, samsungEngine);
        tts = new TextToSpeech(mContext, onInitListener); // para las pruebas en Android studio
    }


    @Override
    public void setLanguage(String newLan) {
        Speak.setSpeakLanguage(tts, newLan);
    }

    @Override
    public void speakChapter(Chapter chapter, boolean interrumpir) {
        if (chapter != null) {
            myLog.add("speak chapter: " + chapter.getUtterance(), tag);
            Speak.speakChapter(chapter, tts);
        }
    }

    @Override
    public void shutUp() {
        myLog.add("shutup", tag);
        forzadoACallar = true;
        tts.stop();
    }

    @Override
    public void predefinedPhrases(TipoFrase tipoFrase, boolean interrupt) {
        Speak.speakPredefinedPhrase(tipoFrase, mCurrentLanguage, tts, interrupt);
    }

    void shutdown() {
        tts.stop();
        tts.shutdown();
    }

    boolean isSpeakingChapter() {
        return (tts.isSpeaking() & isSpeakingChapter);
    }

    void speakDeveloperMsg(String text) {
        tts.speak("Pipipí, pipipí." + text, TextToSpeech.QUEUE_FLUSH, null,
                com.stupidpeople.cuentanos.utils.text.shortenText(text, 15));
    }

    void speakDefinition(String s) {
        Speak.speak(s, true, "[Definition]" + text.shortenText(s, 100), tts);
    }

    void text2file(String txt, String path, String filename) {
        mWavPath = path;
        String utt          = UTTSAVEPREFIX + "_" + filename;
        String destFileName = path + '/' + filename + ".wav";

//        if (isLastText) utt += "_" + "Last";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            File fileTTS = new File(destFileName);
            tts.synthesizeToFile(txt, null, fileTTS, utt);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utt);
            tts.synthesizeToFile(txt, map, destFileName);
        }
    }

    private void cogeElSiguienteYConvierte() {
        IConvertCallback cb = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                isConvetingMp3 = false;
                String name = convertedFile.getName().split("\\.")[0];
                myLog.add("OK, se ha convertido un mp3" + name, tag);

                //remove the .wav
                File wavFile = new File(mWavPath + "/" + name + ".wav");
                wavFile.delete();
                readerEvents.txt2fileOneFileWritten(Integer.parseInt(name));//para mostrar notificación

                cogeElSiguienteYConvierte();
            }

            @Override
            public void onFailure(Exception error) {
                myLog.add("ha fallado un mp3" + error, tag);
            }
        };

        String wavFile = wavFilesCola.pollFirst();
        if (wavFile != null) {
            isConvetingMp3 = true;
            myLog.add("vamos a intentar convertir un mp3" + wavFile, tag);
            wav2mp3(mWavPath, wavFile, mContext, cb);
        } else { //significa que ya ha terminado la cola
            readerEvents.txt2fileBunchProcessed();
        }
    }

    class uListener extends UtteranceProgressListener {

        @Override
        public void onStart(String utteranceId) {
            isSpeakingChapter = isUtteranceOfChapter(utteranceId);
            if (isSpeakingChapter) {
                readerEvents.voiceStartedSpeakChapter();
            }
            myLog.add("onstartspeaking:" + utteranceId, tag);
        }

        @Override
        public void onDone(String utteranceId) {
            myLog.add("on done, terminé de hablar: " + utteranceId + "forzado=" + forzadoACallar,
                    tag);
            if (forzadoACallar) {
                readerEvents.voiceInterrupted();
                forzadoACallar = false;
            } else {
                if (isUtteranceOfChapter(utteranceId)) { // Lectura de voz de chapter
                    readerEvents.voiceEndedReadingChapter();
                    myLog.add("on done, terminé de leer chapter", tag);
                } else if (utteranceId.startsWith(UTTSAVEPREFIX)) { //lectura de file a chapter
                    final String wavFile = utteranceId.split("_")[1];
                    wavFilesCola.add(wavFile);
                    if (!isConvetingMp3) cogeElSiguienteYConvierte();

                }
            }
        }

        @Override
        public void onError(String utteranceId) {
            // si se ha forzado a callar
            myLog.add("on error de leer. Forzado a callar=" + forzadoACallar, tag);

            if (isUtteranceOfChapter(utteranceId)) {
                //readerEvents.onInterruptionOfReading();
                if (forzadoACallar) {
                    readerEvents.voiceInterrupted();
                    forzadoACallar = false;
                } else {
                    speakDeveloperMsg("Error en el uterance, ver el log");
                    myLog.add("***ERROR en utterance: id = " + utteranceId, tag);
                }
            }
        }

        @Override
        public void onStop(String utteranceId, boolean interrupted) {
            super.onStop(utteranceId, interrupted);
            myLog.add("onstop utterance", tag);
        }

        private boolean isUtteranceOfChapter(String utteranceId) {
            return utteranceId.startsWith(Constants.PREFIX_OF_CHAPTER_UTTERANCE);
        }
    }

    class ObjetoHablado {
        private final TipoFrase mTipofrase;
        private final String    mTexto;
        private final String    mUtterance;
        private       int       nChapterId;
        private       boolean   mIsChapter = false;

        public ObjetoHablado(TipoFrase tipoFrase, String texto, String Utterance) {
            mTipofrase = tipoFrase;
            mTexto = texto;
            mUtterance = Utterance;

        }

        public ObjetoHablado(Chapter chapter) {
            mTipofrase = TipoFrase.CHAPTER;
            mTexto = chapter.getProcessedText();
            mUtterance = chapter.getUtterance();
            mIsChapter = true;
            nChapterId = chapter.getChapterId();
        }

        public boolean isChapter() {
            return mTipofrase == TipoFrase.CHAPTER;
        }

        public int getChapterId() {
            return nChapterId;
        }
    }
}
