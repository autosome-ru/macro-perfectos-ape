package ru.autosome.perfectosape.backgroundModels;

public class DiWordwiseBackground implements DiBackgroundModel {
  public DiWordwiseBackground() {
  }

  @Override
  public double[] probability() {
    double[] result;
    result = new double[]{0.25, 0.25, 0.25, 0.25,
                          0.25, 0.25, 0.25, 0.25,
                          0.25, 0.25, 0.25, 0.25,
                          0.25, 0.25, 0.25, 0.25};
    return result;
  }

  @Override
  public double probability(int index) {
    return 0.25;
  }

  @Override
  public double[] count() {
    double[] result;
    result = new double[]{1.0, 1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0, 1.0,
                          1.0, 1.0, 1.0, 1.0};
    return result;
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
    return "1,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,1,1";
  }

  @Override
  public boolean is_wordwise() {
    return true;
  }

  @Override
  public double mean_value(double[] values) {
    double sum = 0;
    for (int letter = 0; letter < 16; ++letter) {
      sum += values[letter];
    }
    return sum / 16.0;
  }

  @Override
  public double mean_square_value(double[] values) {
    double sum_square = 0.0;
    for (int letter = 0; letter < 16; ++letter) {
      sum_square += values[letter] * values[letter];
    }
    return sum_square / 16.0;
  }
}

