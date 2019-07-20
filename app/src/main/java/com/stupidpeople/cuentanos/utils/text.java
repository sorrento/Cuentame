package com.stupidpeople.cuentanos.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by halatm on 08/09/2016.
 */
public class text {
    public static String shortenText(String s, int n) {
        return s.length() < n ? s : s.substring(0, n);
    }

    public static String processForReading(String t) {
//        myLog.add("before:" + t, "ree");
        //todo poner una tabla con estos y recorrer o hacer
//        t = t.replaceAll("([\\w\\?]) —(\\w)", "$1, $2"); // el guión por coma
        t = t.replaceAll("([\\w\\?]) (—|-|–)(\\w)", "$1, $3"); // el guión por coma
        t = t.replaceAll("(-|—|–)", " "); //Los que quedan, por espacio
//        t = t.replaceAll("^(-|—|–) ?(¿|\\w+¡|)", "$2"); //al inicio con guion
//        t = t.replaceAll("\\n ?(-|—|–) ?(¿|\\w+¡|)", "\n$2"); //principio de linea con guion
        //falta que el guión final lo lea como coma
        t = t.replaceAll("¡", "");
//        t = t.replaceAll("(\\w+)\\n", "$1\\.\n"); //punto al final de la línea

        t = t.replaceAll("No\\.", "No . ");
        t = t.replaceAll("no\\.", "No . ");
        t = t.replaceAll("pie", "píe");
        t = t.replaceAll("local", "lokal");
        t = t.replaceAll("normal", " noormal");
        t = t.replaceAll("hospital", "ospital");
        t = t.replaceAll("Mr.", "míster");
        t = t.replaceAll("Mrs", "Mísis");


//        t = t.replaceAll("<<", "");
//        t = t.replaceAll(">>", "");
//        t = t.replaceAll("\\.\\.\\.\\.", "...");
//        t = t.replaceAll("\\.\\.", ".");
//        t = t.replaceAll(":\\.", ".");
//        t = t.replaceAll("’", "");

//        t = t.replaceAll("í\u00AD", "í");
//        t = t.replaceAll("í\u0081", "Á");
//        t = t.replaceAll(":â\u0080\u0094", "");
//        t = t.replaceAll("í¼", "ü");

        t = t.replaceAll("Patxi", "Páchi");


//        myLog.add("   after:" + t, "ree");

        return t;
    }

    static String processForReadingOLD(String text) {

        text = text.replaceAll(" —", ", ");
        text = text.replaceAll("—", "");
        text = text.replaceAll("-", "");//TODo cambiar por expresion regular que comprueba que viene una palabra
        text = text.replaceAll("–¿", "¿");
        text = text.replaceAll("–¡", "¡");
        text = text.replaceAll("<<", "");
        text = text.replaceAll(">>", "");
        text = text.replaceAll("\\.\\.\\.\\.", "...");
        text = text.replaceAll("\\.\\.", ".");
        text = text.replaceAll(":\\.", ".");
        return text;
    }

    /**
     * Checks if we have internet connection     *
     *
     * @param tag
     * @return
     */
    public static boolean isOnline(String tag, Context ct) {

        ConnectivityManager cm =
                (ConnectivityManager) ct.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean     b       = netInfo != null && netInfo.isConnectedOrConnecting();
        myLog.add("Checking connectivity: " + b, tag);

        return b;
    }
}
