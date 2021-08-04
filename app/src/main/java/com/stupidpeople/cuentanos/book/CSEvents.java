package com.stupidpeople.cuentanos.book;

public interface CSEvents {
    void serviceReady();

    void bookEnded();

    void error(String text, Exception e);
}
