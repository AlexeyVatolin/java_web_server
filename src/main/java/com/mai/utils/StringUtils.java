package com.mai.utils;

public class StringUtils {
    public static Object stringToNumber(String s, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(s);
        }
        else if (type == double.class || type == Double.class) {
            return Double.parseDouble(s);
        }
        else if (type == float.class || type == Float.class) {
            return Float.parseFloat(s);
        }
        else if (type == long.class || type == Long.class) {
            return Long.parseLong(s);
        }
        else {
            return s;
        }
    }

    public static String rightTrim(String s, char c) {
        while (s.length() > 0 && s.charAt(s.length() - 1) == c) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
