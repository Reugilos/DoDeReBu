/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model;

import dodecagraphone.ui.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * [CA] Cercle de quintes per a la navegació entre tonalitats.
 * Manté dues llistes estàtiques (major i menor) amb les 12 tonalitats ordenades
 * en cercle de quintes, i ofereix mètodes per navegar-hi (next/prev), obtenir
 * una tonalitat aleatòria i calcular la relativa.
 * <p>
 * [EN] Circle of fifths for navigating between tonalities.
 * Maintains two static lists (major and minor) with the 12 keys ordered in
 * the circle of fifths, and provides methods to navigate them (next/prev),
 * obtain a random key, and calculate the relative key.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyKeyCircles {

    private static List<String> major = Arrays.asList(new String[]{"Do", "Fa", "Li", "Ri", "Sa", "De", "Fo", "Ti", "Mi", "La", "Re", "So"});
    private static List<String> minor = Arrays.asList(new String[]{"la", "re", "so", "do", "fa", "li", "ri", "sa", "de", "fo", "ti", "mi"});
    private static int currentMajor = 0;
    private static int currentMinor = 0;

    /**
     * [CA] Retorna la primera tonalitat major del cercle i reinicia el cursor.
     * <p>
     * [EN] Returns the first major key in the circle and resets the cursor.
     *
     * @return [CA] nom de la primera tonalitat major / [EN] name of the first major key
     */
    public static String firstM() {
        currentMajor = 0;
        return major.get(currentMajor);
    }

    /**
     * [CA] Retorna la primera tonalitat menor del cercle i reinicia el cursor.
     * <p>
     * [EN] Returns the first minor key in the circle and resets the cursor.
     *
     * @return [CA] nom de la primera tonalitat menor / [EN] name of the first minor key
     */
    public static String firstm() {
        currentMinor = 0;
        return minor.get(currentMinor);
    }

//    public static String nextM() {
//        currentMajor++;
//        if (currentMajor == major.size()) {
//            currentMajor = 0;
//        }
//        return major.get(currentMajor);
//    }
//
//    public static String nextm() {
//        currentMinor++;
//        if (currentMinor == minor.size()) {
//            currentMinor = 0;
//        }
//        return minor.get(currentMinor);
//    }
//
//    public static String prevM() {
//        currentMajor--;
//        if (currentMajor == -1) {
//            currentMajor = major.size() - 1;
//        }
//        return major.get(currentMajor);
//    }
//
//    public static String prevm() {
//        currentMinor--;
//        if (currentMinor == -1) {
//            currentMinor = minor.size() - 1;
//        }
//        return minor.get(currentMinor);
//    }

    /**
     * [CA] Retorna la tonalitat següent en el cercle de quintes (seguint l'ordre
     * major → menor i tornat al principi). Si la tonalitat actual no es troba,
     * retorna una cadena buida.
     * <p>
     * [EN] Returns the next key in the circle of fifths (following the order
     * major → minor and back to the beginning). If the current key is not found,
     * returns an empty string.
     *
     * @param oldKey [CA] tonalitat actual / [EN] current key
     * @return [CA] tonalitat següent / [EN] next key
     */
    public static String next(String oldKey) {
        int pos = major.indexOf(oldKey);
        if (pos >= 0 && pos < major.size() - 1) {
            return major.get(pos + 1);
        } else if (pos == major.size() - 1) {
//            return major.get(0);
            return minor.get(0);
        } else if (pos == -1) {
            pos = minor.indexOf(oldKey);
            if (pos >= 0 && pos < minor.size() - 1) {
                return minor.get(pos + 1);
            } else if (pos == minor.size() - 1) {
//                return minor.get(0);
                return major.get(0);
            } else if (pos == -1) {
                return "";
            }
        }
        return "";
    }

    /**
     * [CA] Retorna la tonalitat anterior en el cercle de quintes (seguint l'ordre
     * invers menor → major). Si la tonalitat actual no es troba, retorna una cadena buida.
     * <p>
     * [EN] Returns the previous key in the circle of fifths (following the reverse
     * minor → major order). If the current key is not found, returns an empty string.
     *
     * @param oldKey [CA] tonalitat actual / [EN] current key
     * @return [CA] tonalitat anterior / [EN] previous key
     */
    public static String prev(String oldKey) {
        int pos = major.indexOf(oldKey);
        if (pos >= 1 && pos < major.size()) {
            return major.get(pos - 1);
        } else if (pos == 0) {
//            return major.get(major.size()-1);
            return minor.get(major.size()-1);
        } else if (pos == -1) {
            pos = minor.indexOf(oldKey);
            if (pos >= 1 && pos < minor.size()) {
                return minor.get(pos - 1);
            } else if (pos == 0) {
//                return minor.get(minor.size()-1);
                return major.get(minor.size()-1);
            } else if (pos == -1) {
                return "";
            }
        }
        return "";
    }

    /**
     * [CA] Retorna una tonalitat aleatòria (major o menor).
     * <p>
     * [EN] Returns a random key (major or minor).
     *
     * @return [CA] nom d'una tonalitat aleatòria / [EN] name of a random key
     */
    public static String rand(){
        List<String> all = new ArrayList<>();
        all.addAll(major);
        all.addAll(minor);
        Random rand = Utilities.getRand();
        int pos = rand.nextInt(all.size());
        return all.get(pos);
    }

    /**
     * [CA] Retorna una tonalitat major aleatòria.
     * <p>
     * [EN] Returns a random major key.
     *
     * @return [CA] nom d'una tonalitat major aleatòria / [EN] name of a random major key
     */
    public static String randM(){
        Random rand = Utilities.getRand();
        int pos = rand.nextInt(major.size());
        return major.get(pos);
    }

    /**
     * [CA] Retorna una tonalitat menor aleatòria.
     * <p>
     * [EN] Returns a random minor key.
     *
     * @return [CA] nom d'una tonalitat menor aleatòria / [EN] name of a random minor key
     */
    public static String randm(){
        Random rand = Utilities.getRand();
        int pos = rand.nextInt(minor.size());
        return minor.get(pos);
    }

    /**
     * [CA] Indica si la tonalitat donada és major (comença per majúscula).
     * <p>
     * [EN] Indicates whether the given key is major (starts with an uppercase letter).
     *
     * @param key [CA] nom de la tonalitat / [EN] key name
     * @return [CA] {@code true} si és major, {@code false} si és menor / [EN] {@code true} if major, {@code false} if minor
     */
    public static boolean isMajor(String key){
        return Character.isUpperCase(key.charAt(0));
    }

    /**
     * [CA] Retorna la tonalitat relativa de la donada (major → relativa menor, menor → relativa major).
     * <p>
     * [EN] Returns the relative key of the given key (major → relative minor, minor → relative major).
     *
     * @param key [CA] nom de la tonalitat d'entrada / [EN] input key name
     * @return [CA] nom de la tonalitat relativa / [EN] name of the relative key
     */
    public static String relativeKey(String key){
        if (isMajor(key)){
            int pos = major.indexOf(key);
            return minor.get(pos);
        }
        else {
            int pos = minor.indexOf(key);
            return major.get(pos);
        }
    }

    public static void main(String[] args){
        String key = "Ti";
        key = relativeKey(key);
        key = "la";
        key = relativeKey(key);
    }
}
