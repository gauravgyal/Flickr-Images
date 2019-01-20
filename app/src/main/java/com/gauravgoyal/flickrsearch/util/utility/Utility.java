package com.gauravgoyal.flickrsearch.util.utility;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Utility {

    public static String toString(BufferedReader reader){
        StringBuilder line = new StringBuilder();
        String text;

        try {
            while ((text = reader.readLine()) != null) {
                line.append(text+"\n");
            }

            return line.toString();
        }
        catch (Exception e){
            return null;
        }
    }
}
