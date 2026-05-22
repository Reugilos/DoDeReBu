/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * [CA] Classe d'utilitats generals per a operacions aleatòries i manipulació
 * bàsica de dades. Proporciona mètodes per fer seleccions aleatòries, llençar
 * una moneda, barrejar o rotar arrays, capitalitzar text i trobar valors mínims
 * o màxims dins de llistes. Està pensada com a suport genèric per a altres
 * components de l'aplicació.
 * <p>
 * [EN] General-purpose utilities class for random operations and basic data
 * manipulation. Provides methods for random selection, coin tossing, array
 * shuffling and rotation, text capitalisation, and finding minimum or maximum
 * values in lists. Intended as a generic helper for other application
 * components.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class Utilities {

    private static final byte RAND_SEED = (byte) System.currentTimeMillis();
    private static final Random rand = new Random(RAND_SEED);
    private static int oldrand = -1;

    /**
     * [CA] Retorna un valor aleatori de la llista proporcionada, evitant
     * repetir el darrer valor retornat.
     * <p>
     * [EN] Returns a random value from the provided list, avoiding repetition
     * of the last returned value.
     *
     * @param choice [CA] llista d'enters entre els quals escollir /
     *               [EN] list of integers to choose from
     * @return [CA] un valor aleatori diferent de l'anterior /
     *         [EN] a random value different from the last one
     */
    public static int randFromList(List<Integer> choice) {
        int pos = rand.nextInt(choice.size());
        while (pos == oldrand) pos = rand.nextInt(choice.size());
        oldrand = pos;
        return choice.get(pos);
    }

    /**
     * [CA] Imprimeix un missatge per consola si {@code print} és cert,
     * amb la prioritat màxima.
     * <p>
     * [EN] Prints a message to the console if {@code print} is true,
     * with maximum priority.
     *
     * @param print [CA] true per imprimir / [EN] true to print
     * @param text  [CA] missatge a imprimir / [EN] message to print
     */
    public static void printOutWithPriority(boolean print, String text) {
        if (print) printOutWithPriority(1,text);
    }

    /**
     * [CA] Imprimeix un missatge per consola si la prioritat indicada és
     * inferior o igual a {@link Settings#PRINT_OUT_PRIORITY}. El missatge
     * inclou el nom del fil i el timestamp.
     * <p>
     * [EN] Prints a message to the console if the given priority is less than
     * or equal to {@link Settings#PRINT_OUT_PRIORITY}. The message includes
     * the thread name and a timestamp.
     *
     * @param priority [CA] nivell de prioritat (1 = sempre, valors alts = debug) /
     *                 [EN] priority level (1 = always, high values = debug)
     * @param text     [CA] missatge a imprimir / [EN] message to print
     */
    public static void printOutWithPriority(int priority, String text) {
        if (priority <= Settings.PRINT_OUT_PRIORITY) {
            String time = String.format("%06d", System.currentTimeMillis() % 1000000);
            String thread = Thread.currentThread().getName();
            synchronized (System.out) {
                System.out.println("("+thread+", "+time+") " + priority + " " + text);
            }
        }
    }

    /**
     * [CA] Simula una tirada de moneda.
     * <p>
     * [EN] Simulates a coin toss.
     *
     * @return [CA] true si surt cara; false si surt creu /
     *         [EN] true for heads; false for tails
     */
    public static boolean tossCoin() {
        int val = rand.nextInt(2);
        return val == 1;
    }

    /**
     * [CA] Barreja els elements d'un array enter en ordre aleatori.
     * <p>
     * [EN] Shuffles the elements of an integer array into random order.
     *
     * @param array [CA] l'array a barrejar / [EN] the array to shuffle
     */
    public static void shuffle(Integer[] array) {
        List<Integer> list = new ArrayList<>(Arrays.asList(array));
        for (int i = 0; i < array.length; i++) {
            int next = ((ArrayList<Integer>) list).remove(rand.nextInt(list.size()));
            array[i] = next;
        }
    }

    /**
     * [CA] Retorna l'objecte {@link Random} intern.
     * <p>
     * [EN] Returns the internal {@link Random} object.
     *
     * @return [CA] instància de Random / [EN] Random instance
     */
    public static Random getRand() {
        return rand;
    }

    /**
     * [CA] Suma una constant a tots els valors d'un array.
     * <p>
     * [EN] Adds a constant value to all elements in an array.
     *
     * @param array [CA] l'array d'enters / [EN] the array of integers
     * @param k     [CA] la constant a afegir / [EN] the constant to add
     */
    public static void arrayPlusConst(int[] array, int k) {
        for (int i = 0; i < array.length; i++) {
            array[i] += k;
        }
    }

    /**
     * [CA] Crea una nova llista on cada element és la suma de l'element
     * original i la constant {@code k}.
     * <p>
     * [EN] Creates a new list where each element is the sum of the original
     * element and the constant {@code k}.
     *
     * @param list [CA] llista d'origen / [EN] source list
     * @param k    [CA] la constant a afegir / [EN] the constant to add
     * @return [CA] nova llista amb els valors incrementats /
     *         [EN] new list with incremented values
     */
    public static List<Integer> listPlusConst(List<Integer> list, int k){
        List<Integer> newList = new ArrayList<>();
        for (int i=0;i<list.size();i++){
            newList.add(i,list.get(i)+k);
        }
        return newList;
    }

    /**
     * [CA] Imprimeix un array d'enters per consola en format [a,b,c,...].
     * <p>
     * [EN] Prints an array of integers to the console in [a,b,c,...] format.
     *
     * @param array [CA] l'array a imprimir / [EN] the array to print
     */
    public static void printArray(int[] array) {
        System.out.print("[");
        for (int i = 0; i < array.length - 1; i++) {
            System.out.print(array[i] + ",");
        }
        if (array.length > 0) {
            System.out.print(array[array.length - 1]);
        }
        System.out.println("]");
    }

    /**
     * [CA] Rota cap a l'esquerra un array un nombre determinat de posicions.
     * Cada rotació mou l'element 0 al final.
     * <p>
     * [EN] Rotates an array to the left by a specified number of positions.
     * Each rotation moves element 0 to the end.
     *
     * @param n     [CA] nombre de rotacions / [EN] number of rotations
     * @param array [CA] l'array a rotar / [EN] the array to rotate
     */
    public static void rotateArray(int n, int[] array) {
        for (int i = 0; i < n; i++) {
            int aux = array[0];
            for (int j = 1; j < array.length; j++) {
                array[j - 1] = array[j];
            }
            array[array.length - 1] = aux;
        }
    }

    /**
     * [CA] Capitalitza el primer caràcter d'un text i deixa la resta intacta.
     * <p>
     * [EN] Capitalises the first character of a string and leaves the rest
     * unchanged.
     *
     * @param text [CA] text d'entrada / [EN] input text
     * @return [CA] el text amb la primera lletra en majúscula /
     *         [EN] text with the first letter capitalised
     */
    public static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * [CA] Troba el valor màxim dins d'una llista. Retorna 0 si la llista és
     * null o buida.
     * <p>
     * [EN] Finds the maximum value in a list. Returns 0 if the list is null
     * or empty.
     *
     * @param list [CA] llista d'enters / [EN] list of integers
     * @return [CA] valor màxim / [EN] maximum value
     */
    public static int max(List<Integer> list) {
        if (list==null || list.isEmpty()) return 0;
        int mx = list.get(0);
        for (int val : list) {
            if (val > mx) {
                mx = val;
            }
        }
        return mx;
    }

    /**
     * [CA] Troba el valor mínim dins d'una llista. Retorna 0 si la llista és
     * null o buida.
     * <p>
     * [EN] Finds the minimum value in a list. Returns 0 if the list is null
     * or empty.
     *
     * @param list [CA] llista d'enters / [EN] list of integers
     * @return [CA] valor mínim / [EN] minimum value
     */
    public static int min(List<Integer> list) {
        if (list==null || list.isEmpty()) return 0;
        int mi = list.get(0);
        for (int val : list) {
            if (val < mi) {
                mi = val;
            }
        }
        return mi;
    }

//    public static Integer[] listToArray(List<Integer> llista){
//        Integer[] array = new Integer[llista.size()];
//        if (llista==null||llista.isEmpty()) return null;
//        for (int i=0; i<llista.size(); i++){
//            array[i]=llista.get(i);
//        }
//        return array;
//    }
}
