package com.stupidpeople.cuentanos.book;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.stupidpeople.cuentanos.utils.myLog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Milenko on 22/08/2016.
 */
@ParseClassName("librosSum")
public class BookSummary extends ParseObject {
    private Bitmap bm = null;

    public String getFakeAuthor() {
        return isMusic() ? getAuthor() : getString("fakeAuthor");
    }

    public String fakeTitle() {
        return isMusic() ? getTitle() : getString("fakeTitle");
    }

    public int getNChapters() {
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

        if (isMusic()) {
            if (bm != null) {
                return bm;

            } else {
                boolean loadedOk = false;
//                Random random = new Random();
//                int i;
                URL url;
                try {
                    url = new URL("http://fotos.musicaimg.com/minifotos/0_" + getId() + "_" + "1" + ".jpg");
                    bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    myLog.error("url para imagen", e);
                } catch (IOException e) {
                    myLog.error("cargando imagen para el grupo.", e);
                }
//                do {
////                    i = random.nextInt(5)+1;
//                    i = 1;
//                    try {
//                        url = new URL("http://fotos.musicaimg.com/minifotos/0_" + getId() + "_" + i + ".jpg");
//                        bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//                        loadedOk = true;
//                    } catch (MalformedURLException e) {
//                        myLog.error("url para imagen", e);
//                    } catch (IOException e) {
//                        myLog.error("cargando imagen para el grupo.", e);
//                    }
//                } while (loadedOk);

            }
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

    public void setLike(boolean b) {
        put("like", b);
    }

    public String getLanguage() {
        String language = getString("idioma");
        if (language == null) language = "ES";
        return language;
    }

    @Override
    public String toString() {
        return "BookSummary{" +
                "Autor=" + getAuthor() + ", " +
                "Title=" + getTitle() + ", " +
                "nChapters=" + getNChapters() + ", " +
                "Id=" + getId() +
                '}';
    }


}
