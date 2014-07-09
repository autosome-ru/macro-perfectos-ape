package ru.autosome.commons.importer;

import java.util.ArrayList;
import java.util.List;

public class MatrixParser {
  public static boolean startWith(String string, String stringStart) {
    return (string.length() >= stringStart.length()) && string.subSequence(0, stringStart.length()).equals(stringStart);
  }

  public static List<double[]> parseDi(ArrayList<String> strings, boolean transpose) {
    if (startWith(strings.get(0), "PROG|ru.autosome.di.ChIPMunk")) {
      return parseChIPMunkOutput(strings);
    } else { // load basic matrix
      removeHeader(strings);
      if (transpose) {
        return parseTransposedMatrix(strings);
      } else {
        return parseNormalMatrix(strings);
      }
    }
  }

  private static List<double[]> parseNormalMatrix(List<String> strings) {
    List<double[]> result = new ArrayList<double[]>();
    for (String positionString: strings) {
      String[] weights = positionString.split(" |\t");
      if (weights.length != 16) {
        throw new RuntimeException("Incorrect number of weights per line in the matrix input file.");
      }
      double[] positionParsed = new double[16];
      for (int letterIndex = 0; letterIndex < 16; ++letterIndex) {
        positionParsed[letterIndex] = Double.parseDouble(weights[letterIndex]);
      }
      result.add(positionParsed);
    }
    return result;
  }

  private static List<double[]> parseTransposedMatrix(List<String> strings) {
    List<double[]> result = new ArrayList<double[]>();
    if (strings.size() != 16) {
      throw new RuntimeException("Incorrect number of weight lines in the transposed matrix input file.");
    }

    int lenghtOfMotif = strings.get(0).split(" |\t").length;
    for (int positionIndex = 0; positionIndex < lenghtOfMotif; ++positionIndex) {
      result.add(new double[16]);
    }

    for (int letterIndex = 0; letterIndex < 16; letterIndex ++) {
      String[] weights = strings.get(letterIndex).split(" |\t");
      if (weights.length != lenghtOfMotif) {
        throw new RuntimeException("Different number of elements in positions of transposed matrix input file.");
      }
      for (int positionIndex = 0; positionIndex < lenghtOfMotif; ++positionIndex) {
        result.get(positionIndex)[letterIndex] = Double.parseDouble(weights[positionIndex]);
      }
    }
    return result;
  }

  private static void removeHeader(List<String> strings) {
    String header = strings.get(0);
    if (header.charAt(0) == '>' || header.split(" |\t").length == 1 || header.matches("A|a") ) {
      // skip 1-line header;
      // either 1 id or anything starting with ">" or
      // anything including "A" as a potential "AA AC AG AT.." or similar
      strings.remove(0);
    }
  }

  private static List<double[]> parseChIPMunkOutput(List<String> strings) {
    List<double[]> result = new ArrayList<double[]>();
    int start = strings.lastIndexOf("OUTC|ru.autosome.di.ChIPMunk");
    for (int lineNumber = start; lineNumber < strings.size(); ++lineNumber) {
      if (startWith(strings.get(lineNumber), "PWAA")) {
        int lengthOfMotif = strings.get(lineNumber).split(" |\t").length;
        for (int positionIndex = 0; positionIndex < lengthOfMotif; ++positionIndex) {
          result.add(new double[16]);
        }

        for (int letterIndex = 0; letterIndex < 16; ++letterIndex) {
          String[] weights = strings.get(letterIndex + lineNumber).split("\\|")[1].split(" |\t");
          for (int positionIndex = 0; positionIndex < lengthOfMotif; ++positionIndex) {
            result.get(positionIndex)[letterIndex] = Double.parseDouble(weights[positionIndex]);
          }
        }
        break;
      }
    }
    if (result.size() == 0) {
      throw new RuntimeException("Corrupted ChIPMunk output detected.");
    }
    return result;
  }

}
