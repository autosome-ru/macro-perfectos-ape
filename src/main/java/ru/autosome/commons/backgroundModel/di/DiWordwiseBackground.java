package ru.autosome.commons.backgroundModel.di;

import static ru.autosome.commons.model.indexingScheme.DiIndexingScheme.diIndex;

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
  public double variance(double[] values) {
    double mean = mean_value(values);
    return mean_square_value(values) - mean * mean;
  }

  @Override
  public double average_by_second_letter(double[] values, int firstLetterIndex) {
    double result = 0;
    for (int secondLetterIndex = 0; secondLetterIndex < 4; ++secondLetterIndex) {
      int letter = diIndex(firstLetterIndex, secondLetterIndex);
      result += values[letter];
    }
    return result / 4.0;
  }

  @Override
  public double average_by_first_letter(double[] values, int secondLetterIndex) {
    double result = 0;
    for (int firstLetterIndex = 0; firstLetterIndex < 4; ++firstLetterIndex) {
      int letter = diIndex(firstLetterIndex, secondLetterIndex);
      result += values[letter];
    }
    return result / 4.0;
  }
}
