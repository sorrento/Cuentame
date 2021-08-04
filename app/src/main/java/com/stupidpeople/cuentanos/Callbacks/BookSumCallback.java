package com.stupidpeople.cuentanos.Callbacks;

import com.parse.ParseException;
import com.stupidpeople.cuentanos.book.BookSummary;

/**
 * Created by Milenko on 22/08/2016.
 */
public interface BookSumCallback {
    void onReceived(BookSummary bookSummary);

    void onError(String text, ParseException e);
}
