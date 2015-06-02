package ru.autosome.commons.importer;

import ru.autosome.commons.model.Named;
import ru.autosome.commons.support.ArrayExtensions;

import java.util.ArrayList;
import java.util.List;

public class TransposedMatrixParser {
  public final int alphabet_size;
  public TransposedMatrixParser(int alphabet_size) {
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

  List<String> beforeEmptyLine(List<String> lines) {
    List<String> result = new ArrayList<String>();
    for (String line: lines) {
      if (line.trim().isEmpty()) {
        return result;
      }
      result.add(line);
    }
    return result;
  }

  public Named<double[][]> parse(List<String> strings) {
    String name = getName(strings);
    strings = beforeEmptyLine(withoutHeader(strings));
    List<double[]> matrix = new ArrayList<double[]>();
    if (strings.size() != alphabet_size) {
      throw new RuntimeException("Incorrect number of weight lines in the transposed matrix input file.");
    }

    int lenghtOfMotif = strings.get(0).split("\\s+").length;
    for (int positionIndex = 0; positionIndex < lenghtOfMotif; ++positionIndex) {
      matrix.add(new double[alphabet_size]);
    }

    for (int letterIndex = 0; letterIndex < alphabet_size; letterIndex ++) {
      String[] weights = strings.get(letterIndex).split("\\s+");
      if (weights.length != lenghtOfMotif) {
        throw new RuntimeException("Different number of elements in positions of transposed matrix input file.");
      }
      for (int positionIndex = 0; positionIndex < lenghtOfMotif; ++positionIndex) {
        matrix.get(positionIndex)[letterIndex] = Double.parseDouble(weights[positionIndex]);
      }
    }
    return new Named<double[][]>(ArrayExtensions.toPrimitiveArray(matrix), name);
  }
}
