package com.stupidpeople.cuentanos.Lector;

public interface GenericTaskInterface {
    void onDone();

    void onError(String text, Exception e);
}
