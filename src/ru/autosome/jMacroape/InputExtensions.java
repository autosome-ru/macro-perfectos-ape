package ru.autosome.jMacroape;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class InputExtensions {
  static public ArrayList<String> readLinesFromInputStream(InputStream in, Charset cs) {
    ArrayList<String> lines    = new ArrayList();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, cs));
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
}
