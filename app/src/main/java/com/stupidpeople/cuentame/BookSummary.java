package com.stupidpeople.cuentame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by Milenko on 22/08/2016.
 */
@ParseClassName("librosSum")
public class BookSummary extends ParseObject {
    public String fakeAuthor() {
        return getString("fakeAuthor");
    }

    public String fakeTitle() {
        return getString("fakeTitle");
    }


    public int nChapters() {
        return getInt("nCapitulos");
    }

    public String author() {
        return getString("author");
    }


    public String title() {
        return getString("title");
    }

    public String imageURL() {
        final ParseFile image = getParseFile("image");
        return image.getUrl();
    }

    public Bitmap getImageBitmap() {

        Bitmap bm = null;
        try {
            byte[] bitmapdata = getParseFile("image").getData();
            bm = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return bm;
    }

    public int getId() {
        return getInt("libroId");
    }
}
