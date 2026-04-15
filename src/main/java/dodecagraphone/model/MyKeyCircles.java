package dodecagraphone.model;

import dodecagraphone.ui.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author grogm
 */
public class MyKeyCircles {

    private static List<String> major = Arrays.asList(new String[]{"Do", "Fa", "Li", "Ri", "Sa", "De", "Fo", "Ti", "Mi", "La", "Re", "So"});
    private static List<String> minor = Arrays.asList(new String[]{"la", "re", "so", "do", "fa", "li", "ri", "sa", "de", "fo", "ti", "mi"});
    private static int currentMajor = 0;
    private static int currentMinor = 0;

    public static String firstM() {
        currentMajor = 0;
        return major.get(currentMajor);
    }

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
    
    public static String rand(){
        List<String> all = new ArrayList<>();
        all.addAll(major);
        all.addAll(minor);
        Random rand = Utilities.getRand();
        int pos = rand.nextInt(all.size());
        return all.get(pos);
    }

    public static String randM(){
        Random rand = Utilities.getRand();
        int pos = rand.nextInt(major.size());
        return major.get(pos);
    }

    public static String randm(){
        Random rand = Utilities.getRand();
        int pos = rand.nextInt(minor.size());
        return minor.get(pos);
    }

    public static boolean isMajor(String key){
        return Character.isUpperCase(key.charAt(0));
    }
    
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
