package com.stupidpeople.cuentame;

import com.parse.ParseException;

/**
 * Created by Milenko on 22/08/2016.
 */
public interface BookSumCallback {
    void onReceived(BookSummary bookSummary);

    void onError(String text, ParseException e);
}
