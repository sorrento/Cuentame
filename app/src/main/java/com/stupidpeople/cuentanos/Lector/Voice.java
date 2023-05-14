package com.stupidpeople.cuentanos.Lector;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.stupidpeople.cuentanos.Constants;
import com.stupidpeople.cuentanos.Speak;
import com.stupidpeople.cuentanos.book.Chapter;
import com.stupidpeople.cuentanos.utils.myLog;
import com.stupidpeople.cuentanos.utils.text;


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

    Voice(String currentLaguage, Context context, ReaderEvents re) {

        mCurrentLanguage = currentLaguage;
        mContext = context;
        readerEvents = re;
        //this.voiceEventInterface = voiceEventInterface;
        getBestTTS();

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
//            myLog.add("onstop utterance", tag);
        }

        private boolean isUtteranceOfChapter(String utteranceId) {
            return utteranceId.startsWith(Constants.PREFIX_OF_CHAPTER_UTTERANCE);
        }
    }


}
