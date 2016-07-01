package com.rdfex.util;

import android.content.Context;

import com.rdfex.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Anurag Gautam
 */
public class ExUtil {
    public static boolean isVocabularyPresent(Context context) {
        String filename = context.getString(R.string.vocab_file_name);
        return fileExists(context, filename);
    }

    public static boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file.exists() && file.isFile() && file.length() > 0;
    }

    public static BufferedReader newBufferedReader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream));
    }

    public static String readFile(Context context, String filename) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader br = newBufferedReader(context.openFileInput(filename));
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static void writeFile(Context context, String filename, String text) {
        try {
            FileOutputStream fout = context.openFileOutput(filename, 0);
            fout.write(text.getBytes());
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
