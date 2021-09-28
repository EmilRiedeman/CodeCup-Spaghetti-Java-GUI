package spaghetti.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Utils {
    public static String getFileExtension(File f) {
        String extension = "";

        int i = f.getName().lastIndexOf('.');
        if (i > 0) extension = f.getName().substring(i+1);

        return extension.toLowerCase();
    }

    public static String[] splitBackslash(String string, char regex) {
        List<String> arr = new ArrayList<>();
        StringBuilder sub = new StringBuilder();
        boolean prevBackslash = false;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c == regex && !prevBackslash) {
                arr.add(sub.toString());
                sub = new StringBuilder();
            } else {
                if (prevBackslash || c != '\\') {
                    if (c != regex && c != '\\' && prevBackslash) sub.append('\\');
                    sub.append(c);
                }
            }
            prevBackslash = c == '\\' && !prevBackslash;
        }
        arr.add(sub.toString());
        return arr.toArray(new String[0]);
    }

    public static <T> Pair<T, T> swap(T a, T b) {
        return new Pair<>(b, a);
    }
}
