package ru.autosome.commons.importer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// Usual parser of 4-column matrix with or without name
// (actually matrix can have any number of columns, so DiPWM matrices can be parsed too)
public class PMParser {
  private double[][] matrix;
  private String name;

  public PMParser(List<String> input) {
    parse(input);
  }

  // trivial parser (helps to read matrix of unknown class)
  public PMParser(double[][] matrix, String name) {
    this.matrix = matrix;
    this.name = name;
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

  public static PMParser from_file(String filename_or_stdin) {
    try {
      if (new File(filename_or_stdin).exists()) {
        InputStream reader = new FileInputStream(filename_or_stdin);
        return new PMParser( InputExtensions.readLinesFromInputStream(reader) );
      } else {
        throw new FileNotFoundException("Error! File " + filename_or_stdin + " doesn't exist");
      }
    } catch (FileNotFoundException err) {
      System.err.println(err.getMessage());
      return null;
    }
  }

  public static String parseName(String line) {
    String name;
    name = line.trim();

    while (!name.isEmpty() && (name.charAt(0) == '>' || name.charAt(0) == ' ' || name.charAt(0) == '\t')) {
      name = name.substring(1, name.length());
    }

    return name;
  }

  public static double[] parseMatrixLine(String line) {
    StringTokenizer parser = new StringTokenizer(line.replaceAll("\\s+", " "));
    double[] data = new double[parser.countTokens()];
    for (int j = 0; j < data.length; ++j) {
      String elem = parser.nextToken(" ");
      data[j] = Double.valueOf(elem);
    }
    return data;
  }

  private void parse(List<String> inp_strings) {
    ArrayList<double[]> matrix_as_list = new ArrayList<double[]>();
    name = "";
    int i = 0;
    if (!InputExtensions.startWithDouble(inp_strings.get(0))) {
      name = parseName(inp_strings.get(0));
      i++;
    }

    for (; i < inp_strings.size(); ++i) {
      matrix_as_list.add( parseMatrixLine(inp_strings.get(i)) );
    }
    matrix = new double[matrix_as_list.size()][];
    for (int j = 0; j < matrix_as_list.size(); ++j) {
      matrix[j] = matrix_as_list.get(j);
    }
  }

  public static PMParser loadFromStream(BufferedPushbackReader reader) {
    List<double[]> matrix = new ArrayList<double[]>();
    String name = "";

    try {
      while(reader.eatEndOfLine()) { }

      { // braces to scope variables
        String line = reader.readLine();
        if (InputExtensions.startWithDouble(line)) {
          reader.unreadLine(line);
        } else {
          name = parseName(line);
        }
      }
      boolean endOfMatrix = false;
      while (!endOfMatrix) {
        String line = reader.readLine();
        if (InputExtensions.startWithDouble(line)) {
          matrix.add(parseMatrixLine(line));
        } else {
          reader.unreadLine(line);
          endOfMatrix = true;
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    if (!matrix.isEmpty()) {
      return new PMParser(matrix.toArray(new double[matrix.size()][]), name);
    } else {
      return null;
    }
  }

  public double[][] matrix() {
    return matrix;
  }

  public String name() {
    return name;
  }
}
