package ru.autosome.macroape;

import java.util.HashMap;

public class DiPWM {
  double [][]matrix;
  String name;
  static HashMap<String, Integer> indexByLetter;
  static {
    indexByLetter = new HashMap<String, Integer>();
    indexByLetter.put("AA", 0);
    indexByLetter.put("AC", 1);
    indexByLetter.put("AG", 2);
    indexByLetter.put("AT", 3);

    indexByLetter.put("CA", 4);
    indexByLetter.put("CC", 5);
    indexByLetter.put("CG", 6);
    indexByLetter.put("CT", 7);

    indexByLetter.put("GA", 8);
    indexByLetter.put("GC", 9);
    indexByLetter.put("GG", 10);
    indexByLetter.put("GT", 11);

    indexByLetter.put("TA", 12);
    indexByLetter.put("TC", 13);
    indexByLetter.put("TG", 14);
    indexByLetter.put("TT", 15);
  }

  public DiPWM(double[][] matrix, String name) {
    for (double[] pos : matrix) {
      if (pos.length != 16) {
        throw new IllegalArgumentException("Matrix must have 16 elements in each position");
      }
    }
    this.matrix = matrix;
    this.name = name;
  };

  public int length() {
    return matrix.length + 1;
  }

}
