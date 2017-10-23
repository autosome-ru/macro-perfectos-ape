package ru.autosome.commons.importer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class InputExtensions {

  static public List<String> readLinesFromFile(File file) throws FileNotFoundException {
    InputStream reader = new FileInputStream(file);
    return readLinesFromInputStream(reader);
  }

  static public List<String> readLinesFromInputStream(InputStream in) {
    String newline = System.getProperty("line.separator");
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    return reader.lines().map(line -> line + newline).collect(Collectors.toList());
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
    List<String> result = new ArrayList<>();
    for (String line: lines) {
      if (line.trim().isEmpty()) {
        return result;
      }
      result.add(line);
    }
    return result;
  }

  static public List<String> trimAll(List<String> lines) {
    List<String> result = new ArrayList<>(lines.size());
    for (String line: lines) {
      result.add(line.trim());
    }
    return result;
  }

  public static List<Double> listOfDoubleTokens(String s) {
    StringTokenizer tokenizer = new StringTokenizer(s, ",");
    List<Double> tokens = new ArrayList<>();
    while (tokenizer.hasMoreTokens()) {
      tokens.add(Double.valueOf(tokenizer.nextToken()));
    }
    return tokens;
  }
}
