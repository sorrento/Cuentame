package com.stupidpeople.cuentame;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Milenko on 03/08/2016.
 */
public class Cuentame extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(Chapter.class);
        ParseObject.registerSubclass(Book.class);
//        ParseObject.registerSubclass(WifiSpot.class);
        Parse.initialize(this, "CADa4nX2Lx29QEJlC3LUY1snbjq9zySlF5S3YSVG", "hC9VWCmGEBxb9fSGQPiOjSInaAPnYMZ0t8k3V0UO");
    }
}
