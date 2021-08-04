package com.stupidpeople.cuentanos.ui;

import com.stupidpeople.cuentanos.book.Book;
import com.stupidpeople.cuentanos.book.BookSummary;
import com.stupidpeople.cuentanos.book.Chapter;

public interface NotificationInterface {

    void updateWithNewChapter(Chapter currentChapter, Book book1);

    void updateBecauseStopped();

    void updateBecauseStarted();

    void updateBecauseNewBookLoaded(BookSummary bookSummary, Book book1, int nBookAvailable);
}
