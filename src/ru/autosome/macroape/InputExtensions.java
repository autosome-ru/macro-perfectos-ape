package ru.autosome.macroape;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class InputExtensions {
  static public ArrayList<String> readLinesFromInputStream(InputStream in) {
    ArrayList<String> lines    = new ArrayList();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String line;
    String newline = System.getProperty("line.separator");
    try {
      while ((line = reader.readLine()) != null) {
        lines.add(line + newline);
      }
    } catch (IOException e) {
    }
    return lines;
  }

  public static ArrayList<String> filter_empty_strings(ArrayList<String> list) {
    ArrayList<String> result = new ArrayList<String>();
    for (String str: list) {
      if (!str.trim().isEmpty()) {
        result.add(str);
      }
    }
    return result;
  }
}
