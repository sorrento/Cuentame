package com.stupidpeople.cuentanos.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.core.app.NotificationCompat;

import com.stupidpeople.cuentanos.R;
import com.stupidpeople.cuentanos.book.Book;
import com.stupidpeople.cuentanos.book.BookSummary;
import com.stupidpeople.cuentanos.book.Chapter;
import com.stupidpeople.cuentanos.utils.ExceptionManager;
import com.stupidpeople.cuentanos.utils.myLog;

public class UINotification extends UiGeneric {

    public static final  String                     tag        = "UI_NOTIF";
    private static final String                     CHANNEL_ID = "pipi";
    private static final String                     LIKE       = "like";
    private static final String                     PLAYPAUSE  = "playpause";
    private static final String                     NEXT       = "next";
    private static final String                     STOP       = "stop";
    private final        Context                    context;
    private final        NotificationManager        notificationManager;
    private final        EventsReceiver             eventsReceiver;
    private              PendingIntent              likePendingIntent;
    private              PendingIntent              playPausePendingIntent;
    private              PendingIntent              nextPendingIntent;
    private              PendingIntent              stopPendingIntent;
    private              NotificationCompat.Builder builder;
    private              BookSummary                currentBook;
    private              boolean                    isPlaying;
    private              Chapter                    currentChapter;
    private              int                        mnBookAvailable;

    public UINotification(ActionsInterface actionsInterface, NotificationManager notificationManager,
                          Context context) {
        super(actionsInterface);
        this.context = context;
        this.notificationManager = notificationManager;

        //Register receiver o notification player
        eventsReceiver = new EventsReceiver();
        IntentFilter filter = new IntentFilter(LIKE);
        filter.addAction(NEXT);
        filter.addAction(PLAYPAUSE);
        filter.addAction(LIKE);
        filter.addAction(STOP);
        context.registerReceiver(eventsReceiver, filter);

        createPendingIntents(context);
    }

    private void createPendingIntents(Context context) {
        likePendingIntent = PendingIntent.getBroadcast(context, 1, new Intent(LIKE), PendingIntent.FLAG_UPDATE_CURRENT);
        playPausePendingIntent = PendingIntent.getBroadcast(context, 1, new Intent(PLAYPAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
        nextPendingIntent = PendingIntent.getBroadcast(context, 1, new Intent(NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
        stopPendingIntent = PendingIntent.getBroadcast(context, 1, new Intent(STOP), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void updateWithNewChapter(Chapter currentChapter, Book book1) {
        this.currentChapter = currentChapter;
        Notification notification = builder.setSubText(currentChapter.getChapterId() + "/" +
                currentBook.getNChapters()).build();
        notificationManager.notify(1, notification);
    }

    @Override
    public void updateBecauseStopped() {
        isPlaying = false;
        refreshNotification();
    }

    @Override
    public void updateBecauseStarted() {
        isPlaying = true;
        refreshNotification();
    }

    private void refreshNotification() {
        StringBuilder stringBuilder = new StringBuilder(currentChapter.getChapterId() +
                "/" + currentBook.getNChapters());

        if (mnBookAvailable != 9999) {
            String str = " <" + mnBookAvailable + ">";
            stringBuilder.append(str);
        }

        builder = createBuilder(currentBook);

        Notification notification = builder.setSubText(stringBuilder.toString()).build();
        notificationManager.notify(1, notification);
    }

    @Override
    public void updateBecauseNewBookLoaded(BookSummary bookSummary, Book book1, int nBooksAvailable) {
        currentBook = bookSummary;
        builder = createBuilder(bookSummary);
        mnBookAvailable = nBooksAvailable;
    }

    private NotificationCompat.Builder createBuilder(BookSummary bookSummary) {

        int iconPlayPause = isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_white_24dp;

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_logo_white)
                // Add media control buttons that invoke intents in your media service
                .addAction(R.drawable.ic_previous_white_24dp, "Previous", likePendingIntent) // #0
                .addAction(iconPlayPause, "Pause", playPausePendingIntent)  // #1
                .addAction(R.drawable.ic_next_white_24dp, "Next", nextPendingIntent)     // #2
                // Apply the media style template
//                .setStyle(new android.support.v4.media.app.Notification.MediaStyle()
                .setStyle(new androidx.core.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(1 /* #1: pause button */)
//                        .setMediaSession(mediaSession.getSessionToken())
                )
                .setDeleteIntent(stopPendingIntent)
                .setShowWhen(false)

                .setContentTitle(bookSummary.fakeTitle())
                .setContentText(bookSummary.getFakeAuthor())
                .setLargeIcon(bookSummary.getImageBitmap())
                .setTicker(bookSummary.fakeTitle() + "\n" + bookSummary.getFakeAuthor());

        return b;
    }

    public void showmp3status(int i, int total) {
        BookSummary bookSummary = currentBook;

        String msg = "Procesado mp3 " + i + "/" + total;

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_logo_white)
                .setStyle(new androidx.core.media.app.NotificationCompat.MediaStyle())
                .setShowWhen(false)
                .setContentTitle(bookSummary.fakeTitle())
                .setContentText(bookSummary.getFakeAuthor())
                .setSubText(msg)
                .setLargeIcon(bookSummary.getImageBitmap())
                .setTicker(msg)
                .build();

        notificationManager.notify(1, notification);

    }

    /**
     * Para coger de la notificación
     */
    public class EventsReceiver extends BroadcastReceiver {

        public EventsReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action;

            try {
                action = intent.getAction();
                myLog.add("action received: " + action, tag);
                switch (action) {
                    case LIKE:
                        apretado(TipoBoton.PREV);
                        break;
                    case PLAYPAUSE:
                        if (isPlaying) {
                            apretado(TipoBoton.PAUSE);
                        } else {
                            apretado(TipoBoton.PLAY);
                        }
                        break;
                    case NEXT:
                        apretado(TipoBoton.NEXT);
                        break;
                    case STOP:
                        context.unregisterReceiver(eventsReceiver);
                        apretado(TipoBoton.STOP);
                        break;
                }
            } catch (Exception e) {
                ExceptionManager.ExceptionManager("Problema onReceive", e);
            }
        }
    }
}