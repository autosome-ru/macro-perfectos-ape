package ru.autosome.commons.backgroundModel.mono;

public class WordwiseBackground implements BackgroundModel {
  public WordwiseBackground() {
  }

  @Override
  public double probability(int index) {
    return 0.25;
  }

  @Override
  public double count(int index) {
    return 1.0;
  }

  @Override
  public double volume() {
    return 4;
  }

  @Override
  public String toString() {
    return "wordwise";
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

  @Override
  public boolean equals(Object other) {
    return other instanceof BackgroundModel && ((BackgroundModel) other).is_wordwise();
  }
}
