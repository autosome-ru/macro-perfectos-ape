package ru.autosome.perfectosape.backgroundModels;

import ru.autosome.perfectosape.ArrayExtensions;

import java.util.StringTokenizer;

public class DiBackground implements DiBackgroundModel {
  private double[] background;

  // TODO: whether we should check symmetricity of background
  private DiBackground(double[] background) {
    if (Math.abs(ArrayExtensions.sum(background) - 1.0) > 0.0001) {
      throw new IllegalArgumentException("Background probabilities should be 1.0 being summarized");
    }
    this.background = background;
  }

  private static DiBackgroundModel fromArray(double[] background) {
    if (background.length != ALPHABET_SIZE) {
      throw new IllegalArgumentException("Background constructor accepts double array of length 4");
    }
    boolean wordwise = true;
    for (int i = 0; i < ALPHABET_SIZE; ++i) {
      if (Math.abs(background[i] - 1) > 0.0001) {
        wordwise = false;
      }
    }
    if (wordwise) {
      return new DiWordwiseBackground();
    } else {
      return new DiBackground(background);
    }
  }

  @Override
  public double[] probability() {
    return background;
  }

  @Override
  public double probability(int index) {
    return background[index];
  }

  @Override
  public double[] count() {
    return background;
  }

  @Override
  public double count(int index) {
    return background[index];
  }

  @Override
  public double volume() {
    return 1;
  }

  public static DiBackgroundModel fromString(String s) {
    double[] background = new double[ALPHABET_SIZE];
    StringTokenizer parser = new StringTokenizer(s);
    for (int i = 0; i < ALPHABET_SIZE; ++i) {
      background[i] = Double.valueOf(parser.nextToken(","));
    }
    return DiBackground.fromArray(background);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < ALPHABET_SIZE; ++i) {
      if (i != 0) {
        builder.append(',');
      }
      builder.append(background[i]);
    }
    return builder.toString();
  }

  @Override
  public boolean is_wordwise() {
    return false;
  }

  @Override
  public double mean_value(double[] values) {
    double result = 0;
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      result += values[letter] * probability(letter);
    }
    return result;
  }

  @Override
  public double mean_square_value(double[] values) {
    double mean_square = 0.0;
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      mean_square += values[letter] * values[letter] * probability(letter);
    }
    return mean_square;
  }
}
