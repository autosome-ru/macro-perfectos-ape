package ru.autosome.commons.motifModel.di;

import ru.autosome.commons.backgroundModel.di.DiBackgroundFactory;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.motifModel.BackgroundCompatible;
import ru.autosome.commons.motifModel.MatrixModel;
import ru.autosome.commons.motifModel.Named;

public class DiPM implements Named, MatrixModel, BackgroundCompatible<DiBackgroundModel> {
  public static final int ALPHABET_SIZE = 16;
  protected final double[][] matrix;
  public String name;

  @Override
  public String getName() {
    return name;
  }
  @Override
  public void setName(String name) {
    this.name = name;
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
    return ALPHABET_SIZE;
  }

  @Override
  public DiBackgroundFactory compatibleBackground() {
    return new DiBackgroundFactory();
  }
}
