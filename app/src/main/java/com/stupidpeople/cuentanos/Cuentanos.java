package com.stupidpeople.cuentanos;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.stupidpeople.cuentanos.book.BookContability;
import com.stupidpeople.cuentanos.book.BookSummary;

/**
 * Created by Milenko on 03/08/2016.
 */
public class Cuentanos extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Chapter.class);
        ParseObject.registerSubclass(BookSummary.class);
        ParseObject.registerSubclass(BookContability.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(Constantes.APP_ID)
                .clientKey(Constantes.CLIENT_KEY)
                        .enableLocalDataStore()
                        .server("https://parseapi.back4app.com")
                        .build()
        );

    }
}
