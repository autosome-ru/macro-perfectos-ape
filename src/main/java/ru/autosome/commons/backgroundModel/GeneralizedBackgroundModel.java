package ru.autosome.commons.backgroundModel;

public interface GeneralizedBackgroundModel {
  double probability(int index);

  double volume(); // 1 for probability model, 4 for wordwise model

  @Override
  String toString();

  boolean is_wordwise();

  double mean_value(double[] values);

  double mean_square_value(double[] values);

  double variance(double[] values);
}
