package com.stupidpeople.cuentanos.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.NotificationCompat;

import com.stupidpeople.cuentanos.Chapter;
import com.stupidpeople.cuentanos.ExceptionManager;
import com.stupidpeople.cuentanos.MainActivity;
import com.stupidpeople.cuentanos.R;
import com.stupidpeople.cuentanos.book.BookSummary;
import com.stupidpeople.cuentanos.utils.myLog;

public class NotificationHelper implements NotificationInterface {
    static final  String              LIKE      = "like";
    static final  String              PLAYPAUSE = "playpause";
    static final  String              NEXT      = "next";
    static final  String              STOP      = "stop";
    private final ActionsInterface    mni;
    private final NotificationManager nNotificationManager;


    private final EventsReceiver eventsReceiver = null;

    public NotificationHelper(ActionsInterface ui, NotificationManager notificationManager, MainActivity mainActivity) {
        mni = ui;
        nNotificationManager = notificationManager;
//        eventsReceiver = new MainActivity.EventsReceiver(mainActivity);
    }

    public void unregisterReceiver(MainActivity mainActivity) {
        mainActivity.unregisterReceiver(eventsReceiver);
    }

    public void showMediaNotification(Chapter currentChapter, BookSummary currentBook, boolean isReading,
                                      Context co, NotificationManager mNotificationManager) {

        Intent        like              = new Intent(LIKE);
        PendingIntent likePendingIntent = PendingIntent.getBroadcast(co, 1, like, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent        play              = new Intent(PLAYPAUSE);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(co, 1, play, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent        next              = new Intent(NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(co, 1, next, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent        stop              = new Intent(STOP);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(co, 1, stop, PendingIntent.FLAG_UPDATE_CURRENT);


        //int iconPlayPause = t1.isSpeaking() ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
        int iconPlayPause = isReading ? R.drawable.ic_book : R.drawable.ic_book;
//        int iconMusicBook = musicMode ? R.drawable.ic_book : R.drawable.ic_music_note_white_24dp;
//        int iconMusicBook = R.drawable.ic_book;

        String title   = currentChapter.isSong() ? currentChapter.getBookName() : currentBook.fakeTitle();
        String content = currentChapter.isSong() ? currentChapter.getAuthor() : currentBook.getFakeAuthor();

        Notification notification = new NotificationCompat.Builder(co)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_logo_white)
                // Add media control buttons that invoke intents in your media service
                //.addAction(R.drawable.ic_skip_previous_white_24dp, "Previous", likePendingIntent) // #0
                .addAction(R.drawable.ic_book, "Previous", likePendingIntent) // #0
                .addAction(iconPlayPause, "Pause", playPendingIntent)  // #1
                //.addAction(R.drawable.ic_skip_next_white_24dp, "Next", nextPendingIntent)     // #2
                .addAction(R.drawable.ic_book, "Next", nextPendingIntent)     // #2
//                .addAction(iconMusicBook, "MusicBook", musicBookPendingIntent)     // #3
////                        // Apply the media style template
                .setStyle(new NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(1 /* #1: pause button */)
//                        .setMediaSession( mMediaSession.getSessionToken()))
                )
                .setShowWhen(false)
                .setDeleteIntent(stopPendingIntent)
                .setContentTitle(title)
                .setContentText(content)
                .setSubText(currentChapter.getChapterId() + "/" + currentBook.getNChapters())
                //.setProgress(currentBook.getNChapters(), currentChapter.getChapterId(), false)
                .setProgress(0, 0, true)
                .setLargeIcon(currentBook.getImageBitmap())
                .setTicker(currentBook.fakeTitle() + "\n" + currentBook.getFakeAuthor())
                .build();

        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, notification);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(co);
        builder.setProgress(0, 0, true);
        mNotificationManager.notify(2, builder.build());
        //builder.setContentText("Download complete")
        //      .setProgress(0, 0, false);

        //manager.notify(NOTIF_ID, builder.build());
    }

    @Override
    public void updateWithNewChapter() {

    }

    @Override
    public void updateBecauseStopped() {

    }

    @Override
    public void updateBecauseStarted() {

    }

    @Override
    public void updateBecauseNewBookLoaded() {

    }

    /**
     * Para coger de la notificación
     */
    public static class EventsReceiver extends BroadcastReceiver {
        private final ActionsInterface actionsInterface;
        String tag = "AA";
        private MainActivity mainActivity;


        public EventsReceiver(MainActivity mainActivity, ActionsInterface actionsInterface) {
            this.mainActivity = mainActivity;
            this.actionsInterface = actionsInterface;

            IntentFilter filter = new IntentFilter(LIKE);
            filter.addAction(NEXT);
            filter.addAction(PLAYPAUSE);
            filter.addAction(LIKE);
            filter.addAction(STOP);

            mainActivity.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action;
            try {
                action = intent.getAction();
                myLog.add("action received: " + action, tag);
                switch (action) {
                    case LIKE:
                        actionsInterface.apretadoLikeOrBack();
                        break;
                    case PLAYPAUSE:
                        //TODO solucionar el playpause para notif
/*
                        if (status== playing) {
                            actionsInterface.apretadoPause();
                        } else {
                            actionsInterface.apretadoPlay();
                        }
*/
                        break;
                    case NEXT:
                        actionsInterface.apretadoNextOrHate();
                        break;
                    case STOP:
                        //todo completar ¿es esto el quitar la notif?
                        //                        t1.stop();
                        mainActivity.finish();
                        break;
                }
            } catch (Exception e) {
                ExceptionManager.ExceptionManager("Problema onReceive", e);
            }
        }
    }
}
