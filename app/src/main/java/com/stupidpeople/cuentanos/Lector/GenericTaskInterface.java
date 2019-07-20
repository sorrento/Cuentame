package com.stupidpeople.cuentanos.Lector;

import com.parse.ParseException;

public interface GenericTaskInterface {
    void onDone();

    void onError(String text, ParseException e);
}
