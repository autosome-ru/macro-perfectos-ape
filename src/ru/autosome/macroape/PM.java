package ru.autosome.macroape;

import java.util.HashMap;

public class PM {
  final double[][] matrix;
  BackgroundModel background;
  String name;

  public PM(double[][] matrix, BackgroundModel background, String name) throws IllegalArgumentException {
    for (double[] pos: matrix) {
      if (pos.length != 4) {
        throw new IllegalArgumentException("Matrix must have 4 elements in each position");
      }
    }
    this.matrix = matrix;
    this.background = background;
    this.name = name;
  }

  public int length() {
    return matrix.length;
  }

  public double[] probabilities() {
    return background.probability();
  }
  public String toString() {
    String result;
    result = name + "\n";
    for (double[] pos: matrix) {
      result = result + pos[0] + "\t" + pos[1] + "\t" + pos[2] + "\t" + pos[3] + "\n";
    }
    return result;
  }

  public PM reverseComplement() {
    double[][] mat_result;
    mat_result = new double[length()][];
    for (int i = 0; i < length(); ++i) {
      mat_result[i] = new double[4];
      for (int j = 0; j < 4; ++j) {
        mat_result[i][j] = matrix[length() - 1  - i][4 - 1 - j];
      }
    }
    return new PM(mat_result, background, name);
  }

  public static HashMap<Character, Integer> indexByLetter() {
    HashMap<Character, Integer> result = new HashMap<Character,Integer>();
    result.put('A', 0);
    result.put('C', 1);
    result.put('G', 2);
    result.put('T', 3);
    return result;
  }
}