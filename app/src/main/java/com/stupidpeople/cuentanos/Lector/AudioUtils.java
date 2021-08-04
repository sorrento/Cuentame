package com.stupidpeople.cuentanos.Lector;

import android.content.Context;
import android.util.Log;

import com.stupidpeople.cuentanos.book.Chapter;
import com.stupidpeople.cuentanos.utils.myLog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

class AudioUtils {

    static void wav2mp3(String path, String wavFileName, Context context, IConvertCallback callback) {

        if (wavFileName != null) {

            File wavFile = new File(path + "/" + wavFileName + ".wav");

            AndroidAudioConverter.with(context)
                    .setFile(wavFile)
                    // Your desired audio format
                    .setFormat(AudioFormat.MP3)
                    // An callback to know when conversion is finished
                    .setCallback(callback)
                    .convert();
        } else {
            Log.d("MY", "Hemos terminado de convertir a mp3 todos los wav");
        }


    }


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
