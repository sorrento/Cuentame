package com.stupidpeople.cuentanos.Lector;

public abstract class ReaderEvents {

    private boolean bookOk  = false;
    private boolean voiceOk = false;

    public void bookReady() {
        bookOk = true;
        if (voiceOk) bookAndVoiceReady();
    }

    public abstract void bookAndVoiceReady();

    void voiceReady() {
        voiceOk = true;
        if (bookOk) bookAndVoiceReady();
    }

    public abstract void voiceStartedSpeakChapter();

    public abstract void voiceInterrupted();

    public abstract void voiceEndedReadingChapter();

    public abstract void error(String text, Exception e);
    //

    //    protected abstract void onInterruptionOfReading();
    //
//    protected abstract void onStartedSpeakingChapter(int chapterId, boolean fromLocalStorage, String texto);
//
//
//    public abstract void onStartedSpeaking(String utteranceId);
//
//    public abstract void OnBookChanged();
//
//    public void onChapterJustReaded() {

//}
}