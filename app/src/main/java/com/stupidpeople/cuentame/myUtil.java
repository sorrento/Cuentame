package com.stupidpeople.cuentame;

/**
 * Created by halatm on 08/09/2016.
 */
public class myUtil {
    static String shortenText(String s, int n) {
        return s.length() < n ? s : s.substring(0, n);
    }

    static String processForReading(String t) {
//        myLog.add("before:" + t, "ree");

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
}
