package dodecagraphone.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Aquesta classe ofereix utilitats diverses per a operacions aleatòries i manipulació bàsica de dades.
 * Proporciona mètodes per fer seleccions aleatòries, llençar una moneda, barrejar o rotar arrays,
 * capitalitzar text i trobar valors mínims o màxims dins de llistes.
 *
 * Està pensada com a suport genèric per a altres components de l’aplicació.
 * 
 * ------------------------------------------------------------------------------
 * 
 * This class provides various utilities for random operations and basic data manipulation.
 * It includes methods for random selection, coin tossing, array shuffling and rotation,
 * text capitalization, and finding minimum or maximum values in lists.
 *
 * It is intended as a general-purpose helper for other parts of the application.
 * 
 * @autor pau  
 * @author pau
 */
public class Utilities {

    private static final byte RAND_SEED = (byte) System.currentTimeMillis();
    private static final Random rand = new Random(RAND_SEED);
    private static int oldrand = -1;

    /**
     * Retorna un valor aleatori de la llista proporcionada, evitant repetir el darrer.
     * Returns a random value from the provided list, avoiding repetition of the previous one.
     *
     * @param choice Array d'enters entre els quals escollir. / Array of integers to choose from.
     * @return Un valor aleatori diferent de l'anterior. / A random value different from the last one.
     */
    public static int randFromList(List<Integer> choice) {
        int pos = rand.nextInt(choice.size());
        while (pos == oldrand) pos = rand.nextInt(choice.size());
        oldrand = pos;
        return choice.get(pos);
    }
    
    public static void printOutWithPriority(boolean print, String text) {
        if (print) printOutWithPriority(1,text);
    }

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
     * Simula una tirada de moneda.
     * Simulates a coin toss.
     *
     * @return true si surt cara; false si surt creu. / true for heads; false for tails.
     */
    public static boolean tossCoin() {
        int val = rand.nextInt(2);
        return val == 1;
    }

    /**
     * Barreja els elements d’un array enter.
     * Shuffles the elements of an integer array.
     *
     * @param array L'array a barrejar. / The array to shuffle.
     */
    public static void shuffle(Integer[] array) {
        List<Integer> list = new ArrayList<>(Arrays.asList(array));
        for (int i = 0; i < array.length; i++) {
            int next = ((ArrayList<Integer>) list).remove(rand.nextInt(list.size()));
            array[i] = next;
        }
    }

    /**
     * Retorna l'objecte Random intern.
     * Returns the internal Random object.
     *
     * @return Instància de Random. / Random instance.
     */
    public static Random getRand() {
        return rand;
    }

    /**
     * Suma una constant a tots els valors d’un array.
     * Adds a constant value to all elements in an array.
     *
     * @param array L'array d'enters. / The array of integers.
     * @param k La constant a afegir. / The constant to add.
     */
    public static void arrayPlusConst(int[] array, int k) {
        for (int i = 0; i < array.length; i++) {
            array[i] += k;
        }
    }

    public static List<Integer> listPlusConst(List<Integer> list, int k){
        List<Integer> newList = new ArrayList<>();
        for (int i=0;i<list.size();i++){
            newList.add(i,list.get(i)+k);
        }
        return newList;
    }
    /**
     * Imprimeix un array d'enters per consola.
     * Prints an array of integers to the console.
     *
     * @param array L'array a imprimir. / The array to print.
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
     * Rota cap a l'esquerra un array un nombre determinat de posicions.
     * Rotates an array to the left by a specified number of positions.
     *
     * @param n Nombre de rotacions. / Number of rotations.
     * @param array L'array a rotar. / The array to rotate.
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
     * Capitalitza el primer caràcter d’un text.
     * Capitalizes the first character of a string.
     *
     * @param text Text d’entrada. / Input text.
     * @return El text amb la primera lletra en majúscula. / Text with the first letter capitalized.
     */
    public static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Troba el valor màxim dins d'una llista.
     * Finds the maximum value in a list.
     *
     * @param list Llista d'enters. / List of integers.
     * @return Valor màxim. / Maximum value.
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
     * Troba el valor mínim dins d'una llista.
     * Finds the minimum value in a list.
     *
     * @param list Llista d'enters. / List of integers.
     * @return Valor mínim. / Minimum value.
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
