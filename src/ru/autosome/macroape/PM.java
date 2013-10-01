package ru.autosome.macroape;

import java.util.HashMap;

public class PM {
  final double[][] matrix;
  public String name;

  PM(double[][] matrix, String name) throws IllegalArgumentException {
    for (double[] pos : matrix) {
      if (pos.length != 4) {
        throw new IllegalArgumentException("Matrix must have 4 elements in each position");
      }
    }
    this.matrix = matrix;
    this.name = name;
  }

  public int length() {
    return matrix.length;
  }

  public String toString() {
    String result;
    result = name + "\n";
    for (double[] pos : matrix) {
      result = result + pos[0] + "\t" + pos[1] + "\t" + pos[2] + "\t" + pos[3] + "\n";
    }
    return result;
  }

  public PM reverseComplement() {
    double[][] complement;
    complement = new double[length()][];
    for (int i = 0; i < length(); ++i) {
      complement[i] = ArrayExtensions.reverse(matrix[i]);
    }
    return new PM(ArrayExtensions.reverse(complement), name);
  }

  static HashMap<Character, Integer> indexByLetter() {
    HashMap<Character, Integer> result = new HashMap<Character, Integer>();
    result.put('A', 0);
    result.put('C', 1);
    result.put('G', 2);
    result.put('T', 3);
    return result;
  }
}