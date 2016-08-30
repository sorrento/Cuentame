package com.stupidpeople.cuentame;

import com.parse.ParseException;

/**
 * Created by Milenko on 30/08/2016.
 */
public interface ChapterCB2 {
    void onDone(Chapter chapter);

    void onError(String msg, ParseException e);
}
