package com.stupidpeople.cuentanos.ui;

public interface NotificationInterface {

    void updateWithNewChapter();

    void updateBecauseStopped();

    void updateBecauseStarted();

    void updateBecauseNewBookLoaded();
}
