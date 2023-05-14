package com.stupidpeople.cuentanos.Lector;

import com.stupidpeople.cuentanos.book.Chapter;
import com.stupidpeople.cuentanos.utils.myLog;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


class AudioUtils {


    static void saveTextos2txt(List<Chapter> chapters, String outputFile) {
        //crea ficheros para luego hacer el tf-idf en R
        FileWriter writer;

        try {
            writer = new FileWriter(outputFile);

            for (Chapter chap : chapters) {
                writer.append(chap.getText());
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            myLog.error("Guardando el archivo de texto que es el contenido de los chapters", e);
        }
    }
}
