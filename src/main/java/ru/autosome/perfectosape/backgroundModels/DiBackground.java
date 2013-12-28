package ru.autosome.perfectosape.backgroundModels;

import ru.autosome.perfectosape.ArrayExtensions;

import java.util.StringTokenizer;

public class DiBackground implements BackgroundModel {
  private double[] background;

  // TODO: whether we should check symmetricity of background
  private DiBackground(double[] background) {
    if (Math.abs(ArrayExtensions.sum(background) - 1.0) > 0.0001) {
      throw new IllegalArgumentException("Background probabilities should be 1.0 being summarized");
    }
    this.background = background;
  }

  private static BackgroundModel fromArray(double[] background) {
    if (background.length != 16) {
      throw new IllegalArgumentException("Background constructor accepts double array of length 4");
    }
    boolean wordwise = true;
    for (int i = 0; i < 16; ++i) {
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

  public static BackgroundModel fromString(String s) {
    double[] background = new double[16];
    StringTokenizer parser = new StringTokenizer(s);
    for (int i = 0; i < 16; ++i) {
      background[i] = Double.valueOf(parser.nextToken(","));
    }
    return DiBackground.fromArray(background);
  }

  @Override
  public String toString() {
    return  background[0] + "," + background[1] + "," + background[2] + "," + background[3] + ", " +
            background[4] + "," + background[5] + "," + background[6] + "," + background[7] + ", " +
            background[8] + "," + background[9] + "," + background[10] + "," + background[11] + ", " +
            background[12] + "," + background[13] + "," + background[14] + "," + background[15];
  }

  @Override
  public boolean is_wordwise() {
    return false;
  }

  @Override
  public double mean_value(double[] values) {
    double result = 0;
    for (int letter = 0; letter < 16; ++letter) {
      result += values[letter] * probability(letter);
    }
    return result;
  }

  @Override
  public double mean_square_value(double[] values) {
    double mean_square = 0.0;
    for (int letter = 0; letter < 16; ++letter) {
      mean_square += values[letter] * values[letter] * probability(letter);
    }
    return mean_square;
  }
}
