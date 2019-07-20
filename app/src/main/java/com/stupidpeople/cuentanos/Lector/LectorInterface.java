package com.stupidpeople.cuentanos.Lector;

interface LectorInterface {

    // acciones

    void resumeReadingNewSession();

    void accionLeeLoQueToca();

    void accionStopReading();

    void shutUp();

    void shutdownVoice();

    boolean isReading();
}
