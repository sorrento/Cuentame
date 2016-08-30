package com.stupidpeople.cuentame;

import com.parse.ParseException;

/**
 * Created by Milenko on 30/08/2016.
 */
public interface TaskDoneCallback2 {
    void onDone();

    void onError(String text, ParseException e);

}
