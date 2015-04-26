package ru.autosome.commons.importer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class InputExtensions {

  static public List<String> readLinesFromFile(String filename) throws FileNotFoundException {
    InputStream reader = new FileInputStream(filename);
    return readLinesFromInputStream(reader);
  }

  static public List<String> readLinesFromFile(File file) throws FileNotFoundException {
    InputStream reader = new FileInputStream(file);
    return readLinesFromInputStream(reader);
  }
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
    List<String> result = new ArrayList<>();
    for (String str : list) {
      if (!str.trim().isEmpty()) {
        result.add(str);
      }
    }
    return result;
  }

  public static List<String> filter_comment_strings(List<String> list) {
    List<String> result = new ArrayList<>();
    for (String str : list) {
      if (str.charAt(0) != '#') {
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

  static public List<String> beforeEmptyLine(List<String> lines) {
    List<String> result = new ArrayList<String>();
    for (String line: lines) {
      if (line.trim().isEmpty()) {
        return result;
      }
      result.add(line);
    }
    return result;
  }

  static public List<String> trimAll(List<String> lines) {
    List<String> result = new ArrayList<String>(lines.size());
    for (String line: lines) {
      result.add(line.trim());
    }
    return result;
  }

  public static List<Double> listOfDoubleTokens(String s) {
    StringTokenizer tokenizer = new StringTokenizer(s, ",");
    List<Double> tokens = new ArrayList<Double>();
    while (tokenizer.hasMoreTokens()) {
      tokens.add(Double.valueOf(tokenizer.nextToken()));
    }
    return tokens;
  }
}
