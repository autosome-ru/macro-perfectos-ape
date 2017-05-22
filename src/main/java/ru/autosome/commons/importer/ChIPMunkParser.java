package ru.autosome.commons.importer;

import ru.autosome.commons.support.StringExtensions;

import java.util.ArrayList;
import java.util.List;

public class ChIPMunkParser {
  public final int alphabet_size;
  public final String section;
  public final String matrix_start;
  // section is a string like "ru.autosome.di.ChIPMunk" representing start of motif output section
  // matrix_start is a string like "PWAA" representing first position line
  public ChIPMunkParser(int alphabet_size, String section, String matrix_start) {
    this.section = section;
    this.matrix_start = matrix_start;
    this.alphabet_size = alphabet_size;
  }

  public ParsingResult parse(List<String> strings) {
    List<double[]> matrix = new ArrayList<>();

    strings = InputExtensions.trimAll(strings);
    int start = strings.lastIndexOf("OUTC|" + section);
    if (start == -1) {
      throw new RuntimeException("Corrupted ChIPMunk output detected.");
    }
    for (int lineNumber = start; lineNumber < strings.size(); ++lineNumber) {
      if (StringExtensions.startWith(strings.get(lineNumber), matrix_start)) {
        int lengthOfMotif = strings.get(lineNumber).split("\\s+").length;
        for (int positionIndex = 0; positionIndex < lengthOfMotif; ++positionIndex) {
          matrix.add(new double[alphabet_size]);
        }

        for (int letterIndex = 0; letterIndex < alphabet_size; ++letterIndex) {
          String[] weights = strings.get(letterIndex + lineNumber).split("\\|")[1].split("\\s+");
          for (int positionIndex = 0; positionIndex < lengthOfMotif; ++positionIndex) {
            matrix.get(positionIndex)[letterIndex] = Double.parseDouble(weights[positionIndex]);
          }
        }
        break;
      }
    }
    if (matrix.size() == 0) {
      throw new RuntimeException("Corrupted ChIPMunk output detected.");
    }
    return new ParsingResult(matrix, null);
  }
}
