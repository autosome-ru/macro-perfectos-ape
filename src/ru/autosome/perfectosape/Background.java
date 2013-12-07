package ru.autosome.perfectosape;

import java.util.StringTokenizer;

public class Background implements BackgroundModel {
  static final int ALPHABET_SIZE = 4;
  private double[] background;

  // TODO: whether we should check symmetricity of background
  public Background(double[] background) {
    if (Math.abs(ArrayExtensions.sum(background) - 1.0) > 0.0001) {
      throw new IllegalArgumentException("Background probabilities should be 1.0 being summarized");
    }
    this.background = background;
  }

  public static BackgroundModel fromArray(double[] background) {
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
      return new WordwiseBackground();
    } else {
      return new Background(background);
    }
  }

  public double[] probability() {
    return background;
  }

  public double probability(int index) {
    return background[index];
  }

  public double[] count() {
    return background;
  }

  public double count(int index) {
    return background[index];
  }

  public double volume() {
    return 1;
  }

  public static BackgroundModel fromString(String s) {
    double[] background = new double[4];
    StringTokenizer parser = new StringTokenizer(s);
    for (int i = 0; i < ALPHABET_SIZE; ++i) {
      background[i] = Double.valueOf(parser.nextToken(","));
    }
    return Background.fromArray(background);
  }

  public String toString() {
    return background[0] + "," + background[1] + "," + background[2] + "," + background[3];
  }

  public boolean is_wordwise() {
    return false;
  }

  public double mean_value(double[] values) {
    double result = 0;
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      result += values[letter] * probability(letter);
    }
    return result;
  }

  public double mean_square_value(double[] values) {
    double mean_square = 0.0;
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      mean_square += values[letter] * values[letter] * probability(letter);
    }
    return mean_square;
  }

  public boolean equals(BackgroundModel other) {
    boolean result = true;
    for (int i = 0; i < ALPHABET_SIZE; ++i) {
      result = result && count(i) == other.count(i);
    }
    return result;
  }
}