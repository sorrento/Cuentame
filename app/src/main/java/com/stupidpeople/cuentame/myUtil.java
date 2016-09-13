package com.stupidpeople.cuentame;

/**
 * Created by halatm on 08/09/2016.
 */
public class myUtil {
    static String shortenText(String s, int n) {
        return s.length() < n ? s : s.substring(0, n);
    }
}
