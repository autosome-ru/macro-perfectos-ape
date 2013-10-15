package ru.autosome.macroape;

import java.util.HashMap;

public class PM {
  public final double[][] matrix;
  public String name;

  static HashMap<Character, Integer> indexByLetter;
  static {
    indexByLetter = new HashMap<Character, Integer>();
    indexByLetter.put('A', 0);
    indexByLetter.put('C', 1);
    indexByLetter.put('G', 2);
    indexByLetter.put('T', 3);
  }

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
    StringBuilder result = new StringBuilder();
    result.append(name).append("\n");
    for (double[] pos : matrix) {
      for (int letter_index = 0; letter_index < 4; ++ letter_index) {
        if (letter_index != 0) {
          result.append("\t");
        }
        result.append(pos[letter_index]);
      }
      result.append("\n");
    }
    return result.toString();
  }

  public PM reverseComplement() {
    double[][] complement;
    complement = new double[length()][];
    for (int i = 0; i < length(); ++i) {
      complement[i] = ArrayExtensions.reverse(matrix[i]);
    }
    return new PM(ArrayExtensions.reverse(complement), name);
  }
}