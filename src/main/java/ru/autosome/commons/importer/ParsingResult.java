package ru.autosome.commons.importer;

import ru.autosome.commons.support.ArrayExtensions;

import java.util.List;

public class ParsingResult {
  private final double[][] matrix;
  private final String name;
  public ParsingResult(double[][] matrix, String name) {
    this.matrix = matrix;
    this.name = name;
  }
  public ParsingResult(List<double[]> matrix, String name) {
    this.matrix = ArrayExtensions.toPrimitiveArray(matrix);
    this.name = name;
  }
  public double[][] getMatrix() { return matrix; }
  public String getName() { return name; }
}
