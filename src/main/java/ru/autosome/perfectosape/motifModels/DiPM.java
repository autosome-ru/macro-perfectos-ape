package ru.autosome.perfectosape.motifModels;

import ru.autosome.perfectosape.backgroundModels.AbstractBackgroundFactory;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundFactory;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;

import java.util.HashMap;

public class DiPM implements Named, MatrixModel, BackgroundCompatible<DiBackgroundModel> {
  public static final int ALPHABET_SIZE = 16;
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

  static final HashMap<String, Integer> indexByLetter;
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


  public DiPM(double[][] matrix, String name) throws IllegalArgumentException {
    for (double[] pos : matrix) {
      if (pos.length != ALPHABET_SIZE) {
        throw new IllegalArgumentException("Matrix must have " + ALPHABET_SIZE + " elements in each position");
      }
    }
    this.matrix = matrix;
    this.name = name;
  }

  // length of TFBS, not of a matrix representation
  public int length() {
    return matrix.length + 1;
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

  @Override
  public double[][] getMatrix() {
    return matrix;
  }

  @Override
  public int alphabetSize() {
    return ALPHABET_SIZE;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public DiBackgroundFactory compatibleBackground() {
    return new DiBackgroundFactory();
  }
}
