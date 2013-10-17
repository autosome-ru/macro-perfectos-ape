package ru.autosome.macroape;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class PM {
  static final int ALPHABET_SIZE = 4;
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
      if (pos.length != ALPHABET_SIZE) {
        throw new IllegalArgumentException("Matrix must have " + ALPHABET_SIZE + " elements in each position");
      }
    }
    this.matrix = matrix;
    this.name = name;
  }

  public int length() {
    return matrix.length;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(name).append("\n");
    for (double[] pos : matrix) {
      for (int letter_index = 0; letter_index < ALPHABET_SIZE; ++ letter_index) {
        if (letter_index != 0) {
          result.append("\t");
        }
        result.append(pos[letter_index]);
      }
      result.append("\n");
    }
    return result.toString();
  }
}