package com.stupidpeople.cuentame;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Milenko on 03/08/2016.
 */
public class Cuentame extends Application {

    //    protected static final String APP_ID = "CADa4nX2Lx29QEJlC3LUY1snbjq9zySlF5S3YSVG"; old, from parse
    //    protected static final String CLIENT_KEY = "hC9VWCmGEBxb9fSGQPiOjSInaAPnYMZ0t8k3V0UO";
    protected static final String APP_ID = "osZTTBy5RTLsYv1I9Fgma8LfYnU2vn307cg82jDu";
    protected static final String CLIENT_KEY = "xMOLB4k6wxObRnle0B990HXH75GQCX1QNl9Txf0i";

    @Override
    public void onCreate() {
        super.onCreate();
//        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(Chapter.class);
        ParseObject.registerSubclass(BookSummary.class);
        ParseObject.registerSubclass(BookContability.class);
//        Parse.initialize(this, APP_ID, CLIENT_KEY);

        Parse.initialize(new Parse.Configuration.Builder(this)
                        .applicationId(APP_ID)
                        .clientKey(CLIENT_KEY)
                        .enableLocalDataStore()
                        .server("https://parseapi.back4app.com")
                        .build()
        );

    }
}
