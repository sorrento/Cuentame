package com.stupidpeople.cuentame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 * Created by Milenko on 22/08/2016.
 */
@ParseClassName("librosSum")
public class BookSummary extends ParseObject {
    public String fakeAuthor() {
        return isMusic() ? getAuthor() : getString("fakeAuthor");
    }

    public String fakeTitle() {
        return isMusic() ? getTitle() : getString("fakeTitle");
    }

    public int nChapters() {
        return getInt("nCapitulos");
    }

    public String getAuthor() {
        return getString("author");
    }

    public String getTitle() {
        return getString("title");
    }

    public String imageURL() {
        final ParseFile image = getParseFile("image");
        return image.getUrl();
    }

    public boolean isMusic() {
        return getBoolean("isMusic");
    }


    public Bitmap getImageBitmap() {

        Bitmap bm = null;

        if (isMusic()) {
            boolean loadedOk = false;
            Random random = new Random();
            int i;
            URL url;

            do {
                i = random.nextInt(30);
                try {
                    url = new URL("http://fotos.musicaimg.com/minifotos/0_" + getId() + "_" + i + ".jpg");
                    bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    loadedOk = true;
                } catch (MalformedURLException e) {
                    myLog.error("url para imagen", e);
                } catch (IOException e) {
                    myLog.error("cargando imagen para el grupo.", e);
                }
            } while (loadedOk);


        } else {

            try {
                byte[] bitmapdata = getParseFile("image").getData();
                bm = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }

    public int getId() {
        return getInt("libroId");
    }
}
