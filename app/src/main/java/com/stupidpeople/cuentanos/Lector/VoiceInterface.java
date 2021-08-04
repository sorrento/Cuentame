package com.stupidpeople.cuentanos.Lector;

import com.stupidpeople.cuentanos.book.Chapter;

interface VoiceInterface {

    void setLanguage(String newLan);

    void speakChapter(Chapter chapter, boolean interrumpir);

    void shutUp();

    void predefinedPhrases(TipoFrase tipoFrase, boolean interrupt);

}
