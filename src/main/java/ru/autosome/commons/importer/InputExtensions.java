package ru.autosome.commons.importer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

  static public List<String> beforeEmptyLine(List<String> lines) {
    List<String> result = new ArrayList<String>();
    for(int i = 0; i < lines.size(); ++i) {
      if (lines.get(i).trim().isEmpty()) {
        return result;
      }
      result.add(lines.get(i));
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
}
