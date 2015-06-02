package ru.autosome.commons.importer;

import ru.autosome.commons.model.Named;
import ru.autosome.commons.support.ArrayExtensions;

import java.util.ArrayList;
import java.util.List;

public class NormalMatrixParser {
  public final int alphabet_size;
  public NormalMatrixParser(int alphabet_size) {
    this.alphabet_size = alphabet_size;
  }

  String getName(List<String> strings) {
    String header = strings.get(0).trim();
    if (header.charAt(0) == '>') {
      return header.substring(1).trim();
    } else if (!InputExtensions.startWithDouble(header)) {
      return header;
    } else {
      return null;
    }
  }

  List<String> withoutHeader(List<String> strings) {
    String header = strings.get(0).trim();
    if (header.charAt(0) == '>' || !InputExtensions.startWithDouble(header)) {
      return strings.subList(1, strings.size());
    } else {
      return strings;
    }
  }

  public Named<double[][]> parse(List<String> strings) {
    String name = getName(strings);
    strings = InputExtensions.beforeEmptyLine(withoutHeader(strings));
    List<double[]> matrix = new ArrayList<double[]>();
    for (String positionString: strings) {
      String[] weights = positionString.split("\\s+");
      if (weights.length != alphabet_size) {
        throw new RuntimeException("Incorrect number of weights per line in the matrix input file.");
      }
      double[] positionParsed = new double[alphabet_size];
      for (int letterIndex = 0; letterIndex < alphabet_size; ++letterIndex) {
        positionParsed[letterIndex] = Double.parseDouble(weights[letterIndex]);
      }
      matrix.add(positionParsed);
    }
    return new Named<double[][]>(ArrayExtensions.toPrimitiveArray(matrix), name);
  }
}
