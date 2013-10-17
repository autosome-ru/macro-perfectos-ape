package ru.autosome.macroape;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

// Usual parser of 4-column matrix with or without name
// (actually matrix can have any number of columns, so DiPWM matrices can be parsed too)
public class PMParser {
  private final ArrayList<String> inp_strings;
  private double[][] matrix;
  private String name;

  public PMParser(ArrayList<String> input) {
    inp_strings = input;
    parse();
  }

  public static PMParser from_file(File input_file) {
    try {
      InputStream reader = new FileInputStream(input_file);
      return new PMParser( InputExtensions.readLinesFromInputStream(reader) );
    } catch (FileNotFoundException err) {
      System.err.println(err.getMessage());
      return null;
    }
  }

  public static PMParser from_file_or_stdin(String filename_or_stdin) {
    try {
      InputStream reader;
      if (filename_or_stdin.equals(".stdin")) {
        reader = System.in;
      } else {
        if (new File(filename_or_stdin).exists()) {
          reader = new FileInputStream(filename_or_stdin);
        } else {
          throw new FileNotFoundException("Error! File #{filename} doesn't exist");
        }
      }
      return new PMParser( InputExtensions.readLinesFromInputStream(reader) );
    } catch (FileNotFoundException err) {
      System.err.println(err.getMessage());
      return null;
    }
  }


  void parse() {
    ArrayList<double[]> matrix_as_list = new ArrayList<double[]>();
    name = "";
    int i = 0;
    try {
      Double.valueOf(inp_strings.get(0).replaceAll("\\s+", " ").split(" ")[0]);
    } catch (NumberFormatException e) {
      name = inp_strings.get(0).trim();
      while (name.charAt(0) == '>' || name.charAt(0) == ' ' || name.charAt(0) == '\t') {
        name = name.substring(1, name.length());
      }
      i++;
    }

    for (; i < inp_strings.size(); ++i) {
      StringTokenizer parser = new StringTokenizer(inp_strings.get(i).replaceAll("\\s+", " "));
      double[] tmp = new double[parser.countTokens()];
      for (int j = 0; j < tmp.length; ++j) {
        String elem = parser.nextToken(" ");
        tmp[j] = Double.valueOf(elem);
      }
      matrix_as_list.add(tmp);
    }
    matrix = new double[matrix_as_list.size()][];
    for (int j = 0; j < matrix_as_list.size(); ++j) {
      matrix[j] = matrix_as_list.get(j);
    }
  }

  double[][] matrix() {
    return matrix;
  }

  String name() {
    return name;
  }
}
