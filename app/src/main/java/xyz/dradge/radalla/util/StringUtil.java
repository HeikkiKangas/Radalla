package xyz.dradge.radalla.util;

public class StringUtil {
    public static String capitalizeString(String s) {
        if (s.length() > 1) return s.substring(0, 1).toUpperCase() + s.substring(1);
        return s.toUpperCase();
    }
}
