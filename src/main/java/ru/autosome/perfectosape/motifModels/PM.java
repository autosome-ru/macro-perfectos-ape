package ru.autosome.perfectosape.motifModels;

import gnu.trove.impl.unmodifiable.TUnmodifiableCharIntMap;
import gnu.trove.map.TCharIntMap;
import gnu.trove.map.hash.TCharIntHashMap;
import ru.autosome.perfectosape.backgroundModels.BackgroundFactory;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;

public class PM implements Named, MatrixModel, BackgroundCompatible<BackgroundModel> {
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

  @Override
  public double[][] getMatrix() {
    return matrix;
  }

  @Override
  public int alphabetSize() {
    return ALPHABET_SIZE;  //To change body of implemented methods use File | Settings | File Templates.
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

  @Override
  public BackgroundFactory compatibleBackground() {
    return new BackgroundFactory();
  }
}