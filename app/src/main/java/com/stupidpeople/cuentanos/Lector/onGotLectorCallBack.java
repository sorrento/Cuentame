package com.stupidpeople.cuentanos.Lector;

public interface onGotLectorCallBack {
    void gotLector();

    void startSpeaking(int chapterId);

    void stopSpeaking();
}
