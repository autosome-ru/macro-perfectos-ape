package ru.autosome.perfectosape.importers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class InputExtensions {
  static public List<String> readLinesFromInputStream(InputStream in) {
    List<String> lines = new ArrayList<String>();
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

  public static List<String> filter_empty_strings(List<String> list) {
    List<String> result = new ArrayList<String>();
    for (String str : list) {
      if (!str.trim().isEmpty()) {
        result.add(str);
      }
    }
    return result;
  }

  public static boolean startWithDouble(String s) {
    return isDouble(s.replaceAll("\\s+", " ").split(" ")[0]);
  }

  public static boolean isDouble(String s) {
    try {
      Double.valueOf(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
