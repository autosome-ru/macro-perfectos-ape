package ru.autosome.perfectosape.motifModels;

import gnu.trove.impl.unmodifiable.TUnmodifiableCharIntMap;
import gnu.trove.map.TCharIntMap;
import gnu.trove.map.hash.TCharIntHashMap;

public class PM implements Named {
  public static final int ALPHABET_SIZE = 4;
  public final double[][] matrix;
  public String name;

  @Override
  public String getName() {
    return name;
  }
  @Override
  public void setName(String name) {
    this.name = name;
  }

  protected static final TCharIntMap indexByLetter =
   new TUnmodifiableCharIntMap( new TCharIntHashMap(new char[]{'A','C','G','T'},
                                                    new int[] {0, 1, 2, 3}) );

  public PM(double[][] matrix, String name) throws IllegalArgumentException {
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