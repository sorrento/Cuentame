package com.stupidpeople.cuentanos.Lector;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.stupidpeople.cuentanos.Chapter;
import com.stupidpeople.cuentanos.Speak;
import com.stupidpeople.cuentanos.utils.myLog;

public class Voice implements VoiceInterface {

    public static final String       PREFIX_OF_CHAPTER_UTTERANCE = "(chapter)";
    private final       String       mCurrentLanguage;
    private final       Context      mContext;
    private final       ReaderEvents readerEvents;
    final private       String       samsungEngine               = "com.samsung.SMT";
    //private final       VoiceEventInterface   voiceEventInterface;
    private             TextToSpeech tts;
    private             boolean      isSpeakingChapter           = false;
    private             String       tag                         = "VOICE";
    private             boolean      forzadoACallar              = false;


    public Voice(String currentLaguage, Context context, ReaderEvents re) {

        mCurrentLanguage = currentLaguage;
        mContext = context;
        readerEvents = re;
        //this.voiceEventInterface = voiceEventInterface;
        getBestTTS();
    }

    /**
     * Primero se inicializa para obtener los posibles motores disponibles
     *
     * @return
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
        //mObjetoHablado = new ObjetoHablado(chapter);
        myLog.add("speak chapter: " + chapter.getUtterance(), tag);
        Speak.speakChapter(chapter, tts);
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

    public void speakDeveloperMsg(String text) {
        tts.speak("Pipipí, pipipí." + text, TextToSpeech.QUEUE_FLUSH, null,
                com.stupidpeople.cuentanos.utils.text.shortenText(text, 15));
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
                if (isUtteranceOfChapter(utteranceId)) {
                    readerEvents.voiceEndedReadingChapter();
                    myLog.add("on done, terminé de leer chapter", tag);
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

            } else {

            }
        }

        @Override
        public void onStop(String utteranceId, boolean interrupted) {
            super.onStop(utteranceId, interrupted);
            myLog.add("onstop utterance", tag);
        }

        private boolean isUtteranceOfChapter(String utteranceId) {
            return utteranceId.startsWith(PREFIX_OF_CHAPTER_UTTERANCE);
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
