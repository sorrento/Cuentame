package com.stupidpeople.cuentanos.diccionario;

import android.support.annotation.NonNull;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.stupidpeople.cuentanos.book.ArrayCallback;
import com.stupidpeople.cuentanos.book.ParseHelper;
import com.stupidpeople.cuentanos.book.palabraDiccionario;
import com.stupidpeople.cuentanos.utils.myLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DiccionarioUtils {

    public static final String DIC = "dic";

    public static void definePalabra(String palabra, String idioma, fetchUrl.fetchingResults callback) {
        String url = getUrl(palabra, idioma);
        (new fetchUrl(url, callback)).execute();
    }

    private static String getUrl(String texto, String idioma) {
        return "https://googledictionaryapi.eu-gb.mybluemix.net/?define=" + texto + "&lang=" + idioma;
    }

    public static ArrayList<String> ejemploSeparaPalabras(String text) {


        String[] words = text.split("\\W+");
        //repetidas
        HashMap<String, Integer> mapa  = new HashMap<>();
        ArrayList<String>        fuera = new ArrayList<>();
        ArrayList<String>        ok    = new ArrayList<>();
        ArrayList<String>        repe  = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!(word.matches("[a-ú]+"))) {
                fuera.add(word);
                continue;
            }
            if (mapa.containsKey(word)) {
                mapa.put(word, mapa.get(word) + 1);
            } else {
                mapa.put(word, 1);
            }
        }

        Iterator<Map.Entry<String, Integer>> it = mapa.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pair = it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            if (pair.getValue() == 1) {
                ok.add(pair.getKey());
            } else {
                repe.add(pair.getKey());
            }
        }
        return ok;
    }

    public static void topPalabrasRaras(String text, final ArrayCallback cb) {
        final ArrayList<String> palabras = ejemploSeparaPalabras(text);

        ParseHelper.Diccionario(palabras, new FindCallback<palabraDiccionario>() {
            @Override
            public void done(List<palabraDiccionario> objects, ParseException e) {
                ArrayList<String> noEncontradas;
                ArrayList<String> encontradas;
                ArrayList<Double> encontradasScore;

                if (e == null) {
                    noEncontradas = palabras;
                    encontradas = new ArrayList<>();
                    encontradasScore = new ArrayList<>();
                    for (palabraDiccionario pp : objects) {
                        encontradas.add(pp.getWord());
                        encontradasScore.add(pp.getScore());
                    }
                    noEncontradas.removeAll(encontradas);
                    for (String pal : noEncontradas) {
                        //noEncontradasTreemap.put(pal, (double) 0);
                        encontradasScore.add(0, (double) 0);
                    }
                    noEncontradas.addAll(encontradas);

                    myLog.add("no encotradas: " + noEncontradas.toString(), DIC);

                    //lista ascendente por ranking:
                    List<String> best       = noEncontradas.subList(0, 10);
                    List<Double> bestScores = encontradasScore.subList(0, 10);
                    cb.onDone(best, bestScores);
                } else {
                    myLog.add("ERR", DIC);
                }
            }
        });
    }

    @NonNull
    public static String getSampleChapterText() {
        ArrayList<String> msg = new ArrayList<>();

        msg.add("Volvemos a toparnos con el mujeriego Watson, que detecta la belleza femenina con olfato y discreción y, ¡sorpresa!, con un gesto en Sherlock ante Watson que ha llevado a más de un investigador a sospechar sobre su homosexualidad. Vean y juzguen:\n" +
                "«... y poniendo las manos sobre mis rodillas, me miró a los ojos con aquella mirada peculiarmente maliciosa que caracterizaba sus momentos de mayor picardía.\n" +
                "—Watson —dijo—, creo recordar que suele usted venir armado a estas excursiones nuestras».\n" +
                "Y un poco más adelante, de nuevo Holmes:\n" +
                "«No creo que en todas nuestras aventuras nos hayamos encontrado jamás con un ejemplo tan extraño de lo que puede hacer el amor pervertido».\n" +
                "Este relato debería figurar en todas las antologías. Si no lo hace, me temo que es por sus interioridades políticamente incorrectas.\n" +
                "* * *\n" +
                "Volver al texto\n" +
                "49. La aventura del colegio Priory\n" +
                "El hecho se desarrolla desde el jueves 16 al sábado 18 de mayo de 1901. Holmes tiene 47 años y Watson 53.\n" +
                "Publicado originalmente en la revista The Strand Magazine, en su número de febrero de 1904. Pertenece al libro El regreso de Sherlock Holmes, Londres, George Newnes, Ltd., 1905.\n" +
                "Existen dos casos intermedios. Véase relación.");
        msg.add("—Casi podría ser un símbolo de la Gran Bretaña —dijo—, con su absoluta concentración y su aspecto general de confortable somnolencia. ¡Bien, Von Bork, au revoir!\n" +
                "Haciendo un último saludo con la mano, se introdujo en el coche; un momento después, los dos conos dorados de los faros se dispararon a través de la oscuridad. El secretario se recostó en los cojines de la lujosa limusina, con su pensamiento tan absorto en la inminente tragedia europea que ni se dio cuenta de que, al torcer para tomar la calle del pueblo, su automóvil estuvo a punto de chocar con un pequeño Ford que venía en dirección contraria.");
        msg.add("—No es nada difícil engañarlos —comentó—. No es posible imaginar gente más dócil y más simple.\n" +
                "—No estoy tan seguro de eso —dijo el otro, pensativo—. Tienen limitaciones sorprendentes y hay que aprender a tenerlas en cuenta. Esa misma simplicidad superficial constituye una verdadera trampa para el extranjero. La primera impresión que uno se lleva es que son absolutamente blandos. Y de pronto, uno tropieza con algo muy duro y se da cuenta de que ha llegado al límite y que tiene que adaptarse a esa realidad. Tienen, por ejemplo, esos convencionalismos insulares que, simplemente, hay que respetar.\n" +
                "—¿Se refiere usted a los «buenos modales» y todas esas cosas? —preguntó Von Bork con un suspiro, como quien ha tenido que aguantar mucho.\n" +
                "—Me refiero a los prejuicios británicos en todas sus curiosas manifestaciones. Como ejemplo, podría citar uno de mis peores tropiezos. Puedo permitirme el lujo de hablar de mis tropiezos porque usted conoce mi trabajo lo suficientemente bien como para estar al corriente de mis éxitos. Sucedió la primera vez que vine. Me invitaron a pasar un fin de semana en la casa de campo de un ministro del Gobierno. Las conversaciones fueron increíblemente indiscretas.");
        msg.add("La acción transcurre el jueves 5 de abril de 1894. Holmes tiene 40 años y Watson 47. Publicado originalmente en la revista The Strand Magazine, en su número de octubre de 1903. Pertenece al libro El regreso de Sherlock Holmes, Londres, George Newnes, Ltd., 1905.\n" +
                "Los casi tres años en que Holmes permanece desaparecido han dado objeto a múltiples averiguaciones, siendo a su vez el lugar donde también muchos escritores de continuaciones y pastiches han querido depositar su discreto óbolo. En principio Sherlock no interviene en ningún caso salvo aquellos que relata a su amigo Watson, y entrarían dentro de otro apartado que se podría titular Las otras aventuras de Sherlock Holmes. No es nuestro cometido aquí describirlas más que lo que el propio investigador resume.");

//        return msg.get(new Random().nextInt(msg.size()));
        return msg.get(0);
    }

    public static String toParrafo(Definicion response) {

        int           i    = response.getNumberOfFuncionesGramaticales();
        StringBuilder sb   = new StringBuilder();
        String        word = response.getWord();

        if (i > 1) {
            sb.append("La palabra " + word + " tiene varias funciones gramaticales. \n");
        } else {
            sb.append("Veamos la definición de la palabra " + word + ".\n");
        }

        for (Definicion.TipoDefinicion funcion : response.getFuncionesGramaticales()) {
            ArrayList<Definicion.unaDefinicion> definiciones      = funcion.getDefinitions();
            String                              funcionGramatical = funcion.getFuncionGramatical();

            if (!funcionGramatical.equals("")) sb.append("Como " + funcionGramatical);

            int size = definiciones.size();
            if (size > 1) sb.append(" tiene " + size + " significados. ");

            for (Definicion.unaDefinicion unaDefinicion : definiciones) {
                sb.append(unaDefinicion.getDefi());
                if (unaDefinicion.hasExample())
                    sb.append(" Por ejemplo, " + unaDefinicion.getEjemplo());
                sb.append("\n");
            }
        }
        return (sb.toString());
    }
}
