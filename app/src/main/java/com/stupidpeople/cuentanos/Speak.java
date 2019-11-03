package com.stupidpeople.cuentanos;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;

import com.stupidpeople.cuentanos.Lector.TipoFrase;
import com.stupidpeople.cuentanos.book.Chapter;
import com.stupidpeople.cuentanos.utils.myLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class Speak {

    private static String tag;

    //TODO que los mensajes de voz estén en un archivo o en BBDD. Que se actualicen cuando se lee un libro entero
    //TODO poner en inggles tambien

    public static String generaMsgDondeMeQuede(@NonNull String lan) {
        final ArrayList<String> msg = new ArrayList<>();

        if (lan.equals("ES")) {
            msg.add("A ver donde me quedé... ");
            msg.add("Creo que iba por aquí...");
            msg.add("¿Qué fue lo último que te conté? ah, ya sé...");
            msg.add("A ver cómo era esto...");
            msg.add("Memoria, memoria, no me falles...");
            msg.add("¿Por dónde iba? Ah, sí, calla calla...");
            msg.add("Atento que ahora viene la parte crucial...");
            msg.add("Te eché de menos. Pero te estaba esperando...");
            msg.add("Te está enganchando, verdad? Espera a oir lo que viene...");
        }

        return msg.get(new Random().nextInt(msg.size()));
    }

    static String generaMsgTeGusta(@NonNull String lan) {

        final ArrayList<String> msg = new ArrayList<>();

        if (lan.equals("ES")) {
            msg.add("Bueno, ya que te gusta el relato, veamo si recuerdo cómo empezaba...");
            msg.add("Empecemos por el principio...");
            msg.add("Había una vez, hace mucho mucho mucho tiempo...");
            msg.add("Hace mucho tiempo, en una galaxia lejana...");
            msg.add("En algún lugar de la mancha, de cuyo nombre no quiero acordarme...");
            msg.add("¡Eso! Se empieza desde el principio");
            msg.add("Ahora te cuento lo que te has perdido.");
        }

        return msg.get(new Random().nextInt(msg.size()));
    }

    public static void speak(String s, boolean interrupting, String utterance, TextToSpeech t1) {

        // Parche por si el chapter es muy largo
        int endIndex = TextToSpeech.getMaxSpeechInputLength() - 10;
        if (s.length() > endIndex) {
            String s1 = s.substring(0, endIndex);
            String s2 = s.substring(endIndex + 1);
            speak(s1, false, "cortado 1", t1);
            speak(s2, false, Constants.PREFIX_OF_CHAPTER_UTTERANCE +
                    "cortado 2", t1);
            return;
        }

        int queueMode = interrupting ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            t1.speak(s, queueMode, null, utterance);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterance);
            t1.speak(s, queueMode, map);
        }
    }

    private static String generaBienvenida(String lan) {
        String welcome = "Hola, te voy a contar algunas de las historias que más me gustan. Espero que a ti también." +
                " Si no te gusta como suena mi voz, instala un sintetizador nuevo. Busca en el google play poniendo TTS." +
                " Vamos a ver...";

        return welcome;
    }

    static String generaMsgNoTeGusta(@NonNull String lan) {
        final ArrayList<String> msg = new ArrayList<>();

        if (lan.equals("ES")) {
            msg.add("Vaya, no te ha gustado. Probemos otra cosa..");
            msg.add("Uf, no le vas a dar una oportunidad?");
            msg.add("Bueno, en gustos no hay nada escrito..");
            msg.add("¡Hala! ¡Como si fuera mejor el libro que escribiste tú! ");
            msg.add("La verdad es que era un poco flojillo...");
            msg.add("¿No será mi voz la que no te gusta, verdad?");
            msg.add("No culpe al libro, eres tú el que se distrae...");
            msg.add("Mira, el próximo es güeno, güeno...");
            msg.add("A tomar por saco, veamos otro");
        }

        return msg.get(new Random().nextInt(msg.size()));
    }

    static public void setSpeakLanguage(TextToSpeech t, @NonNull String lan) {
        tag = "spk";
        myLog.add("Setting Language:" + lan, tag);

        switch (lan) {
            case "ES":
                Locale spanish = new Locale("es", "ES");
                t.setLanguage(spanish);
                break;

            case "EN":
                t.setLanguage(Locale.US);

                //Select voice
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Set<Voice> voices = t.getVoices();
                    for (Voice voice : voices) {
                        // if (voice.getName().equals("en-US-SMTl01")) {
                        if (voice.getQuality() == 400 && voice.getLocale() == Locale.US) {
                            myLog.add("voice set to: " + voice.toString(), tag);
                            t.setVoice(voice);
                            break;
                        }
                    }
                }
                break;
            //TODO manage unknown language
        }
    }

    public static void speakPredefinedPhrase(TipoFrase tipoFrase, String lan, TextToSpeech tts, boolean interrupt) {
        String txt = getSamplePhrase(tipoFrase, lan);
        speak(txt, interrupt, txt, tts);

    }

    public static void speakChapter(@NonNull Chapter chapter, TextToSpeech mtts) {
        speak(chapter.getProcessedText(), false, chapter.getUtterance(), mtts);
    }

    private static String getSamplePhrase(@NonNull TipoFrase tipoFrase, String lan) {
        String txt = "No sé qué decir";

        switch (tipoFrase) {
            case WELCOME:
                txt = generaBienvenida(lan);
                break;
            case RETOMEMOS:
                txt = generaMsgDondeMeQuede(lan);
                break;
            case A_OTRO_LIBRO:
                txt = generaMsgNoTeGusta(lan);
                break;
            case IR_A_PRINCIPIO:
                txt = generaMsgTeGusta(lan);
                break;
            case VAMOS_ALLA:
                txt = "vamos allá";
                break;
        }
        return txt;
    }
}
