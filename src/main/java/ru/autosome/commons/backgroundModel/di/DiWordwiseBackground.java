package ru.autosome.commons.backgroundModel.di;

public class DiWordwiseBackground implements DiBackgroundModel {
  public DiWordwiseBackground() {
  }

  @Override
  public double probability(int index) {
    return 0.0625;
  }

  @Override
  public double conditionalCount(int previousLetter, int letter) {
    return 1.0;
  }

  @Override
  public double countAnyFirstLetter(int secondLetter) {
    return 1.0;
  }

  @Override
  public double countAnySecondLetter(int firstLetter) {
    return 1.0;
  }

  @Override
  public double volume() {
    return 4;
  }

  @Override
  public String toString() {
    return "1,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1";
  }

  @Override
  public boolean is_wordwise() {
    return true;
  }

  @Override
  public double mean_value(double[] values) {
    double sum = 0;
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      sum += values[letter];
    }
    return sum / ALPHABET_SIZE;
  }

  @Override
  public double mean_square_value(double[] values) {
    double sum_square = 0.0;
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      sum_square += values[letter] * values[letter];
    }
    return sum_square / ALPHABET_SIZE;
  }
}
